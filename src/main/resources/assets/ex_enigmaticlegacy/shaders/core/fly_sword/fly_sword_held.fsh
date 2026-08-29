#version 330 core

// 手持飞剑透明模型片元 shader。
// Sampler0 绑定 AkatZumaTool 自定义图集，采样三张飞剑噪声；Sampler1 采样场景颜色折射。
// 输出 location 0 写 mainFBO.CA0 可见颜色，location 1 写 mainFBO.CA1 bloom source。
in vec2 vLocalUV;
in float vModelAlpha;
in vec3 vViewPosition;
in vec3 vViewNormal;
in float vSwordGradient;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 bloomColor;

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform vec2 EffectParams;
uniform vec4 Noise1SpriteUV;
uniform vec4 Noise2SpriteUV;
uniform vec4 Noise3SpriteUV;
uniform vec3 FresnelParams;
uniform vec3 GradientStartColor;
uniform vec3 GradientEndColor;
uniform float EmissiveStrength;
uniform vec2 ScreenSize;
uniform vec4 Noise1FlowParams;
uniform vec4 Noise2FlowParams;

// 渐变亮度与折射像素范围直接保存在 shader 中，避免增加只用于固定参数的 Uniform。
const float GRADIENT_START_BRIGHTNESS = 1.0;
const float GRADIENT_END_BRIGHTNESS = 2.35;
const float REFRACTION_MIN_PIXELS = 1.5;
const float REFRACTION_MAX_PIXELS = 6.0;

// 把可平铺的局部 UV 限制并映射到自定义图集中的单个 sprite，避免越界采到相邻纹理。
vec2 atlasRepeat(vec2 localUV, vec4 spriteUV) {
    return spriteUV.xy + fract(localUV) * (spriteUV.zw - spriteUV.xy);
}

// 第三张月面噪声仅沿 Y 轴流动，RG 分量分别扰动后续噪声采样的 U、V 坐标。
vec2 flowingNoiseOffset(vec2 baseUV, float time) {
    vec2 noise3UV = baseUV + vec2(0.0, time * 0.5);
    return texture(Sampler0, atlasRepeat(noise3UV, Noise3SpriteUV)).rg * 0.5;
}

void main() {
    // EffectParams: x=时间秒，y=当前普通/真飞剑的 Bloom 强度。
    float time = EffectParams.x;
    float bloomStrength = max(EffectParams.y, 0.0);

    // 飞剑透明本体不使用原始纹理颜色或 alpha，直接由噪声、菲尼尔和折射决定输出。
    float emissiveStrength = max(EmissiveStrength, 0.0);

    // 两张噪声保持原有平铺倍率，但使用当前物品栈固定的独立速度和起始相位流动。
    vec2 noise1UV = vLocalUV * vec2(1.0, 2.0) + time * Noise1FlowParams.xy + Noise1FlowParams.zw;
    vec2 noise2UV = vLocalUV * vec2(2.0, 3.0) + time * Noise2FlowParams.xy + Noise2FlowParams.zw;
    // 第三张噪声的 RG 偏移同时加入两张既有流动噪声，再保留它们各自的速度和相位。
    vec2 modelNoiseOffset = flowingNoiseOffset(vLocalUV, time);
    float noise1 = texture(Sampler0, atlasRepeat(noise1UV + modelNoiseOffset, Noise1SpriteUV)).r;
    float noise2 = texture(Sampler0, atlasRepeat(noise2UV + modelNoiseOffset, Noise2SpriteUV)).r;
    float flowNoise = noise1 * noise2;

    // 使用观察空间法线和视线方向计算菲尼尔，背面处理由 RenderType 当前的剔除状态决定。
    vec3 viewNormal = normalize(vViewNormal);
    vec3 viewDirection = normalize(-vViewPosition);
    float fresnelPower = max(FresnelParams.x, 0.0001);
    float edgeStart = clamp(FresnelParams.y, 0.0, 1.0);
    float edgeEnd = max(FresnelParams.z, edgeStart + 0.0001);
    float facing = clamp(dot(viewNormal, viewDirection), 0.0, 1.0);
    float fresnelRaw = pow(clamp(1.0 - facing, 0.0, 1.0), fresnelPower);
    // 平滑阈值把逐面菲尼尔收窄为外轮廓边缘带，剑身中部不再保留最低透明度。
    float edgeMask = smoothstep(edgeStart, edgeEnd, fresnelRaw);
    if (edgeMask < 0.004) {
        discard;
    }

    // 顶点 shader 沿模型局部 Y 轴生成连续比例，片元插值后不会在原剑身/剑柄边界跳色。
    float gradientAmount = clamp(vSwordGradient, 0.0, 1.0);
    vec3 gradientColor = mix(GradientStartColor, GradientEndColor, gradientAmount);
    float gradientStrength = mix(GRADIENT_START_BRIGHTNESS, GRADIENT_END_BRIGHTNESS, gradientAmount);
    // 两张噪声先相乘，再乘边缘菲尼尔、长度渐变颜色和亮度，结果主要写入 CA1 Bloom source。
    float bloomFresnel = fresnelRaw;
    float energyMask = flowNoise * bloomFresnel;
    vec3 emissive = energyMask * gradientColor * emissiveStrength * gradientStrength;
    vec3 bloomSource = emissive * bloomStrength;
    // 第二张噪声在 1.5 到 6.0 像素间改变采样位置，增强透明边缘中的背景错位。
    float refractionStrength = mix(REFRACTION_MIN_PIXELS, REFRACTION_MAX_PIXELS, noise2);
    vec2 normalXY = viewNormal.xy;
    float normalXYLength = length(normalXY);
    vec2 refractionDirection = normalXYLength > 0.0001 ? normalXY / normalXYLength : vec2(0.0);
    vec2 safeScreenSize = max(ScreenSize, vec2(1.0));
    vec2 screenUV = gl_FragCoord.xy / safeScreenSize;
    vec2 refractionOffset = refractionDirection * refractionStrength / safeScreenSize;
    vec3 refractedScene = texture(Sampler1, clamp(screenUV + refractionOffset, vec2(0.001), vec2(0.999))).rgb;

    // 顶点 alpha 控制无原始贴图遮罩模型的可见透明度，菲尼尔继续收束透明边缘。
    float modelAlpha = clamp(vModelAlpha, 0.0, 1.0);
    // CA0 以 Bloom source 使用的菲尼尔法线量调制折射场景，保留边缘的法线明暗层次。
    float normalRefractionLight = mix(0.72, 1.28, bloomFresnel);
    fragColor = vec4(refractedScene * normalRefractionLight, edgeMask * modelAlpha);
    // CA1 只写双噪声、菲尼尔与长度渐变共同生成的自发光，不把场景背景写入 Bloom source。
    bloomColor = vec4(bloomSource, 1.0);
}
