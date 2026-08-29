#version 430 core

struct ParticleMaterialGpu {
    vec4 baseSpriteUV;
    vec4 noiseSpriteUV0;
    vec4 noiseSpriteUV1;
    vec4 topDissolveSpriteUV;
    vec4 noiseParams;
    vec4 bloomParams;
    vec4 flags;
};

layout(std430, binding = 2) readonly buffer ParticleMaterialBuffer {
    ParticleMaterialGpu materials[];
};

in vec4 vColor;
in vec2 vLocalUv;
in vec2 vUvTile;
in vec3 vWorldPos;
in vec3 vWorldNormal;
in float vAge;
in float vAgeT;
in float vTotalLife;
flat in int vMaterialId;
flat in float vEffectPower;
flat in float vDissolvePower;
flat in float vUvFlowSpeed;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 bloomColor;

uniform sampler2D Sampler0;
uniform float uTime;
uniform vec3 uCameraPos;

const float OPACITY_CUTOFF = 0.003; // 最终透明度裁剪阈值，避免透明片元写入 MRT。
const float FRESNEL_POWER = 2.0; // 基础菲尼尔指数，发射器 power 继续作用在 1-Fresnel 上。
const float TEXTURE_COLOR_MIX = 0.85; // 纹理 RGB 只控制少量色相，主体颜色主要由发射器颜色决定。
const float TEXTURE_SHAPE_STRENGTH = 0.85; // 纹理 R 通道形状参与亮度的强度，避免降低颜色混合后丢失纹理结构。
const float TOP_FADE_START = 0.78; // 顶部淡化开始高度比例，减少圆台顶部硬切面。
const float TOP_FADE_END = 1.0; // 顶部淡化结束高度比例，通常保持为最顶端。
const float TOP_FADE_MIN_ALPHA = 0.08; // 顶部最低透明度保留值，避免顶部突然断层。
const float TOP_FADE_POWER = 1.45; // 顶部淡化曲线指数，越大越集中在最顶端。
const float TOP_COLOR_FADE_MIN = 0.85; // 顶部颜色最低亮度比例，让 CA0 和 CA1 一起柔和收尾。
const float FLOW_ACCEL_ENABLED = 1.0; // 是否启用生命周期后段流动加速，首版写死在 shader 内。
const float FLOW_ACCEL_START_T = 0.45; // 生命周期超过该比例后开始叠加额外流速。
const float FLOW_ACCEL_MULTIPLIER = 2.75; // 后段额外流速倍率，基于发射器基础速度叠加。

// 把重复 UV 映射到指定 atlas sprite 的 texel 中心范围，隔离线性过滤对边界外 texel 的采样。
vec2 atlasRepeatInset(vec2 localUV, vec4 spriteUV) {
    vec2 atlasSize = vec2(textureSize(Sampler0, 0));
    vec2 halfTexel = 0.5 / atlasSize;
    vec2 spriteMin = spriteUV.xy + halfTexel;
    vec2 spriteMax = spriteUV.zw - halfTexel;
    vec2 repeatedUv = fract(localUV);
    return mix(spriteMin, spriteMax, repeatedUv);
}

// 根据粒子自身 age 计算 UV 流动偏移，避免新出生粒子直接继承很大的全局时间偏移。
float resolveFlowOffset(float age, float ageT, float totalLife, float baseSpeed) {
    float baseOffset = age * baseSpeed;
    float accelT = smoothstep(FLOW_ACCEL_START_T, 1.0, ageT) * FLOW_ACCEL_ENABLED;
    float extraAge = max(age - totalLife * FLOW_ACCEL_START_T, 0.0);
    float accelOffset = extraAge * baseSpeed * (FLOW_ACCEL_MULTIPLIER - 1.0) * accelT;
    return baseOffset + accelOffset;
}

// 安全归一化水平向量，避免相机位于圆台轴心附近时产生 NaN。
vec3 safeNormalizeHorizontal(vec3 value, vec3 fallback) {
    float lenSq = dot(value, value);
    if (lenSq <= 0.00000001) {
        return fallback;
    }
    return value * inversesqrt(lenSq);
}

// 根据圆台高度比例淡化顶部，让程序化侧面不会在最顶端形成整齐切面。
float resolveTopFade(float heightT) {
    float fadeT = smoothstep(TOP_FADE_START, TOP_FADE_END, heightT);
    return mix(1.0, TOP_FADE_MIN_ALPHA, pow(fadeT, TOP_FADE_POWER));
}

// 顶部颜色跟随透明度轻微变暗，避免 alpha 柔化后仍残留过亮硬边。
float resolveTopColorFade(float heightT) {
    float fadeT = smoothstep(TOP_FADE_START, TOP_FADE_END, heightT);
    return mix(1.0, TOP_COLOR_FADE_MIN, fadeT);
}

void main() {
    ParticleMaterialGpu material = materials[vMaterialId];

    // 局部 U 绕圆柱一圈，局部 V 从底到顶；Y 方向减去偏移通常表现为纹理向上流动。
    float flowOffset = resolveFlowOffset(vAge, vAgeT, vTotalLife, vUvFlowSpeed);
    vec2 flowUv = vec2(vLocalUv.x * vUvTile.x, vLocalUv.y * vUvTile.y - flowOffset);
    vec4 tex = texture(Sampler0, atlasRepeatInset(flowUv, material.baseSpriteUV));
    vec4 dissolved = pow(tex, vec4(vDissolvePower));

    // 在世界水平 XZ 平面计算菲尼尔，让透明度主要沿圆周左右变化，不再随圆台高度上下移动。
    vec3 worldViewDir = uCameraPos - vWorldPos;
    vec3 horizontalViewDir = safeNormalizeHorizontal(vec3(worldViewDir.x, 0.0, worldViewDir.z), vec3(0.0, 0.0, 1.0));
    vec3 horizontalNormal = safeNormalizeHorizontal(vec3(vWorldNormal.x, 0.0, vWorldNormal.z), vec3(0.0, 0.0, 1.0));
    float ndv = abs(dot(horizontalNormal, horizontalViewDir));
    float fresnel = pow(1.0 - ndv, FRESNEL_POWER);
    float inverseFresnel = 1.0 - fresnel;
    float fresnelValue = pow(inverseFresnel, vEffectPower);

    // 纹理 RGBA 全程保持 vec4，颜色混合和形状遮罩分离，避免降低颜色比例后圆柱变成纯色片。
    vec4 shockwaveValue = dissolved * fresnelValue;
    float shapeMask = dissolved.r;
    float topFade = resolveTopFade(vLocalUv.y);
    float topColorFade = resolveTopColorFade(vLocalUv.y);
    vec3 textureTint = mix(vec3(1.0), shockwaveValue.rgb, TEXTURE_COLOR_MIX);
    float shapedLight = mix(1.0, shapeMask, TEXTURE_SHAPE_STRENGTH);
    vec3 baseColor = textureTint * vColor.rgb * shapedLight * topColorFade;
    float opacity = shapeMask * fresnelValue * topFade * vColor.a * material.bloomParams.w;

    if (opacity <= OPACITY_CUTOFF) {
        discard;
    }

    // 当前粒子系统使用预乘 Alpha 混合，可见颜色和 Alpha 都沿同一 RGBA 链路输出。
    fragColor = vec4(baseColor * opacity, opacity);

    // Bloom 使用同一个基础色，先去掉旧的 halo 标量加成以便和 UE 主链路对齐。
    float bloomAlpha = opacity * material.bloomParams.x;
    bloomColor = vec4(baseColor * bloomAlpha, bloomAlpha);
}
