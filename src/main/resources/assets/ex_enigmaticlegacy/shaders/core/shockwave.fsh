#version 330 core

// 冲击波片段 shader：
// 使用 AkatZumaTool 自定义图集中的 trail_2 sprite 还原 UE5 冲击波材质。
// 当前正式输出：按 VectorToRadialValue 极坐标 UV 采样 trail_2，并使用 G 通道控制黑底透明度。
// UE5 [5,2,0] 拆成角度方向倍率和半径方向倍率，分别控制纹理精度与圆环数量。
// 输出 location 0 写可见层，location 1 写 bloom 源，接入现有 MRT/Bloom 管线。
in vec2 vUV;
in vec4 vColor;
flat in float vTimeSpeedRandom;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 bloomColor;

// x=时间，y=bloom强度，z=预留旧径向倍率，w=自发光整体强度。
uniform vec4 EffectParams;
// xyz=UE5 材质反推得到的蓝色自发光基调，w=预留。
uniform vec4 TintParams;
// x=角度方向倍率，y=半径方向倍率，z=半径归一化倍率，w=角度偏移。
uniform vec4 RadialParams;
// x=UV横向偏移，y=时间速度，z=UV纵向偏移，w=预留。
uniform vec4 UvAnimParams;
// x=边缘淡出开始，y=边缘淡出结束，z=透明度倍率，w=预留。
uniform vec4 ShapeParams;
// x=径向出现起点，y=径向出现终点，z=起点柔化宽度，w=终点柔化宽度。
uniform vec4 RevealParams;
// 自定义图集中 trail_2 sprite 的 UV 范围。
uniform vec4 ShockwaveSpriteUV;
// 单图集采样器，trail_2 从这里采样。
uniform sampler2D Sampler0;

// 圆周率，用于把 atan 得到的 -PI..PI 角度归一化到 0..1。
const float PI = 3.14159265359;
// 径向采样中心，对齐 UE5 VectorToRadialValue 默认使用的 UV 中心。
const vec2 RADIAL_CENTER = vec2(0.5);

// 把局部 UV 循环映射到指定 sprite 区域，匹配 UE5 里 Time 滚动后的重复采样。
vec2 atlasUVRepeat(vec2 localUV, vec4 spriteUV) {
    vec2 uv = fract(localUV);
    return spriteUV.xy + uv * (spriteUV.zw - spriteUV.xy);
}

// 将二维 UV 转成极坐标 UV，对齐 UE5 VectorToRadialValue 的笛卡尔坐标到极坐标转换。
vec2 vectorToRadialValue(vec2 uv, vec2 center, float radiusNormalize) {
    // 以 billboard 中心为原点，得到当前像素相对冲击波中心的二维向量。
    vec2 centered = uv - center;
    // atan 返回 -PI..PI，归一化后作为径向纹理的 U 方向，也就是绕中心一圈的角度。
    float angle = atan(centered.y, centered.x);
    float angleN = angle / (2.0 * PI) + 0.5;
    // 距离中心的长度作为径向纹理的 V 方向，半径归一化倍率默认 2.0。
    float radiusN = length(centered) * radiusNormalize;
    return vec2(angleN, radiusN);
}

void main() {
    float time = EffectParams.x;
    float bloomStrength = max(EffectParams.y, 0.0);
    float intensity = max(EffectParams.w, 0.0);
    float radiusNormalize = max(RadialParams.z, 0.001);
    float edgeFadeStart = clamp(ShapeParams.x, 0.0, 1.0);
    float edgeFadeEnd = max(ShapeParams.y, edgeFadeStart + 0.001);
    float opacityScale = max(ShapeParams.z, 0.0);
    float visibleStart = clamp(RevealParams.x, 0.0, 1.0);
    float visibleEnd = clamp(RevealParams.y, visibleStart + 0.001, 1.0);
    float startSoftness = max(RevealParams.z, 0.001);
    float endSoftness = max(RevealParams.w, 0.001);
    float localTimeSpeed = UvAnimParams.y * mix(0.75, 1.25, clamp(vTimeSpeedRandom, 0.0, 1.0));

    // 局部 UV 固定来自 camera-facing quad，中心 0.5/0.5 对应冲击波中心。
    vec2 uv = clamp(vUV, 0.0, 1.0);
    // VectorToRadialValue 输出二维极坐标 UV：x=角度，y=半径，不能再降维成单个半径值。
    vec2 radialUV = vectorToRadialValue(uv, RADIAL_CENTER, radiusNormalize);
    // 角度偏移只旋转采样方向，用于后续对齐 UE 截图中的接缝和图案朝向。
    radialUV.x = fract(radialUV.x + RadialParams.w);
    // UE5 [5,2,0] 的 R/G 分开：x=纹理精度，y=圆环数量。
    vec2 radialMask = max(RadialParams.xy, vec2(0.001));
    // AppendVector(2, -0.5 * Time)：固定横向偏移和随时间滚动的纵向偏移。
    vec2 uvAnim = vec2(UvAnimParams.x, UvAnimParams.z + localTimeSpeed * time);
    vec2 shockUv = radialUV * radialMask + uvAnim;
    vec4 tex = texture(Sampler0, atlasUVRepeat(shockUv, ShockwaveSpriteUV));

    // 外缘柔化只作用于 billboard 最外圈，避免扩散到顶点边缘时出现明显方形硬边。
    float dist = length(uv - RADIAL_CENTER);
    float edgeMask = 1.0 - smoothstep(edgeFadeStart, edgeFadeEnd, dist);
    // 径向可见窗口控制图案从哪里出现、到哪里结束，避免内外两端突然硬切。
    float innerMask = smoothstep(visibleStart, visibleStart + startSoftness, dist);
    float outerMask = 1.0 - smoothstep(visibleEnd - endSoftness, visibleEnd, dist);
    float revealMask = innerMask * outerMask;
    // 纹理是黑色背景，使用手绘的 G 通道作为透明度，避免黑底被 additive blend 直接铺出来。
    float opacity = clamp(tex.g * vColor.a * opacityScale * edgeMask * revealMask, 0.0, 1.0);
    // 冲击波2材质为蓝色自发光基调加贴图 RGB，再由 opacity 压掉黑色背景区域。
    vec3 emissive = (TintParams.rgb + tex.rgb) * intensity;

    fragColor = vec4(emissive * opacity, opacity);
    bloomColor = vec4(emissive * bloomStrength * opacity, opacity);
}
