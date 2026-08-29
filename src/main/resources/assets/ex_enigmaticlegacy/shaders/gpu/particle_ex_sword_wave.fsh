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
flat in vec2 vNoiseSpeedSeed;
flat in int vMaterialId;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 bloomColor;

uniform sampler2D Sampler0;
uniform float uTime;

const vec2 NOISE_054_UV_SPEED = vec2(-0.5, 0.5); // noise_054 的中心流动速度。
const vec2 NOISE_054_UV_SPEED_RANDOM_RANGE = vec2(0.15, 0.15); // 每粒子 X/Y 独立速度偏移范围。
const float NOISE_054_DISTORT_STRENGTH = 0.1; // noise_054.r 乘入两张主体纹理 UV 的扰动强度。
const vec3 EX_WAVE1_HIGHLIGHT_SCALE = vec3(1.0, 1.4839, 3.5); // 把发射器核心色提升为咖喱棒螺旋高亮色。
const float EX_WAVE1_INTENSITY = 1.0; // ex_wave1 主体层颜色强度。
const float EX_WAVE2_INTENSITY = 1.0; // ex_wave2 主体层颜色强度。
const float OPACITY_CUTOFF = 0.003; // 最终透明度裁剪阈值。

// 将不重复的主体 UV 映射到 atlas sprite 内，并内缩半个 texel 隔离相邻图集内容。
vec2 atlasClampInset(vec2 localUv, vec4 spriteUv) {
    vec2 atlasSize = vec2(textureSize(Sampler0, 0));
    vec2 halfTexel = 0.5 / atlasSize;
    vec2 spriteMin = spriteUv.xy + halfTexel;
    vec2 spriteMax = spriteUv.zw - halfTexel;
    return mix(spriteMin, spriteMax, clamp(localUv, 0.0, 1.0));
}

// 将循环噪声 UV 映射到 atlas sprite 内，并内缩半个 texel 避免线性过滤串色。
vec2 atlasRepeatInset(vec2 localUv, vec4 spriteUv) {
    vec2 atlasSize = vec2(textureSize(Sampler0, 0));
    vec2 halfTexel = 0.5 / atlasSize;
    vec2 spriteMin = spriteUv.xy + halfTexel;
    vec2 spriteMax = spriteUv.zw - halfTexel;
    return mix(spriteMin, spriteMax, fract(localUv));
}

void main() {
    ParticleMaterialGpu material = materials[vMaterialId];
    vec4 ex_wave1 = material.baseSpriteUV;
    vec4 ex_wave2 = material.noiseSpriteUV0;
    vec4 noise_054 = material.noiseSpriteUV1;

    // 每个粒子使用出生时固定的两个随机值，生命周期内速度不跳变。
    vec2 speedRandom = vNoiseSpeedSeed * 2.0 - 1.0;
    vec2 particleNoiseSpeed = NOISE_054_UV_SPEED
            + speedRandom * NOISE_054_UV_SPEED_RANDOM_RANGE;
    vec2 noiseUv = vTexCoord + uTime * particleNoiseSpeed;
    float noise_054_r = texture(Sampler0, atlasRepeatInset(noiseUv, noise_054)).r;

    // 严格使用 noise_054.r * 0.1 同时扰动两张主体纹理，保持黄橙图案对齐。
    float uvDistort = noise_054_r * NOISE_054_DISTORT_STRENGTH;
    vec2 mainUv = vTexCoord + vec2(uvDistort);
    float ex_wave1_r = texture(Sampler0, atlasClampInset(mainUv, ex_wave1)).r;
    float ex_wave2_r = texture(Sampler0, atlasClampInset(mainUv, ex_wave2)).r;

    // 发射器颜色作为核心色，ex_wave1 按螺旋 CORE 到 EDGE 的比例生成高亮层。
    vec3 waveCoreColor = vColor.rgb;
    vec3 waveHighlightColor = min(waveCoreColor * EX_WAVE1_HIGHLIGHT_SCALE, vec3(1.0));
    vec3 layeredColor = waveHighlightColor * ex_wave1_r * EX_WAVE1_INTENSITY
            + waveCoreColor * ex_wave2_r * EX_WAVE2_INTENSITY;
    float layerWeight = max(ex_wave1_r + ex_wave2_r, 0.0001);
    vec3 finalColor = layeredColor / layerWeight;

    // 透明度只使用两张主体纹理 R 通道、粒子 Alpha 和材质 Alpha。
    float textureOpacity = clamp(ex_wave1_r + ex_wave2_r, 0.0, 1.0);
    float opacity = textureOpacity * vColor.a * material.bloomParams.w;
    if (opacity <= OPACITY_CUTOFF) {
        discard;
    }

    // CA0 与 CA1 共用最终 opacity，透明区域不会单独写入 Bloom source。
    fragColor = vec4(finalColor * opacity, opacity);
    float bloomStrength = material.bloomParams.x
            + max(ex_wave1_r, ex_wave2_r) * material.bloomParams.y;
    float bloomAlpha = opacity * bloomStrength;
    bloomColor = vec4(finalColor * bloomAlpha, bloomAlpha);
}
