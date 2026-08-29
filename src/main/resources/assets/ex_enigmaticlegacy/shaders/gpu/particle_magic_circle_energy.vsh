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
out float vAgeT;
flat out int vMaterialId;

// 基础能量法阵继续使用粒子的三段颜色，使基础阶段和增强阶段可以只在调用点调整亮度。
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
    // active index 按 pipeline 分段保存，先恢复真实粒子下标并读取材质。
    int activeBase = uRenderPipelineId * uMaxParticles;
    int particleIndex = int(activeIndices[activeBase + gl_InstanceID]);
    Particle p = particles[particleIndex];
    float life = p.position.w;
    float totalLife = max(p.velocity.w, 0.0001);
    int materialId = clamp(int(p.extra.w + 0.5), 0, MATERIAL_COUNT - 1);
    int particlePipelineId = int(materials[materialId].flags.x + 0.5);

    // 防御性过滤异常粒子，避免错误材质进入水平法阵批次。
    if (life <= 0.0 || particlePipelineId != uRenderPipelineId) {
        gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
        vColor = vec4(0.0);
        vTexCoord = vec2(0.0);
        vAgeT = 0.0;
        vMaterialId = 0;
        return;
    }

    float ageT = clamp(1.0 - life / totalLife, 0.0, 1.0);
    vec4 color = resolveParticleColor(p, ageT);
    // 生命周期末段统一淡出，避免重复法阵交接时突然消失。
    color.a *= smoothstep(0.0, 0.18, life / totalLife);

    // 不使用相机 right/up 轴，直接在世界 XZ 平面展开四边形，保证法阵始终水平朝上。
    vec2 size = max(resolveParticleSize(p, ageT), vec2(0.001));
    vec2 local = aPos * size;
    // 局部 Y 映射到世界 -Z，使原 TRIANGLE_STRIP 的正面法线朝向 +Y，兼容上游启用面剔除的状态。
    vec3 worldPos = p.position.xyz + vec3(local.x, 0.0, -local.y);

    gl_Position = uProjection * uView * vec4(worldPos, 1.0);
    vColor = color;
    vTexCoord = aPos + vec2(0.5);
    vAgeT = ageT;
    vMaterialId = materialId;
}
