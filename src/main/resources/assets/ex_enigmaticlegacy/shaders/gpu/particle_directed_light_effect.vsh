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
const int MOTION_CIRCULAR = 1; // 圆形运动模式，定向光柱会把圆心到当前位置的半径方向作为长轴。
const int MOTION_ARC_DIRECTION = 6; // 弧面方向模式，长光柱从世界 Y 轴同步旋向目标方向。

layout(location = 0) in vec2 aPos;

out vec4 vColor;
out vec2 vCoreTexCoord;
out vec2 vBloomTexCoord;
flat out vec3 vParticleSeed;
flat out int vMaterialId;
flat out int vMotionType;
flat out vec2 vLightEffectMaskParams;

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

// 按 Compute Shader 相同的 X/Y/Z 欧拉角顺序旋转轨道基底，保证渲染和运动共用同一圆弧平面。
vec3 rotateOrbitVector(vec3 v, vec3 angles) {
    float cp = cos(angles.x);
    float sp = sin(angles.x);
    v = vec3(v.x, v.y * cp - v.z * sp, v.y * sp + v.z * cp);

    float cy = cos(angles.y);
    float sy = sin(angles.y);
    v = vec3(v.x * cy + v.z * sy, v.y, -v.x * sy + v.z * cy);

    float cr = cos(angles.z);
    float sr = sin(angles.z);
    return vec3(v.x * cr - v.y * sr, v.x * sr + v.y * cr, v.z);
}

// 围绕任意单位轴旋转向量，用于把同轴补充光片绕光柱长轴转开角度。
vec3 rotateAroundAxis(vec3 value, vec3 axis, float radians) {
    vec3 safeAxis = length(axis) < 0.00001 ? vec3(0.0, 1.0, 0.0) : normalize(axis);
    float c = cos(radians);
    float s = sin(radians);
    return value * c + cross(safeAxis, value) * s + safeAxis * dot(safeAxis, value) * (1.0 - c);
}

// 在两个方向之间做球面插值，保证弧面光柱的旋转速度稳定且不会缩短长轴。
vec3 slerpVec3(vec3 fromDir, vec3 toDir, float t) {
    vec3 fromSafe = length(fromDir) < 0.00001 ? vec3(0.0, 1.0, 0.0) : normalize(fromDir);
    vec3 toSafe = length(toDir) < 0.00001 ? vec3(0.0, 0.0, 1.0) : normalize(toDir);
    float dotValue = clamp(dot(fromSafe, toSafe), -1.0, 1.0);

    // 两个方向接近平行时改用线性插值，避免 sin(theta) 过小导致数值放大。
    if (dotValue > 0.9995) {
        return normalize(mix(fromSafe, toSafe, clamp(t, 0.0, 1.0)));
    }

    // 两个方向接近反向时绕稳定侧轴旋转，避免中点向量变成零。
    if (dotValue < -0.9995) {
        vec3 helper = abs(fromSafe.y) < 0.95 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);
        vec3 axis = normalize(cross(fromSafe, helper));
        return rotateAroundAxis(fromSafe, axis, 3.14159265 * clamp(t, 0.0, 1.0));
    }

    float theta = acos(dotValue);
    float sinTheta = sin(theta);
    float fromWeight = sin((1.0 - t) * theta) / sinTheta;
    float toWeight = sin(t * theta) / sinTheta;
    return normalize(fromSafe * fromWeight + toSafe * toWeight);
}

void main() {
    // active index 已经由 compute 按 pipeline 压缩，当前实例先转成真实粒子下标。
    int activeBase = uRenderPipelineId * uMaxParticles;
    int particleIndex = int(activeIndices[activeBase + gl_InstanceID]);
    Particle p = particles[particleIndex];
    float life = p.position.w;
    float totalLife = max(p.velocity.w, 0.0001);
    int materialId = clamp(int(p.extra.w + 0.5), 0, MATERIAL_COUNT - 1);
    int particlePipelineId = int(materials[materialId].flags.x + 0.5);

    // 防御性过滤异常粒子，避免其它材质误进入定向光效批次。
    if (life <= 0.0 || particlePipelineId != uRenderPipelineId) {
        gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
        vColor = vec4(0.0);
        vCoreTexCoord = vec2(0.0);
        vBloomTexCoord = vec2(0.0);
        vParticleSeed = vec3(0.0);
        vMaterialId = 0;
        vMotionType = 0;
        vLightEffectMaskParams = vec2(0.20, 0.18);
        return;
    }

    int motionType = int(p.motion.x + 0.5);
    float ageT = clamp(1.0 - life / totalLife, 0.0, 1.0);
    vec4 color = resolveParticleColor(p, ageT);
    // 弧面光柱需要精确遵守 arc + hold + fade 时间轴，不再提前吃通用生命周期淡出。
    if (motionType != MOTION_ARC_DIRECTION) {
        color.a *= smoothstep(0.0, 0.5, life / totalLife);
    }

    // 出生、中段、结束三段尺寸仍由 CPU 控制，圆形光柱分支会把 render.z 改作同轴面旋转角。
    vec2 size = max(resolveParticleSize(p, ageT), vec2(0.001));
    vec3 worldUp = vec3(0.0, 1.0, 0.0);

    vec2 local;
    vec3 right;
    vec3 up;
    if (motionType == MOTION_CIRCULAR) {
        // MOTION_CIRCULAR 下 velocity.xyz 保存轨道欧拉角，不再保存光片法线。
        vec3 planeAngles = p.velocity.xyz;
        vec3 orbitNormal = rotateOrbitVector(worldUp, planeAngles);
        vec3 beamDir = p.position.xyz - p.origin.xyz;
        if (length(beamDir) < 0.00001) {
            beamDir = worldUp;
        } else {
            beamDir = normalize(beamDir);
        }

        // render.z 在圆形光柱中表示同轴面绕长轴的补充角，多个长粒子首尾相同但面朝向不同。
        vec3 planeNormal = rotateAroundAxis(orbitNormal, beamDir, p.render.z);
        right = cross(beamDir, planeNormal);
        if (length(right) < 0.00001) {
            right = normalize(cross(beamDir, vec3(1.0, 0.0, 0.0)));
        } else {
            right = normalize(right);
        }
        up = beamDir;
        local = aPos * size;
    } else if (motionType == MOTION_ARC_DIRECTION) {
        // MOTION_ARC_DIRECTION 下 velocity.xyz 保存最终目标方向，所有同源粒子按同一 age 同步旋转。
        float age = totalLife - life;
        float arcDuration = max(p.speedParams.x, 0.001);
        float holdDuration = max(p.speedParams.y, 0.0);
        float fadeDuration = max(p.speedParams.z, 0.001);
        float arcT = smoothstep(0.0, 1.0, clamp(age / arcDuration, 0.0, 1.0));
        vec3 targetDir = length(p.velocity.xyz) < 0.00001 ? vec3(0.0, 0.0, 1.0) : normalize(p.velocity.xyz);
        vec3 beamDir = slerpVec3(worldUp, targetDir, arcT);

        // 到达最终朝向后保持 targetDir；保留结束后按 fadeDuration 额外淡出。
        if (age >= arcDuration) {
            beamDir = targetDir;
        }
        float fadeT = clamp((age - arcDuration - holdDuration) / fadeDuration, 0.0, 1.0);
        color.a *= 1.0 - fadeT;

        // 先构造一个稳定侧面法线，再用 render.z 把第二片光柱绕长轴转 90 度补足侧视角。
        vec3 helper = abs(beamDir.y) < 0.95 ? worldUp : vec3(1.0, 0.0, 0.0);
        vec3 baseNormal = normalize(cross(beamDir, helper));
        vec3 planeNormal = rotateAroundAxis(baseNormal, beamDir, p.render.z);
        right = cross(beamDir, planeNormal);
        if (length(right) < 0.00001) {
            right = vec3(1.0, 0.0, 0.0);
        } else {
            right = normalize(right);
        }
        up = beamDir;

        // 弧面光柱使用带端帽余量的根部 Pivot，给玩家近端预留椭圆帽空间，避免几何底边切平。
        float capLength = max(size.x * 0.5, 0.001);
        float beamLength = max(size.y, 0.001);
        local = vec2(aPos.x * size.x, (aPos.y + 0.5) * (beamLength + capLength * 2.0) - capLength);
    } else {
        // 普通定向光效保持原语义：velocity.xyz 是世界空间平面法线，render.z 是面内固定旋转。
        float rotation = p.render.z;
        float c = cos(rotation);
        float s = sin(rotation);
        local = vec2(
            aPos.x * size.x * c - aPos.y * size.y * s,
            aPos.x * size.x * s + aPos.y * size.y * c
        );

        vec3 planeNormal = p.velocity.xyz;
        if (length(planeNormal) < 0.00001) {
            planeNormal = vec3(0.0, 0.0, 1.0);
        } else {
            planeNormal = normalize(planeNormal);
        }

        // 以世界 Y 轴为默认长边方向，结合平面法线构造稳定的竖直劈砍平面。
        right = cross(worldUp, planeNormal);
        if (length(right) < 0.00001) {
            right = vec3(1.0, 0.0, 0.0);
        } else {
            right = normalize(right);
        }
        up = normalize(cross(planeNormal, right));
    }
    vec3 worldPos = p.position.xyz + right * local.x + up * local.y;

    gl_Position = uProjection * uView * vec4(worldPos, 1.0);
    vColor = color;
    vCoreTexCoord = aPos + vec2(0.5);
    vBloomTexCoord = aPos + vec2(0.5);
    vParticleSeed = vec3(p.extra.y, p.extra.z, p.origin.w);
    vMaterialId = materialId;
    vMotionType = motionType;
    // 每个发射器可独立设置最终圆形遮罩，沿用 LIGHT_EFFECT 的发射器级遮罩参数。
    vLightEffectMaskParams = p.sizeControl.zw;
}
