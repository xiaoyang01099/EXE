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
out vec2 vTexCoord;
flat out vec2 vNoiseSpeedSeed;
flat out int vMaterialId;

// 根据 start/mid/end 三段颜色计算当前生命周期颜色。
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
    // active index 按 pipeline 分段保存，先恢复真实粒子下标并校验材质批次。
    int activeBase = uRenderPipelineId * uMaxParticles;
    int particleIndex = int(activeIndices[activeBase + gl_InstanceID]);
    Particle p = particles[particleIndex];
    float life = p.position.w;
    float totalLife = max(p.velocity.w, 0.0001);
    int materialId = clamp(int(p.extra.w + 0.5), 0, MATERIAL_COUNT - 1);
    int particlePipelineId = int(materials[materialId].flags.x + 0.5);

    // 异常粒子移出裁剪空间，避免错误材质进入 EX 剑气批次。
    if (life <= 0.0 || particlePipelineId != uRenderPipelineId) {
        gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
        vColor = vec4(0.0);
        vTexCoord = vec2(0.0);
        vNoiseSpeedSeed = vec2(0.0);
        vMaterialId = 0;
        return;
    }

    float ageT = clamp(1.0 - life / totalLife, 0.0, 1.0);
    vec4 color = resolveParticleColor(p, ageT);
    color.a *= smoothstep(0.0, 0.12, life / totalLife);

    // EX 剑气以底边中心作为局部原点，尺寸增长时只向上展开，不再从中心向地下延伸半个高度。
    vec2 size = max(resolveParticleSize(p, ageT), vec2(0.001));
    float rotation = p.render.z;
    float c = cos(rotation);
    float s = sin(rotation);
    vec2 anchoredLocal = vec2(
        aPos.x * size.x,
        (aPos.y + 0.5) * size.y
    );

    // 手动旋转围绕底边中心执行；正式 EX 剑气使用 0 角度，底边在整个生命周期保持稳定。
    vec2 local = vec2(
        anchoredLocal.x * c - anchoredLocal.y * s,
        anchoredLocal.x * s + anchoredLocal.y * c
    );

    // 粒子 direction 同时作为剑气平面法线；忽略 Y 分量保证四边形始终竖直。
    vec3 planeNormal = vec3(p.velocity.x, 0.0, p.velocity.z);
    if (length(planeNormal) < 0.00001) {
        planeNormal = vec3(0.0, 0.0, 1.0);
    } else {
        planeNormal = normalize(planeNormal);
    }
    vec3 worldUp = vec3(0.0, 1.0, 0.0);
    vec3 right = normalize(cross(worldUp, planeNormal));
    vec3 worldPos = p.position.xyz + right * local.x + worldUp * local.y;

    gl_Position = uProjection * uView * vec4(worldPos, 1.0);
    vColor = color;
    // 四边形局部 Y 向上，而纹理 V 向下；只翻转 V 坐标修正 EX 剑气上下颠倒，不改变世界朝向。
    vTexCoord = vec2(aPos.x + 0.5, 0.5 - aPos.y);
    // p.extra.yz 是 Compute Shader 在出生阶段生成并保持稳定的两个随机值。
    vNoiseSpeedSeed = p.extra.yz;
    vMaterialId = materialId;
}
