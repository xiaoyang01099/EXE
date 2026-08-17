#version 150

#define PI 3.141596
#define FREQ 5.5

uniform float iTime;
uniform vec2  iResolution;

in  vec2 texCoord;
out vec4 fragColor;

mat2 rot(float a) {
    float s = sin(a), c = cos(a);
    return mat2(c, -s, s, c);
}

float hash11(float v) {
    return fract(sin(v * 3.568 + 7.395));
}

vec2 hash12(float v) {
    return vec2(hash11(v), hash11(v + 1.5));
}

// 单条刀光线，返回 rgb + 强度
vec4 bladeLine(vec2 p, vec2 size, float angle, float colorOff) {
    p = rot(angle) * p;
    float d    = pow(size.x / max(abs(p.y), 0.0001), 1.7);
    float mask = smoothstep(size.y, 0.0, abs(p.x));
    d *= mask;
    // 让线条颜色偏蓝白，适合刀光
    vec3 col = d * (vec3(0.6, 0.85, 1.0) + 0.4 * sin(vec3(3.0, 3.0, 1.0) * colorOff + p.x));
    return vec4(col, d);
}

vec4 swordSlash(vec2 uv, float timeOff) {
    float t    = (iTime + timeOff) * FREQ;
    float ti   = floor(t);
    float tf   = fract(t);

    vec2  offset     = hash12(ti) * 0.1 - 0.05;
    vec2  size       = vec2(0.005, 0.45) * (hash12(ti) + 1.0);
    float angle      = hash11(ti) * PI * 20.0;
    float colorOff   = tf;

    // 沿刀身方向拉伸：把 UV 映射到以刀身中心为原点的空间
    vec2 p = uv - vec2(0.5, 0.5) - offset;
    // 刀身方向拉长（X 轴），宽度方向压缩
    p.x *= 1.6;
    p.y *= 2.5;

    return bladeLine(p, size, angle, colorOff);
}

// 拖尾：用多个时间偏移层叠加，衰减模拟残影
vec3 trailEffect(vec2 uv) {
    vec3 col = vec3(0.0);
    float decay = 0.85;
    float w = 1.0;
    for (int i = 0; i < 6; i++) {
        float off = float(i) * 0.04;  // 每层时间偏移
        vec4  l   = swordSlash(uv, -off);
        col += l.rgb * w;
        w   *= decay;
    }
    return col;
}

// 刀身核心发光
float coreGlow(vec2 uv) {
    float y    = uv.y - 0.5;
    float edge = smoothstep(0.0, 1.0, uv.x);       // 尖端淡出
    float root = smoothstep(0.0, 0.15, uv.x);      // 根部淡入
    float glow = exp(-abs(y) * 18.0) * edge * root;
    return glow;
}

void main() {
    vec2 uv = texCoord;

    vec3 col = vec3(0.0);

    // 拖尾刀光
    col += trailEffect(uv);

    // 刀身核心白光
    float core = coreGlow(uv);
    col += vec3(0.8, 0.95, 1.0) * core * 2.5;

    // 外层蓝色晕光
    col += vec3(0.2, 0.5, 1.0) * core * 0.8;

    float alpha = clamp(length(col) * 0.8 + core * 0.9, 0.0, 1.0);

    fragColor = vec4(col, alpha);
}