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
in vec2 vCoreTexCoord;
in vec2 vBloomTexCoord;
flat in vec3 vParticleSeed;
flat in int vMaterialId;
flat in int vMotionType;
flat in vec2 vLightEffectMaskParams;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 bloomColor;

uniform sampler2D Sampler0;
uniform float uTime;

const vec2 LIGHT_EFFECT_NOISE1_TILE = vec2(0.5, 0.5); // Noise1 的 X/Y 平铺倍率，只影响定向 LIGHT_EFFECT shader。
const vec2 LIGHT_EFFECT_NOISE2_TILE = vec2(0.5, 0.5); // Noise2 的 X/Y 平铺倍率，只影响定向 LIGHT_EFFECT shader。
const float TOP_DISSOLVE_START_Y = 0.28; // 顶部消散开始高度，低于该高度基本保持完整圆形遮罩。
const float TOP_DISSOLVE_FULL_Y = 0.62; // 顶部消散完全生效高度，越低顶部破碎范围越大。
const vec2 TOP_DISSOLVE_NOISE_TILE = vec2(1.0, 1.0); // noise_092_128x 顶部消散噪声平铺倍率。
const float TOP_DISSOLVE_SCROLL_SPEED = 0.5; // 顶部消散噪声向上流动速度。
const float TOP_DISSOLVE_CUTOFF_LOW = 0.94; // 顶部消散噪声低阈值。
const float TOP_DISSOLVE_CUTOFF_HIGH = 1.0; // 顶部消散噪声高阈值。
const float TOP_DISSOLVE_STRENGTH = 1.0; // 顶部最大溶解强度。
const int MOTION_ARC_DIRECTION = 6; // 弧面方向光柱使用长轴胶囊遮罩。
const float DIRECTED_LIGHT_COLUMN_CAP_SCALE = 3.0; // 弧面光柱端帽沿长度方向的椭圆拉伸倍率。

// 把平铺 UV 限制到单个 atlas sprite 内，避免采样到相邻贴图。
vec2 atlasRepeat(vec2 localUV, vec4 spriteUV) {
    vec2 uv = fract(localUV);
    return spriteUV.xy + uv * (spriteUV.zw - spriteUV.xy);
}
// 简单随机函数用于给每个粒子错开三噪声相位。
float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

// 统一裁剪定向 LIGHT_EFFECT 的最终可见区域和 Bloom source，避免三噪声能量露出方形边。
float circleMask(vec2 uv, float radius, float softness) {
    float distanceToCenter = length(uv - vec2(0.5));
    return 1.0 - smoothstep(radius, radius + softness, distanceToCenter);
}

// 弧面长光柱专用胶囊遮罩：中段保持长条，首尾用沿长轴拉伸的椭圆端帽收口。
float capsuleMask(vec2 uv, float radius, float softness, float capScale) {
    vec2 centered = uv - vec2(0.5);
    float xDistance = abs(centered.x);
    float yDistance = abs(centered.y);
    float safeCapScale = max(capScale, 0.001);
    float halfBody = max(0.0, 0.5 - radius * safeCapScale);
    float capY = max(0.0, yDistance - halfBody);
    float capsuleDistance = length(vec2(xDistance, capY / safeCapScale));
    return 1.0 - smoothstep(radius, radius + softness, capsuleDistance);
}

// 第三张噪声使用 RG 通道扰动前两张噪声 UV，复用金色螺旋光效的三噪声链路。
vec2 sampleNoiseOffset(ParticleMaterialGpu material, vec2 uv, float phase) {
    vec2 tile = max(material.noiseParams.xy * 0.55, vec2(0.001));
    vec2 noiseUv = uv * tile + vec2(uTime * material.noiseParams.z * 0.23 + phase, -uTime * material.noiseParams.z * 0.41);
    vec2 rg = texture(Sampler0, atlasRepeat(noiseUv, material.noiseSpriteUV1)).rg;
    return (rg * 2.0 - 1.0) * material.noiseParams.w * 0.18;
}

// 按局部 UV 高度计算顶部消散影响区域，让底部和中部仍保持稳定光效主体。
float topDissolveRegion(vec2 uv) {
    return smoothstep(TOP_DISSOLVE_START_Y, TOP_DISSOLVE_FULL_Y, uv.y);
}

// 使用 noise_092_128x 采样顶部消散噪声，缓慢上移避免顶部边缘完全静止。
float sampleTopDissolveNoise(ParticleMaterialGpu material, vec2 uv, float phase) {
    vec2 dissolveUv = uv * TOP_DISSOLVE_NOISE_TILE;
    dissolveUv += vec2(phase * 3.7, phase * 5.1 + uTime * TOP_DISSOLVE_SCROLL_SPEED);
    return texture(Sampler0, atlasRepeat(dissolveUv, material.topDissolveSpriteUV)).r;
}

// 将顶部高度权重和 noise_092_128x 噪声混合到最终圆形遮罩，保证消散只主要影响顶部。
float topDissolveMask(ParticleMaterialGpu material, vec2 uv, float phase) {
    float topRegion = topDissolveRegion(uv);
    float dissolveNoise = sampleTopDissolveNoise(material, uv, phase);
    float noiseMask = smoothstep(TOP_DISSOLVE_CUTOFF_LOW, TOP_DISSOLVE_CUTOFF_HIGH, dissolveNoise);
    float dissolveAmount = topRegion * TOP_DISSOLVE_STRENGTH;
    return mix(1.0, noiseMask, dissolveAmount);
}

void main() {
    ParticleMaterialGpu material = materials[vMaterialId];
    float phase = rand(vParticleSeed.xy + vec2(vParticleSeed.z, 9.37));
    vec2 uvOffset = sampleNoiseOffset(material, vBloomTexCoord, phase);

    // Noise1 使用 t_fx_tile_0012，Noise2 使用 fx_noise015，二者相乘形成光效能量遮罩。
    vec2 baseTile = max(material.noiseParams.xy, vec2(0.001));
    vec2 noise1Tile = baseTile * LIGHT_EFFECT_NOISE1_TILE;
    vec2 noise2Tile = baseTile * 0.77 * LIGHT_EFFECT_NOISE2_TILE;
    vec2 noise1Uv = vBloomTexCoord * noise1Tile + uvOffset + vec2(uTime * material.noiseParams.z + phase, -uTime * material.noiseParams.z * 0.32);
    vec2 noise2Uv = vBloomTexCoord * noise2Tile + uvOffset - vec2(uTime * material.noiseParams.z * 0.29, uTime * material.noiseParams.z * 0.55 + phase);
    float noise1 = texture(Sampler0, atlasRepeat(noise1Uv, material.baseSpriteUV)).r;
    float noise2 = texture(Sampler0, atlasRepeat(noise2Uv, material.noiseSpriteUV0)).r;

    // 遮罩半径和柔边来自当前粒子的发射器参数，最小值保护 smoothstep 的有效区间。
    float maskRadius = max(vLightEffectMaskParams.x, 0.001);
    float maskSoftness = max(vLightEffectMaskParams.y, 0.001);
    float roundMask = vMotionType == MOTION_ARC_DIRECTION
        ? capsuleMask(vBloomTexCoord, maskRadius, maskSoftness, DIRECTED_LIGHT_COLUMN_CAP_SCALE)
        : circleMask(vBloomTexCoord, maskRadius, maskSoftness);
    float topMask = topDissolveMask(material, vBloomTexCoord, phase);
    float finalMask = roundMask * topMask;
    float energy = smoothstep(0.10, 0.62, noise1 * noise2);
    energy *= finalMask;

    // 核心遮罩使用固定粒子 UV，CA0 可见层保持粒子尺寸。
    float coreDistance = length(vCoreTexCoord - vec2(0.5));
    float coreMask = 1.0 - smoothstep(0.42, 0.5, coreDistance);
    float coreAlpha = coreMask * energy * vColor.a * material.bloomParams.w;

    // 光晕遮罩使用固定 bloom UV，范围主要由材质 softness 和后处理 blur 控制。
    float haloDistance = length(vBloomTexCoord - vec2(0.5));
    float haloSoftness = max(material.bloomParams.z, 0.001);
    float haloMask = 1.0 - smoothstep(0.28, 0.5 + haloSoftness, haloDistance);
    float haloAlpha = haloMask * energy * vColor.a * material.bloomParams.y * material.bloomParams.w;

    if (coreAlpha <= 0.003 && haloAlpha <= 0.003) {
        discard;
    }

    vec3 visible = vColor.rgb * coreAlpha * mix(0.85, 1.45, energy);
    float bloomAlpha = max(coreAlpha * material.bloomParams.x, haloAlpha);
    fragColor = vec4(visible, coreAlpha);
    bloomColor = vec4(vColor.rgb * bloomAlpha, bloomAlpha);
}
