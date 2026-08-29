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
in vec2 vTexCoord;
in float vAgeT;
flat in int vMaterialId;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 bloomColor;

uniform sampler2D Sampler0;
uniform float uTime;

const float TWO_PI = 6.28318530718; // 极坐标角度归一化使用的完整圆周常量。
const float PARTICLE_PHASE_SCALE = 0.35; // 单粒子生命周期附加相位，错开重复法阵的纹理状态。
const float CIRCLE_MASK_RADIUS = 0.96; // 最终圆形遮罩半径，局部中心到四边中点为 1.0。
const float CIRCLE_MASK_SOFTNESS = 0.56; // 圆形边缘柔化宽度，同时作用于可见层和 Bloom 层。
const float OPACITY_CUTOFF = 0.0; // 最终透明度低于该值时丢弃片元，避免无效透明混合。
const float ANGLE_SEAM_BLEND_WIDTH = 0.04; // 角度 0/1 接缝两侧的双相位混合宽度。

// 将重复 UV 映射到指定 atlas sprite 的 texel 中心范围，隔离线性过滤对边界外 texel 的采样。
vec2 atlasRepeatInset(vec2 localUV, vec4 spriteUV) {
    vec2 atlasSize = vec2(textureSize(Sampler0, 0));
    vec2 halfTexel = 0.5 / atlasSize;

    // 两侧各内缩半个 atlas texel，使局部 0/1 边界落在 sprite 首尾 texel 的中心。
    vec2 spriteMin = spriteUV.xy + halfTexel;
    vec2 spriteMax = spriteUV.zw - halfTexel;
    vec2 repeatedUv = fract(localUV);
    return mix(spriteMin, spriteMax, repeatedUv);
}

// 将未平铺的极坐标 UV 分别应用到主纹理 X/Y 两个方向的平铺值。
vec2 buildMainTextureUv(vec2 baseUv, vec4 noiseParams, float angleShift) {
    // 先分别应用主纹理 X/Y 平铺，再在主纹理局部 U 上偏移半个纹理周期。
    vec2 tiledBaseUv = vec2(
            baseUv.x * noiseParams.x,
            baseUv.y * noiseParams.y
    );
    return tiledBaseUv + vec2(angleShift, 0.0);
}

// 在主纹理每个 U 周期的接缝附近混入错相采样，使亮度跨周期接缝连续。
float sampleCircularBaseSeamless(
        vec2 baseUv,
        vec4 spriteUv,
        vec4 noiseParams,
        bool flipUv
) {
    vec2 primaryUv = buildMainTextureUv(baseUv, noiseParams, 0.0);
    float tileLocalU = fract(primaryUv.x);

    // 冲击波法阵复刻 UE 中的 UV * -1，翻转主纹理的 U/V 两个采样方向。
    // 双相位采样必须同时翻转，避免接缝混合时使用不同方向的纹理。
    if (flipUv) {
        primaryUv = -primaryUv;
    }

    // 主采样保留现有图案，错相采样把自身接缝旋转到半圈外。
    float primaryR = texture(
            Sampler0,
            atlasRepeatInset(
                    primaryUv,
                    spriteUv
            )
    ).r;
    vec2 shiftedUv = buildMainTextureUv(baseUv, noiseParams, 0.5);
    if (flipUv) {
        shiftedUv = -shiftedUv;
    }

    float shiftedR = texture(
            Sampler0,
            atlasRepeatInset(
                    shiftedUv,
                    spriteUv
            )
    ).r;

    // 使用平铺后的局部 U 检测每一个重复周期的接缝，而不是只检测原始角度 0/1。
    float seamDistance = min(tileLocalU, 1.0 - tileLocalU);
    float primaryWeight = smoothstep(0.0, ANGLE_SEAM_BLEND_WIDTH, seamDistance);
    return mix(shiftedR, primaryR, primaryWeight);
}

// 为所有水平法阵构建统一的未平铺圆形采样 UV，平铺在主纹理采样前分别应用。
vec2 buildCircularSampleUv(vec2 localUv, vec4 noiseSpriteUv, vec4 noiseParams, float timePhase) {
    // 把面片 UV 中心化后计算标准极坐标，角度始终只沿圆周采样一圈。
    vec2 centered = localUv * 2.0 - 1.0;
    float radius = length(centered);
    float angle01 = radius > 0.00001
            ? fract(atan(centered.y, centered.x) / TWO_PI)
            : 0.0;

    // tex_pattern59 只使用未平铺的平面 UV 读取 R 通道，不使用主纹理平铺参数。
    vec2 noiseUv = centered * 0.5 + 0.5;
    noiseUv += vec2(timePhase * 0.03);
    float radialNoise = texture(
            Sampler0,
            atlasRepeatInset(noiseUv, noiseSpriteUv)
    ).r;

    // 直接使用 tex_pattern59.r 乘扰动强度，作为主纹理 V/径向方向的扰动量。
    float radialOffset = radialNoise * noiseParams.w;
    float radialPhase = radius;
    radialPhase -= timePhase * noiseParams.z;
    radialPhase += radialOffset;
    return vec2(angle01, radialPhase);
}

void main() {
    ParticleMaterialGpu material = materials[vMaterialId];

    // 两个法阵无条件使用同一个圆形采样方法，只通过材质平铺、速度和扰动参数区分效果。
    float timePhase = uTime + vAgeT * PARTICLE_PHASE_SCALE;
    vec2 circularUv = buildCircularSampleUv(
            vTexCoord,
            material.noiseSpriteUV0,
            material.noiseParams,
            timePhase
    );

    // 两个法阵统一使用独立 X/Y 平铺和双相位接缝混合，主纹理分别为 tex_pattern66 或 trail_2。
    float baseTextureR = sampleCircularBaseSeamless(
            circularUv,
            material.baseSpriteUV,
            material.noiseParams,
            vMaterialId == 3
    );
    float textureOpacity = clamp(baseTextureR, 0.0, 1.0);

    // 圆形遮罩仍使用未平铺的局部半径，避免材质平铺次数改变法阵的世界空间外轮廓。
    vec2 centered = vTexCoord * 2.0 - 1.0;
    float radius = length(centered);

    // 最终圆形遮罩与纹理 R 通道合并为唯一 opacity，CA0 和 CA1 都必须使用该结果。
    float circleMask = 1.0 - smoothstep(
        CIRCLE_MASK_RADIUS - CIRCLE_MASK_SOFTNESS,
        CIRCLE_MASK_RADIUS,
        radius
    );
    float finalOpacityMask = textureOpacity * circleMask;
    float opacity = finalOpacityMask * vColor.a * material.bloomParams.w;

    if (opacity <= OPACITY_CUTOFF) {
        discard;
    }

    // 当前粒子系统使用预乘 Alpha 混合，可见颜色和 Alpha 都严格使用最终 opacity。
    fragColor = vec4(vColor.rgb * opacity, opacity);

    // Bloom 强度可以由纹理亮部增强，但源能量必须乘同一个 opacity，透明区不直接写入 CA1。
    float bloomStrength = material.bloomParams.x + baseTextureR * material.bloomParams.y;
    float bloomAlpha = opacity * bloomStrength;
    bloomColor = vec4(vColor.rgb * bloomAlpha, bloomAlpha);
}
