#version 330 core
in vec2 vUV;
in vec4 vColor;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 bloomColor;

uniform sampler2D Sampler0;
uniform vec4 EffectParams;
uniform vec4 ColorParams;
uniform vec4 EdgeColor;
uniform vec4 Noise1SpriteUV;
uniform vec4 Noise2SpriteUV;
uniform vec4 Noise3SpriteUV;
uniform vec4 Noise1Params;
uniform vec4 Noise2Params;
uniform vec4 Noise3Params;
uniform vec4 Noise1Flow;
uniform vec4 Noise2Flow;
uniform vec4 Noise3Flow;
uniform vec4 MaskParams;
uniform vec2 HeightFade;
uniform vec2 NoiseCutoff;

// 把可平铺局部 UV 映射到自定义图集中的单个 sprite 区域，避免采到相邻贴图。
vec2 atlasRepeat(vec2 localUV, vec4 spriteUV) {
    vec2 uv = fract(localUV);
    return spriteUV.xy + uv * (spriteUV.zw - spriteUV.xy);
}

// 第三张月面噪声读取 RG 通道并中心化，用于扰动前两张噪声的采样位置。
vec2 sampleNoiseOffset(vec2 baseUV, float time) {
    vec2 noise3UV = baseUV * max(Noise3Params.xy, vec2(0.001)) + time * Noise3Flow.xy + Noise3Flow.zw;
    vec2 rg = texture(Sampler0, atlasRepeat(noise3UV, Noise3SpriteUV)).rg;
    return (rg * 2.0 - 1.0) * Noise3Params.z;
}

// ribbon 宽度方向圆形柔边 mask，并叠加高度方向淡入淡出。
float spiralMask(vec2 uv) {
    float widthCoord = abs(uv.x * 2.0 - 1.0) * max(MaskParams.x, 0.001);
    float widthMask = 1.0 - smoothstep(MaskParams.z, MaskParams.z + max(MaskParams.w, 0.0001), widthCoord);
    float bottomFade = smoothstep(0.0, max(HeightFade.x, 0.0001), uv.y);
    float topFade = 1.0 - smoothstep(1.0 - max(HeightFade.y, 0.0001), 1.0, uv.y);
    return widthMask * bottomFade * topFade;
}

void main() {
    // EffectParams: x=时间秒，y=Bloom 强度，z=自发光强度，w=可见层透明度。
    float time = EffectParams.x;
    float bloomStrength = max(EffectParams.y, 0.0);
    float intensity = max(EffectParams.z, 0.0);
    float opacityScale = max(EffectParams.w, 0.0);

    // 三噪声链路：Noise3.rg 先扰动 UV，Noise1.r 与 fx_noise015.r 再相乘生成能量遮罩。
    vec2 uvOffset = sampleNoiseOffset(vUV, time);
    vec2 noise1UV = vUV * max(Noise1Params.xy, vec2(0.001)) + time * Noise1Flow.xy + Noise1Flow.zw;
    vec2 noise2UV = vUV * max(Noise2Params.xy, vec2(0.001)) + time * Noise2Flow.xy + Noise2Flow.zw;
    float noise1 = texture(Sampler0, atlasRepeat(noise1UV + uvOffset, Noise1SpriteUV)).r;
    float noise2 = texture(Sampler0, atlasRepeat(noise2UV + uvOffset, Noise2SpriteUV)).r;
    float noiseMix = noise1 * noise2;
    float noiseMask = smoothstep(NoiseCutoff.x, max(NoiseCutoff.y, NoiseCutoff.x + 0.0001), noiseMix);

    // 最终遮罩由三噪声、圆形柔边、高度淡化和生命周期顶点 alpha 共同决定。
    float mask = spiralMask(clamp(vUV, 0.0, 1.0));
    float alpha = noiseMask * mask * vColor.a * opacityScale;
    if (alpha < 0.003) {
        discard;
    }

    // 颜色只使用金黄色 uniform，不从贴图取色。
    vec3 coreColor = ColorParams.rgb;
    vec3 highColor = EdgeColor.rgb;
    float colorPower = max(ColorParams.w, 0.0001);
    vec3 color = mix(coreColor, highColor, pow(noiseMask, colorPower));
    vec3 emissive = color * alpha * intensity;

    fragColor = vec4(emissive, alpha);
    bloomColor = vec4(emissive * bloomStrength, alpha);
}
