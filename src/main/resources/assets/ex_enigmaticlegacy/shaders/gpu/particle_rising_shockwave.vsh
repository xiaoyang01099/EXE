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
const int RISING_SHOCKWAVE_SEGMENTS = 32; // 圆台圆周分段数，必须与 Java 侧间接绘制顶点数一致。
const float TWO_PI = 6.28318530718; // 完整圆周弧度。
const float TOP_RADIUS_RATIO = 0.82; // 顶部半径相对底部半径比例，形成上窄下宽主体。
const float MIN_RADIUS = 0.001; // 半径保护，避免粒子尺寸过小时法线异常。
const float MIN_HEIGHT = 0.001; // 高度保护，避免法线坡度除零。

out vec4 vColor;
out vec2 vLocalUv;
out vec2 vUvTile;
out vec3 vWorldPos;
out vec3 vWorldNormal;
out float vAge;
out float vAgeT;
out float vTotalLife;
flat out int vMaterialId;
flat out float vEffectPower;
flat out float vDissolvePower;
flat out float vUvFlowSpeed;

// 根据 start/mid/end 三段颜色计算当前生命周期颜色。
vec4 resolveParticleColor(Particle p, float ageT) {
    float midT = clamp(p.renderParams.x, 0.001, 0.999);
    if (ageT < midT) {
        return mix(p.startColor, p.midColor, ageT / midT);
    }
    return mix(p.midColor, p.endColor, (ageT - midT) / (1.0 - midT));
}

// 根据出生、中间、结束三段参数计算当前生命周期底部直径和高度。
vec2 resolveParticleSize(Particle p, float ageT) {
    float midT = clamp(p.sizeControl.x, 0.001, 0.999);
    if (ageT < midT) {
        return mix(p.render.xy, p.sizeParams.xy, ageT / midT);
    }
    return mix(p.sizeParams.xy, p.sizeParams.zw, (ageT - midT) / (1.0 - midT));
}

// 围绕世界 Y 轴旋转局部圆台顶点，让发射器 rotation 能整体转动纹理接缝方向。
vec3 rotateAroundY(vec3 value, float angle) {
    float c = cos(angle);
    float s = sin(angle);
    return vec3(
        value.x * c + value.z * s,
        value.y,
        -value.x * s + value.z * c
    );
}

// 将每个圆周分段拆成两个三角形，返回当前顶点对应的圆周角和高度比例。
void resolveCylinderVertex(out float theta, out float heightT, out float u) {
    int segment = gl_VertexID / 6;
    int corner = gl_VertexID - segment * 6;
    float segment0 = float(segment);
    float segment1 = float(segment + 1);
    float theta0 = segment0 / float(RISING_SHOCKWAVE_SEGMENTS) * TWO_PI;
    float theta1 = segment1 / float(RISING_SHOCKWAVE_SEGMENTS) * TWO_PI;

    // 三角形顺序为：底0、顶0、底1、底1、顶0、顶1。
    if (corner == 0) {
        theta = theta0;
        heightT = 0.0;
        u = segment0 / float(RISING_SHOCKWAVE_SEGMENTS);
    } else if (corner == 1) {
        theta = theta0;
        heightT = 1.0;
        u = segment0 / float(RISING_SHOCKWAVE_SEGMENTS);
    } else if (corner == 2 || corner == 3) {
        theta = theta1;
        heightT = 0.0;
        u = segment1 / float(RISING_SHOCKWAVE_SEGMENTS);
    } else if (corner == 4) {
        theta = theta0;
        heightT = 1.0;
        u = segment0 / float(RISING_SHOCKWAVE_SEGMENTS);
    } else {
        theta = theta1;
        heightT = 1.0;
        u = segment1 / float(RISING_SHOCKWAVE_SEGMENTS);
    }
}

// 根据圆台半径坡度计算世界空间侧面法线，支持上窄下宽造成的向上倾斜法线。
vec3 resolveConeNormal(float theta, float bottomRadius, float topRadius, float height) {
    vec3 radial = vec3(cos(theta), 0.0, sin(theta));
    float slope = (bottomRadius - topRadius) / max(height, MIN_HEIGHT);
    return normalize(vec3(radial.x, slope, radial.z));
}

void main() {
    // gl_InstanceID 是当前 pipeline active list 内的位置，先映射回真实粒子下标。
    int activeBase = uRenderPipelineId * uMaxParticles;
    int particleIndex = int(activeIndices[activeBase + gl_InstanceID]);
    Particle p = particles[particleIndex];
    float life = p.position.w;
    float totalLife = max(p.velocity.w, 0.0001);
    int materialId = clamp(int(p.extra.w + 0.5), 0, MATERIAL_COUNT - 1);
    int particlePipelineId = int(materials[materialId].flags.x + 0.5);

    // active index 正常已经按 pipeline 过滤，这里保留异常数据兜底。
    if (life <= 0.0 || particlePipelineId != uRenderPipelineId) {
        gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
        vColor = vec4(0.0);
        vLocalUv = vec2(0.0);
        vUvTile = vec2(1.0);
        vWorldPos = vec3(0.0);
        vWorldNormal = vec3(0.0, 1.0, 0.0);
        vAge = 0.0;
        vAgeT = 0.0;
        vTotalLife = 1.0;
        vMaterialId = 0;
        vEffectPower = 1.0;
        vDissolvePower = 1.0;
        vUvFlowSpeed = 0.0;
        return;
    }

    float age = totalLife - life;
    float ageT = clamp(age / totalLife, 0.0, 1.0);
    vec4 color = resolveParticleColor(p, ageT);
    color.a *= smoothstep(0.0, 0.5, life / totalLife);

    // render.xy 表示底部直径和高度，顶部半径首版由 shader 常量比例控制。
    vec2 currentSize = max(resolveParticleSize(p, ageT), vec2(0.001));
    float bottomRadius = max(currentSize.x * 0.5, MIN_RADIUS);
    float topRadius = max(bottomRadius * TOP_RADIUS_RATIO, MIN_RADIUS);
    float height = max(currentSize.y, MIN_HEIGHT);

    float theta;
    float heightT;
    float u;
    resolveCylinderVertex(theta, heightT, u);

    float radius = mix(bottomRadius, topRadius, heightT);
    vec3 localPos = vec3(cos(theta) * radius, heightT * height, sin(theta) * radius);
    vec3 localNormal = resolveConeNormal(theta, bottomRadius, topRadius, height);
    vec3 worldPos = p.position.xyz + rotateAroundY(localPos, p.render.z);
    vec3 worldNormal = rotateAroundY(localNormal, p.render.z);
    vec4 viewPos = uView * vec4(worldPos, 1.0);

    gl_Position = uProjection * viewPos;
    vColor = color;
    vLocalUv = vec2(u, heightT);
    vUvTile = max(p.sizeControl.zw, vec2(0.001));
    vWorldPos = worldPos;
    vWorldNormal = normalize(worldNormal);
    vAge = age;
    vAgeT = ageT;
    vTotalLife = totalLife;
    vMaterialId = materialId;
    vEffectPower = max(p.renderParams.w, 0.001);
    vDissolvePower = max(p.motion.y, 0.001);
    vUvFlowSpeed = p.renderParams.z;
}
