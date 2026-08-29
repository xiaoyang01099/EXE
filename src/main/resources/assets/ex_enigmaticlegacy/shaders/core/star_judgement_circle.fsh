#version 330 core

// 星辰裁决法阵片段 shader。
// 目标是用一个 shader 同时完成多层高度法阵、中心八芒星、外围展开、旋转圆环、刻度、伪符文、侧面薄光和最终裁决闪光。
in vec2 vUV;
in vec4 vColor;

// CA0 输出可见颜色，CA1 输出 bloom source。
layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 bloomColor;

// EffectParams.x = 客户端时间秒。
// EffectParams.y = 实体生命周期进度。
// EffectParams.z = 中心法阵展开进度。
// EffectParams.w = 外围法阵展开进度。
uniform vec4 EffectParams;

// StrikeParams.x = 最终裁决闪光进度。
// StrikeParams.y = CPU 侧法阵半径。
// StrikeParams.z = bloom 强度。
// StrikeParams.w = 保留。
uniform vec4 StrikeParams;

const float PI = 3.14159265359;
const float LINE_WIDTH_SCALE = 0.20; // 法阵圆环、八芒星、符文带的整体线宽倍率，越小线越细。
const float TICK_WIDTH_SCALE = 0.12; // 法阵刻度和星尖扫光的角向宽度倍率，越小刻度越细。

// 哈希用于生成稳定的伪符文跳动，不依赖纹理。
float hash(float n) {
    return fract(sin(n) * 43758.5453123);
}

// 平滑圆环，center 是半径位置，width 是线宽。
float ring(float radius, float center, float width) {
    float scaledWidth = width * LINE_WIDTH_SCALE;
    float d = abs(radius - center);
    return 1.0 - smoothstep(scaledWidth, scaledWidth + 0.009, d);
}

// 角向刻度，把圆周切成 count 段，只保留每段中心附近的短线。
float ticks(float angle, float count, float width) {
    float scaledWidth = width * TICK_WIDTH_SCALE;
    float cell = fract(angle / (PI * 2.0) * count);
    float d = abs(cell - 0.5);
    return 1.0 - smoothstep(scaledWidth, scaledWidth + 0.012, d);
}

// 正多边形边线距离，保留给外层几何骨架微光使用。
float polygonLine(vec2 p, float n, float radius, float width, float rotation) {
    float angle = atan(p.y, p.x) + rotation;
    float sector = PI * 2.0 / n;
    float projected = cos(floor(0.5 + angle / sector) * sector - angle) * length(p);
    return ring(projected, radius, width);
}

// 八芒星轮廓，利用极坐标在内外半径之间交替生成星尖。
float eightPointStar(vec2 p, float innerRadius, float outerRadius, float width, float rotation) {
    float angle = atan(p.y, p.x) + rotation;
    float cell = fract(angle / (PI * 2.0) * 8.0);
    float spike = abs(cell - 0.5) * 2.0;
    float boundary = mix(outerRadius, innerRadius, smoothstep(0.0, 1.0, spike));
    return ring(length(p), boundary, width);
}

// 八个星尖的扫光，用于最终裁决前让中心图案有星辰仪式感。
float starTipSweep(float radius, float angle, float time, float progress) {
    float ray = ticks(angle + time * 0.18, 8.0, 0.048);
    float radialBand = smoothstep(0.18, 0.30, radius) * (1.0 - smoothstep(0.64, 0.80, radius));
    float sweep = 0.72 + 0.28 * sin(time * 10.0 + angle * 8.0);
    return ray * radialBand * sweep * progress;
}

// 伪符文：由角度分段、半径带和随机阈值组成，看起来像沿环排列的小字块。
float runeMarks(float radius, float angle, float time, float reveal) {
    float band = ring(radius, 0.68, 0.018) + ring(radius, 0.82, 0.015);
    float cell = floor(angle / (PI * 2.0) * 44.0);
    float randomGate = step(0.38, hash(cell * 17.13));
    float blink = 0.78 + 0.22 * sin(time * 6.2 + cell * 0.73);
    float segment = ticks(angle + sin(cell) * 0.12, 44.0, 0.13);
    return band * randomGate * blink * segment * reveal;
}

// 中心向外展开时，半径越靠外越晚出现。
float radialReveal(float radius, float progress, float softness) {
    return 1.0 - smoothstep(progress, progress + softness, radius);
}

// 顶点颜色的 r 通道存放层编号，这里把层编号转成柔和遮罩。
float layerMask(float layer, float target) {
    return 1.0 - smoothstep(0.08, 0.16, abs(layer - target));
}

void main() {
    float time = EffectParams.x;
    float ageProgress = clamp(EffectParams.y, 0.0, 1.0);
    float centerProgress = clamp(EffectParams.z, 0.0, 1.0);
    float outerProgress = clamp(EffectParams.w, 0.0, 1.0);
    float strikeProgress = clamp(StrikeParams.x, 0.0, 1.0);
    float bloomStrength = StrikeParams.z;

    // UV 转成 -1 到 1 的局部法阵坐标。
    vec2 p = vUV * 2.0 - 1.0;
    float radius = length(p);
    if (radius > 1.05) {
        discard;
    }

    float angle = atan(p.y, p.x);
    float layer = vColor.r;

    // layer 约定：0=主环层，0.25=核心八芒星层，0.5=外符文层，0.85=侧面辉光层。
    float mainLayer = layerMask(layer, 0.0);
    float coreLayer = layerMask(layer, 0.25);
    float outerLayer = layerMask(layer, 0.5);
    float sideLayer = layerMask(layer, 0.85);

    // 侧面薄光不是完整圆盘，只保留水平长条和中心竖向柔光，远处侧视时更像有厚度。
    float sideBand = exp(-abs(p.y) * 10.0) * (1.0 - smoothstep(0.12, 1.0, abs(p.x)));
    float sidePulse = 0.72 + 0.28 * sin(time * 8.0 + p.x * 9.0);

    // 中心层先展开，外围层稍后展开，形成“先中间再外围”的节奏。
    float centerReveal = radialReveal(radius, max(centerProgress, 0.04), 0.10);
    float outerReveal = radialReveal(radius, max(outerProgress, 0.04), 0.12);
    float reveal = max(centerReveal * (1.0 - smoothstep(0.42, 0.56, radius)), outerReveal);

    // 多层旋转角度错开，避免所有线条同速转动导致画面单调。
    float slowAngle = angle + time * 0.22;
    float fastAngle = angle - time * 0.48;

    // 基础圆环，主环层和外符文层分开显示，避免多层叠在同一高度糊成一片。
    float circles = ring(radius, 0.18, 0.016) * centerReveal;
    circles += ring(radius, 0.34, 0.012) * centerReveal;
    circles += ring(radius, 0.55, 0.014) * outerReveal;
    circles += ring(radius, 0.76, 0.018) * outerReveal;
    circles += ring(radius, 0.93, 0.012) * outerReveal;

    // 中心图案固定为八芒星，核心层先展开，最终裁决前八个星尖依次扫亮。
    float star = eightPointStar(p, 0.22, 0.58, 0.014, time * 0.12) * centerReveal;
    star += eightPointStar(p, 0.10, 0.34, 0.010, -time * 0.20) * centerReveal * 0.65;
    star += starTipSweep(radius, angle, time, max(centerProgress, strikeProgress)) * 0.8;

    // 外层只保留淡淡八边骨架，中心不再使用三角或六边形作为主图案。
    float outerFrame = polygonLine(p, 8.0, 0.82, 0.010, -time * 0.16) * outerReveal * 0.42;

    // 外圈刻度和内圈短刻度提供机械式仪式细节。
    float outerTicks = ticks(slowAngle, 96.0, 0.055) * ring(radius, 0.88, 0.035) * outerReveal;
    float innerTicks = ticks(fastAngle, 36.0, 0.11) * ring(radius, 0.39, 0.022) * centerReveal;

    // 伪符文不需要贴图，依靠角度分段和随机门控生成字块感。
    float runes = runeMarks(radius, angle, time, outerReveal);

    // 扫描光从中心向外绕圈，增强“法阵正在计算”的感觉。
    float scanAngle = abs(fract((angle + time * 1.15) / (PI * 2.0)) - 0.5);
    float scan = (1.0 - smoothstep(0.0, 0.045, scanAngle)) * smoothstep(0.18, 0.88, radius) * outerReveal;

    // 最终裁决阶段增强中心闪光和外圈爆亮。
    float strikeFlash = strikeProgress * (1.0 - smoothstep(0.0, 0.86, radius));
    float edgeFlash = strikeProgress * ring(radius, 0.96, 0.045);

    float coreSymbol = (star * 1.2 + circles * 0.35 + innerTicks * 0.35) * coreLayer * centerReveal;
    float mainSymbol = (circles * 0.75 + innerTicks * 0.46 + scan * 0.28) * mainLayer * reveal;
    float outerSymbol = (circles * 0.42 + outerFrame + outerTicks * 0.44 + runes * 0.52 + scan * 0.22) * outerLayer * outerReveal;
    float symbol = coreSymbol + mainSymbol + outerSymbol;
    symbol += sideLayer * sideBand * sidePulse * max(outerProgress, centerProgress);
    symbol += strikeFlash * (1.35 + coreLayer * 0.55) + edgeFlash * 0.82;

    // 颜色统一为紫蓝色调：亮蓝紫核心、冰蓝紫符文、深紫蓝外辉。
    vec3 coreColor = vec3(0.78, 0.88, 1.0);
    vec3 runeColor = vec3(0.42, 0.48, 1.0);
    vec3 outerColor = vec3(0.22, 0.06, 0.92);
    vec3 color = mix(runeColor, coreColor, smoothstep(0.0, 0.44, 1.0 - radius));
    color = mix(color, outerColor, smoothstep(0.62, 1.0, radius));

    // 轻微脉冲和生命周期淡出，避免法阵静止或突然消失。
    float pulse = 0.82 + 0.18 * sin(time * 5.8 + radius * 18.0);
    float lateFade = 1.0 - smoothstep(0.90, 1.0, ageProgress);
    float alpha = clamp(symbol * pulse * vColor.a * lateFade, 0.0, 1.0);
    if (alpha < 0.004) {
        discard;
    }

    fragColor = vec4(color * alpha, alpha);
    // bloom 与可见颜色分离，范围翻倍后只给核心和最终闪光较高 bloom，外环保持清晰。
    float bloomWeight = 0.22 + coreLayer * 0.28 + mainLayer * 0.16 + outerLayer * 0.10 + sideLayer * 0.06 + strikeProgress * 0.72;
    bloomWeight = clamp(bloomWeight, 0.0, 1.0);
    bloomColor = vec4(color * alpha * bloomStrength * bloomWeight, alpha * bloomWeight);
}
