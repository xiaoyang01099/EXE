#version 430 core

struct Particle {
    vec4 position;
    vec4 velocity;
    vec4 startColor;
    vec4 midColor;
    vec4 endColor;
    vec4 render;
    vec4 motion;
    vec4 origin;
    vec4 extra;
    vec4 speedParams;
    vec4 renderParams;
    vec4 sizeParams;
    vec4 sizeControl;
};

struct ParticleMaterialGpu {
    vec4 baseSpriteUV;
    vec4 noiseSpriteUV0;
    vec4 noiseSpriteUV1;
    vec4 topDissolveSpriteUV;
    vec4 noiseParams;
    vec4 bloomParams;
    vec4 flags;
};

layout(std430, binding = 0) readonly buffer ParticleBuffer {
    Particle particles[];
};

layout(std430, binding = 2) readonly buffer ParticleMaterialBuffer {
    ParticleMaterialGpu materials[];
};

layout(std430, binding = 3) readonly buffer ActiveIndexBuffer {
    uint activeIndices[];
};

uniform mat4 uProjection;
uniform mat4 uView;
uniform float uTime;
uniform int uRenderPipelineId;
uniform int uMaxParticles;

const int MATERIAL_COUNT = 8; // 材质表包含上升冲击波粒子，共 8 个材质。

layout(location = 0) in vec2 aPos;

out vec4 vColor;
out vec2 vCoreTexCoord;
out vec2 vBloomTexCoord;
flat out vec3 vParticleSeed;
flat out int vMaterialId;
flat out vec2 vLightEffectMaskParams;

// 光效粒子同样使用 start/mid/end 三段颜色，方便做金色核心到橙红消散。
vec4 resolveParticleColor(Particle p, float ageT) {
    float midT = clamp(p.renderParams.x, 0.001, 0.999);
    if (ageT < midT) {
        return mix(p.startColor, p.midColor, ageT / midT);
    }
    return mix(p.midColor, p.endColor, (ageT - midT) / (1.0 - midT));
}

// 根据出生、中间、结束三段参数计算当前生命周期尺寸。
vec2 resolveParticleSize(Particle p, float ageT) {
    float midT = clamp(p.sizeControl.x, 0.001, 0.999);
    if (ageT < midT) {
        return mix(p.render.xy, p.sizeParams.xy, ageT / midT);
    }
    return mix(p.sizeParams.xy, p.sizeParams.zw, (ageT - midT) / (1.0 - midT));
}

void main() {
    // active index 已经由 compute 按 pipeline 压缩，当前实例先转成真实粒子下标。
    int activeBase = uRenderPipelineId * uMaxParticles;
    int particleIndex = int(activeIndices[activeBase + gl_InstanceID]);
    Particle p = particles[particleIndex];
    float life = p.position.w;
    float totalLife = max(p.velocity.w, 0.0001);
    // 材质表缩减后旧粒子可能残留旧 ID，这里先夹紧再查表，避免 SSBO 越界。
    int materialId = clamp(int(p.extra.w + 0.5), 0, MATERIAL_COUNT - 1);
    int particlePipelineId = int(materials[materialId].flags.x + 0.5);

    // 防御性过滤异常粒子，正常情况下不应触发。
    if (life <= 0.0 || particlePipelineId != uRenderPipelineId) {
        gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
        vColor = vec4(0.0);
        vCoreTexCoord = vec2(0.0);
        vBloomTexCoord = vec2(0.0);
        vParticleSeed = vec3(0.0);
        vMaterialId = 0;
        vLightEffectMaskParams = vec2(0.20, 0.18);
        return;
    }

    float ageT = clamp(1.0 - life / totalLife, 0.0, 1.0);
    vec4 color = resolveParticleColor(p, ageT);
    color.a *= smoothstep(0.0, 0.5, life / totalLife);

    vec2 size = max(resolveParticleSize(p, ageT), vec2(0.001));
    float rotation = p.render.z;
    float c = cos(rotation);
    float s = sin(rotation);
    vec2 local = vec2(
        aPos.x * size.x * c - aPos.y * size.y * s,
        aPos.x * size.x * s + aPos.y * size.y * c
    );

    // billboard 始终朝向相机，Bloom 范围改由片元 shader 和后处理参数控制。
    vec3 right = vec3(uView[0][0], uView[1][0], uView[2][0]);
    vec3 up = vec3(uView[0][1], uView[1][1], uView[2][1]);
    vec3 worldPos = p.position.xyz + right * local.x + up * local.y;

    gl_Position = uProjection * uView * vec4(worldPos, 1.0);
    vColor = color;
    vCoreTexCoord = aPos + vec2(0.5);
    vBloomTexCoord = aPos + vec2(0.5);
    vParticleSeed = vec3(p.extra.y, p.extra.z, p.origin.w);
    vMaterialId = materialId;
    // 每个发射器可独立设置最终圆形遮罩，不再由片元 Shader 全局常量控制。
    vLightEffectMaskParams = p.sizeControl.zw;
}
