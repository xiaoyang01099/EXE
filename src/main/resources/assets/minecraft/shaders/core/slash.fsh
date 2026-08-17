#version 150

#define PI 3.141596

uniform float iTime;
uniform vec2  iResolution;

in  vec2 texCoord;
out vec4 fragColor;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

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

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    mat2 r = mat2(0.866, -0.5, 0.5, 0.866);
    for (int i = 0; i < 5; ++i) {
        v += a * noise(p);
        p = r * p * 2.0 + vec2(100.0);
        a *= 0.5;
    }
    return v;
}

void main() {
    float LOOP_TIME   = 5.0;
    float ANGLE       = 2.6;
    float SLASH_FADE  = 0.8;
    float CORE_WIDTH  = 0.015;
    float GLOW_SPREAD = 40.0;
    vec3  GLOW_COLOR  = vec3(2.0, 2.0, 2.0);

    float SMOKE_FADE   = 0.99;
    float SMOKE_EXPAND = 0.3;
    vec3  SMOKE_COLOR1 = vec3(0.9, 0.9, 0.95);
    float SMOKE_SIZE1  = 0.4;
    vec3  SMOKE_COLOR2 = vec3(0.02, 0.02, 0.02);
    float SMOKE_SIZE2  = 0.6;

    // texCoord 是 UV [0,1]，还原成像素坐标
    vec2 fragCoord = texCoord * iResolution;

    vec2 uv = fragCoord / iResolution.xy;
    float aspect = iResolution.x / iResolution.y;
    vec2 p = uv;
    p.x *= aspect;

    vec2 center = vec2(0.5 * aspect, 0.5);
    vec2 dir    = vec2(cos(ANGLE), sin(ANGLE));
    vec2 norm   = vec2(-sin(ANGLE), cos(ANGLE));

    float t          = mod(iTime, LOOP_TIME) / LOOP_TIME;
    float slashAnim  = smoothstep(0.0, 0.03, t) * (1.0 - smoothstep(0.03, SLASH_FADE, t));
    float smokeAnim  = smoothstep(0.0, 0.03, t) * (1.0 - smoothstep(0.2,  SMOKE_FADE, t));
    float smokeSpread = 1.0 + smoothstep(0.0, SMOKE_FADE, t) * SMOKE_EXPAND;

    float dist    = dot(p - center, norm);
    float absDist = abs(dist);
    float l       = dot(p - center, dir);
    float lengthFade = smoothstep(0.9, 0.0, abs(l));

    // 背景透明（Minecraft 世界做背景）
    vec3 col = vec3(0.0);

    // 白煙
    float smokeMask1   = smoothstep(SMOKE_SIZE1 * lengthFade * smokeSpread, 0.0, absDist);
    vec2  smokeUv1     = p * 4.0 - dir * iTime * 0.3;
    smokeUv1          += fbm(p * 3.0 - iTime * 0.2) * 1.5;
    float smokeDensity1 = fbm(smokeUv1);
    float smokeAlpha1  = smoothstep(0.3, 0.7, smokeDensity1 * smokeMask1) * smokeAnim;

    // 黒煙
    float smokeMask2   = smoothstep(SMOKE_SIZE2 * lengthFade * smokeSpread, 0.0, absDist);
    vec2  smokeUv2     = p * 6.0 + dir * iTime * 0.2;
    smokeUv2          += fbm(p * 5.0 + iTime * 0.4) * 2.0;
    float smokeDensity2 = fbm(smokeUv2);
    float smokeAlpha2  = smoothstep(0.4, 0.8, smokeDensity2 * smokeMask2) * smokeAnim;

    col = mix(col, SMOKE_COLOR1, clamp(smokeAlpha1 * 1.5, 0.0, 1.0));
    col = mix(col, SMOKE_COLOR2, clamp(smokeAlpha2 * 1.5, 0.0, 1.0));

    // 斬撃发光
    float whiteAura = exp(-absDist * GLOW_SPREAD);
    float coreLine  = smoothstep(CORE_WIDTH, 0.0, absDist);

    col += GLOW_COLOR * whiteAura * slashAnim * lengthFade;
    col  = mix(col, vec3(0.0), coreLine * slashAnim * lengthFade);

    // alpha：烟雾 + 发光都贡献透明度，背景保持透明
    float alpha = clamp(
        smokeAlpha1 * 0.85 + smokeAlpha2 * 0.7 + whiteAura * slashAnim * lengthFade * 1.5,
        0.0, 1.0
    );

    fragColor = vec4(col, alpha);
}