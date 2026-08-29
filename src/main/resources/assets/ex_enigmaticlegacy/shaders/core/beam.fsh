#version 330 core

// 光束片段 shader：写出可见颜色 CA0 和 bloom source CA1。
in vec2 vUV;
in vec4 vColor;

// MRT 输出：mainFBO 的 CA0 接收可见颜色，CA1 接收泛光源。
layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 bloomColor;

// 打包浮点 uniform：x=时间秒，y=bloom强度，z=噪声强度，w=保留。
uniform vec4 EffectParams;

// 打包整型 uniform：x=效果类型，y=是否写入bloom，z/w=保留。
uniform ivec4 RenderFlags;

// 光束颜色 uniform：普通光束和 ColorfulCoin 光束由不同队列分别设置。
uniform vec4 BeamCoreColor;
uniform vec4 BeamInnerColor;
uniform vec4 BeamOuterColor;

// 噪声哈希：用于生成沿光束流动的电弧闪烁。
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

// 二维值噪声：足够轻量，适合当前光束片段效果。
float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

// 分形噪声：让电弧边缘比单层噪声更自然。
float fbm(vec2 p) {
    float value = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 3; i++) {
        value += amp * noise(p);
        p *= 2.0;
        amp *= 0.5;
    }
    return value;
}

void main() {
    // 基础参数解包。
    float time = EffectParams.x;
    float bloomStrength = EffectParams.y;
    float noiseStrength = EffectParams.z;
    float bloomEnabled = RenderFlags.y == 0 ? 0.0 : 1.0;

    // UV 含义：x 沿光束方向，y 为截面宽度方向。
    float alongBeam = clamp(vUV.x, 0.0, 1.0);
    float crossBeam = clamp(vUV.y, 0.0, 1.0);
    float dist = abs(crossBeam - 0.5) * 2.0;

    // 两端做软淡出，避免 CPU 生成的 QUADS 边缘太硬。
    float startFade = smoothstep(0.0, 0.045, alongBeam);
    float endFade = 1.0 - smoothstep(0.90, 1.0, alongBeam);
    float edgeFade = startFade * endFade;

    // 光束核心和外圈辉光。
    float core = exp(-dist * dist * 30.0);
    float glow = exp(-dist * dist * 4.5) * 0.62;

    // 沿光束方向滚动的电弧噪声。
    float n = fbm(vec2(crossBeam * 5.0, alongBeam * 18.0 - time * 5.5));
    float electric = smoothstep(0.38, 0.72, n * noiseStrength) * glow;

    // 高频闪烁只轻微调制，避免整条光束明暗跳变过大。
    float flicker = 0.86 + 0.14 * sin(time * 38.0 + alongBeam * 72.0);

    // 合成颜色：核心色 + 内外圈渐变辉光 + 更亮的电弧。
    vec3 coreColor = BeamCoreColor.rgb;
    vec3 glowColor = mix(BeamInnerColor.rgb, BeamOuterColor.rgb, smoothstep(0.25, 1.0, dist));
    vec3 arcColor = BeamInnerColor.rgb * 1.35;
    vec3 color = coreColor * core + glowColor * glow + arcColor * electric;

    // alpha 使用顶点透明度控制生命周期，shader 负责截面和端点软化。
    float alpha = max(core, glow * 0.55 + electric * 0.35) * vColor.a * edgeFade * flicker;
    alpha = clamp(alpha, 0.0, 1.0);
    if (alpha < 0.004) {
        discard;
    }

    // CA0 输出可见光束。
    fragColor = vec4(color * alpha, alpha);

    // CA1 输出 bloom source，外圈颜色单独参与，保证 ColorfulCoin 的红色泛光能被 blur 放大。
    vec3 bloomTint = coreColor * core
            + BeamInnerColor.rgb * (glow * 0.45 + electric * 0.42)
            + BeamOuterColor.rgb * glow * 1.05;
    float bloomAlpha = (core * 0.95 + glow * 0.85 + electric * 0.42) * vColor.a * edgeFade * flicker;
    bloomAlpha = clamp(bloomAlpha, 0.0, 1.0);
    bloomColor = vec4(bloomTint * bloomAlpha * bloomStrength * bloomEnabled, bloomAlpha * bloomEnabled);
}
