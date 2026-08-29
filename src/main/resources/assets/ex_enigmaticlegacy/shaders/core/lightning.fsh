#version 330 core

// 闪电片段 shader：
// 使用 AkatZumaTool 自定义图集中的三个 sprite 完成 UE5 风格闪电材质。
// LightningSpriteUV 指向黑底白线主闪电，NoiseSpriteUV 和 NoiseSpriteUVAlt 指向两张噪声 sprite。
// Sampler0 只绑定一次自定义图集，主闪电和两张噪声都通过 atlas UV 在同一个 sampler 中采样。
// 主纹理只用 RGB 亮度生成形状遮罩，不参与最终颜色；噪声纹理 G 通道按 UE5 方案乘强度后扰动主纹理 UV。
// Java 侧用顶点 Color 表达每条闪电的可见 tint，用 BloomColor 打包每条闪电的 bloom tint、noiseIndex 和 noiseStrength。
// 输出 location 0 写可见层，location 1 写 bloom 源，继续接入现有后处理管线。
in vec2 vUV;
in vec4 vColor;
flat in ivec2 vBloomColor;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 bloomColor;

// x=时间，y=bloom强度，z=默认 UE5 噪声 UV 扰动系数，w=自发光整体强度。
uniform vec4 EffectParams;
// x=效果类型，y=bloom开关，zw=预留。
uniform ivec4 RenderFlags;
// xy=噪声 panner 速度，z=闪烁强度，w=预留。
uniform vec4 PannerParams;
// x=旧版条带 Bloom alpha 权重，y=旧版条带 Bloom 颜色权重，z=旧版核心 alpha 兜底，w=旧版核心颜色兜底。
uniform vec4 BloomParams;
// 自定义图集中 lightning_256x sprite 的 UV 范围，用于黑底白线主闪电形状采样。
uniform vec4 LightningSpriteUV;
// 自定义图集中第一张噪声 sprite 的 UV 范围，默认对应 noise_076_256x。
uniform vec4 NoiseSpriteUV;
// 自定义图集中第二张噪声 sprite 的 UV 范围，默认对应 noise_092_256x。
uniform vec4 NoiseSpriteUVAlt;
// 单图集采样器，主闪电和噪声 sprite 都从这里采样，避免绑定第二个 sampler。
uniform sampler2D Sampler0;

// 把 0 到 1 的局部 UV 映射到指定 sprite 区域，使用 clamp 防止边缘外采样串色。
vec2 atlasUVClamp(vec2 localUV, vec4 spriteUV) {
    vec2 uv = clamp(localUV, 0.0, 1.0);
    return spriteUV.xy + uv * (spriteUV.zw - spriteUV.xy);
}

// 主闪电 sprite 使用半 texel 内缩，避免线性过滤采到图集 padding 或相邻 texel。
vec2 atlasUVClampInset(vec2 localUV, vec4 spriteUV) {
    vec2 atlasTexel = 1.0 / vec2(textureSize(Sampler0, 0));
    vec2 minUv = spriteUV.xy + atlasTexel * 0.5;
    vec2 maxUv = spriteUV.zw - atlasTexel * 0.5;
    vec2 uv = clamp(localUV, 0.0, 1.0);
    return mix(minUv, maxUv, uv);
}

// 把局部 UV 映射到指定 sprite 区域，噪声用 fract 循环，保证 panner 可以持续滚动。
vec2 atlasUVRepeat(vec2 localUV, vec4 spriteUV) {
    vec2 uv = fract(localUV);
    return spriteUV.xy + uv * (spriteUV.zw - spriteUV.xy);
}

// 主闪电采样入口，集中封装图集 UV 映射，黑底主纹理只提供遮罩强度。
vec2 lightningAtlasUV(vec2 localUV) {
    return atlasUVClampInset(localUV, LightningSpriteUV);
}

// 噪声采样入口，调用方先选定 sprite，保持 repeat 行为以匹配 UE5 Panner 噪声扰动流程。
vec2 noiseAtlasUV(vec2 localUV, vec4 spriteUV) {
    return atlasUVRepeat(localUV, spriteUV);
}

// 解包 Java 侧写入 UV2 的 bloom 颜色和噪声参数，第四通道高位选图、低 7 位保存噪声强度。
vec4 unpackBloomColor(ivec2 packedColor) {
    int first = packedColor.x & 65535;
    int second = packedColor.y & 65535;
    return vec4(
        float(first & 255),
        float((first >> 8) & 255),
        float(second & 255),
        float((second >> 8) & 255)
    ) / 255.0;
}

// 轻量 hash 用于按 UV 分段制造高频闪烁，不引入额外噪声纹理。
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

void main() {
    float time = EffectParams.x;
    float bloomStrength = clamp(EffectParams.y, 0.0, 8.0);
    float defaultNoiseStrength = clamp(EffectParams.z, 0.0, 0.35);
    float intensity = clamp(EffectParams.w, 0.0, 12.0);
    float bloomEnabled = RenderFlags.y == 0 ? 0.0 : 1.0;
    float flickerStrength = clamp(PannerParams.z, 0.0, 1.0);

    // 路径闪电和圆环闪电都只传局部 UV；shader 内部统一映射到 atlas sprite。
    vec2 uv0 = clamp(vUV, 0.0, 1.0);

    // 先解包顶点传入的 bloom 颜色和噪声参数。
    // vBloomColor 使用 flat 插值，整条四边形内部不会在两张噪声图和两个强度之间渐变。
    vec4 bloomVertexColor = unpackBloomColor(vBloomColor);
    float noiseParamByte = floor(bloomVertexColor.a * 255.0 + 0.5);
    float noiseIndex = noiseParamByte >= 128.0 ? 1.0 : 0.0;
    float packedNoiseStrength = mod(noiseParamByte, 128.0) / 127.0 * 0.35;
    float noiseStrength = packedNoiseStrength > 0.0 ? clamp(packedNoiseStrength, 0.0, 0.35) : defaultNoiseStrength;
    vec4 selectedNoiseSpriteUV = noiseIndex > 0.5 ? NoiseSpriteUVAlt : NoiseSpriteUV;

    // UE5 材质中的 Panner：随时间滚动已选定的噪声 sprite，并只采样一次 G 通道作为扰动源。
    vec2 pannerUV = uv0 + PannerParams.xy * time;
    float noiseG = texture(Sampler0, noiseAtlasUV(pannerUV, selectedNoiseSpriteUV)).g;

    // UE5 参考公式的基础上保护宽度边缘：U 完整扰动，V 只在中心区域扰动。
    float noiseOffset = noiseG * noiseStrength;
    float edgeDistance = abs(uv0.y - 0.5) * 2.0;
    float centerWeight = 1.0 - smoothstep(0.45, 1.0, edgeDistance);
    vec2 uvDistort = uv0 + vec2(noiseOffset, noiseOffset * centerWeight);

    // 黑底白线主纹理只参与遮罩计算，RGB 亮度不直接进入最终颜色。
    vec4 mainSample = texture(Sampler0, lightningAtlasUV(uvDistort));
    float luminance = max(max(mainSample.r, mainSample.g), mainSample.b);
    float glowMask = smoothstep(0.02, 0.22, luminance);
    float wideGlowMask = smoothstep(0.005, 0.16, luminance);
    float coreMaskFromTexture = smoothstep(0.45, 0.85, luminance);
    float coreBloomMaskFromTexture = smoothstep(0.28, 0.75, luminance);

    // 按横向位置给核心加一点收束，纹理亮度仍是主要形状来源。
    float cross = abs(uv0.y - 0.5) * 2.0;
    float coreMask = mix(1.0, exp(-cross * cross * 3.0), 0.45);

    // 高频闪烁与顶点 alpha 共同控制最终透明度，闪烁强度由 PannerParams.z 调试。
    float n = hash(vec2(floor(uv0.x * 18.0), floor(time * 28.0)));
    float flicker = mix(1.0, 0.78 + 0.22 * sin(time * 55.0 + uv0.x * 43.0 + n * 6.2831853), flickerStrength);

    // Bloom 层不再依赖闪电贴图灰边，改由条带横向距离独立生成发光范围。
    // 贴图亮度只保留给中心核心和电弧细节，避免 FB1 只贴着主体线条。
    float bloomCross = abs(uv0.y - 0.5) * 2.0;
    float bloomCoreShape = exp(-bloomCross * bloomCross * 18.0);
    float bloomGlowShape = exp(-bloomCross * bloomCross * 4.35);//控制外辉范围；越大范围越窄，建议亮度调好后再动。
    float bloomOuterShape = exp(-bloomCross * bloomCross * 0.95);
    float textureCoreDetail = coreBloomMaskFromTexture;
    float textureLineDetail = max(glowMask, wideGlowMask * 0.65);
    float bloomCore = bloomCoreShape * textureCoreDetail;
    float bloomGlow = bloomGlowShape * 0.28;
    float bloomOuter = bloomOuterShape * 0.06;
    float bloomElectric = bloomGlowShape * textureLineDetail * (0.35 + 0.65 * flicker) * 0.88;//控制电弧细节进入 Bloom 的强度；太闪就降，缺细节可升到 0.25。
    float lightningBloomShape = bloomCore * 0.55 + bloomGlow + bloomOuter + bloomElectric;

    // 取消 shader 首尾渐变，PATH 闪电可以稳定显示到几何终点。
    float visibleAlpha = clamp(glowMask * coreMask * vColor.a * flicker, 0.0, 1.0);
    float bloomAlpha = clamp(lightningBloomShape * vColor.a, 0.0, 1.0);
    if (max(visibleAlpha, bloomAlpha * bloomEnabled) < 0.006) {
        discard;
    }

    // 主纹理只输出遮罩强度；可见层颜色来自顶点 Color，bloom 颜色来自 BloomColor。
    float visibleMask = max(coreMaskFromTexture, glowMask * 0.35) * 0.3;
    // Bloom 颜色分层：核心保留少量主体色，外辉和外圈主要吃顶点 BloomColor。
    vec3 bloomTint = vColor.rgb * bloomCore * 0.45
            + bloomVertexColor.rgb * (bloomGlow * 0.45 + bloomOuter * 0.35 + bloomElectric * 0.35);
    vec3 visible = vColor.rgb * visibleMask * intensity;
    vec3 bloom = bloomTint * bloomAlpha * intensity * bloomStrength;

    fragColor = vec4(visible * visibleAlpha, visibleAlpha);
    bloomColor = vec4(bloom * bloomEnabled, bloomAlpha * bloomEnabled);
}
