#version 330 core

// 剑气实例化片段 shader：单个普通实例同时输出可见色和 bloom。
in vec2 texCoord;
// Java 侧写入的 per-entity 参数：x=渐变选择，y=划出进度，z=bloom 强度，w=透明度。
in vec4 vertexColor;

// CA0 输出可见颜色，CA1 输出 bloom source。
layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 bloomColor;

// 全局参数：x=累计动画时间，y=bloom倍率，z/w=保留。
uniform vec4 GlobalParams;
// sword1 sprite 在 EXETextureAtlas 中的 atlas UV 范围，用于把 atlas UV 还原成局部 UV。
uniform vec4 SwordSpriteUV;
// multi_gradient sprite 在 EXETextureAtlas 中的 atlas UV 范围，用于给剑气叠加渐变颜色。
uniform vec4 GradientSpriteUV;
// blue_gradient sprite 在 EXETextureAtlas 中的 atlas UV 范围，用于随机蓝白渐变。
uniform vec4 BlueGradientSpriteUV;
// EXETextureAtlas 图集采样器，由实例化 renderer 绑定到 0 号纹理单元。
uniform sampler2D Sampler0;

// 把 0~1 局部 UV 映射到目标 sprite 的图集 UV。
vec2 localToAtlasUV(vec2 localUV, vec4 spriteUV) {
    return spriteUV.xy + clamp(localUV, 0.0, 1.0) * (spriteUV.zw - spriteUV.xy);
}

void main() {
    float time = GlobalParams.x;
    float bloomScale = clamp(GlobalParams.y, 0.0, 4.0);
    float gradientSelect = step(0.5, vertexColor.r);
    float revealProgress = clamp(vertexColor.g, 0.0, 1.0);
    float bloomStrength = clamp(vertexColor.b, 0.0, 4.0);

    // texCoord 已经是 sword1 的局部 UV，先映射到图集再采样形状贴图。
    vec2 localUV = clamp(texCoord, 0.0, 1.0);
    vec2 swordAtlasUV = localToAtlasUV(localUV, SwordSpriteUV);

    // sword1 只负责形状遮罩和明暗细节，不再直接决定最终颜色。
    vec4 shapeColor = texture(Sampler0, swordAtlasUV);
    float luminance = max(max(shapeColor.r, shapeColor.g), shapeColor.b);
    float contentAlpha = smoothstep(0.01, 0.12, max(shapeColor.a, luminance));
    float pulse = 0.96 + sin(time * 5.2 + localUV.x * 8.0) * 0.04;

    // 从自身左侧到右侧划出的显现遮罩，前沿保留一条亮边。
    float revealHead = mix(-0.08, 1.08, revealProgress);
    float revealMask = 1.0 - smoothstep(revealHead, revealHead + 0.08, localUV.x);
    float revealFront = (1.0 - smoothstep(0.0, 0.08, abs(localUV.x - revealHead))) * (1.0 - smoothstep(0.92, 1.0, revealProgress));
    float alpha = clamp(shapeColor.a * vertexColor.a * contentAlpha * revealMask, 0.0, 1.0);

    if (alpha <= 0.003) {
        discard;
    }

    // 渐变颜色沿剑气长度方向流动，并按实体 seed 在 multi_gradient 与 blue_gradient 间稳定随机。
    float flow = fract(localUV.x * 0.85 + localUV.y * 0.18 - time * 0.42);
    vec3 multiGradientColor = texture(Sampler0, localToAtlasUV(vec2(flow, 0.5), GradientSpriteUV)).rgb;
    vec3 blueGradientColor = texture(Sampler0, localToAtlasUV(vec2(flow, 0.5), BlueGradientSpriteUV)).rgb;
    vec3 gradientColor = mix(multiGradientColor, blueGradientColor, gradientSelect);

    // 用 sword1 的灰度保留原贴图笔触层次，纹理本身不再做动态扰动。
    float detail = mix(0.46, 1.18, luminance);
    vec3 rimBlue = vec3(0.35, 0.78, 1.0) * revealFront * 0.85;
    vec3 baseColor = gradientColor * detail;
    vec3 visible = clamp((baseColor + rimBlue) * pulse, 0.0, 1.0);
    vec3 bloom = (visible + rimBlue) * (0.58 + contentAlpha * 1.25 + revealFront * 1.25) * bloomScale * bloomStrength;

    // CA0 预乘 alpha，便于最终合成。
    fragColor = vec4(visible * alpha, alpha);
    // CA1 单独作为 bloom 源，不再额外绘制 glow 实例或放大模型。
    bloomColor = vec4(bloom * alpha, alpha);
}
