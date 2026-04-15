#version 150

#define PI     3.141596
#define freq   5.5
#define TRAILS 10

uniform float iTime;
uniform vec2  iResolution;

out vec4 fragColor;

mat2 rot(float a) {
    float s = sin(a);
    float c = cos(a);
    return mat2(c, -s, s, c);
}

float hash11(float v) {
    return fract(sin(v * 3.568 + 7.395));
}

vec2 hash12(float v) {
    return vec2(hash11(v), hash11(v + 1.5));
}

vec4 line(vec2 p, vec2 size, float a, float colorOffset) {
    p *= rot(a);

    float d    = abs(p.y);
    d          = pow(size.x / d, 1.7);

    float mask = smoothstep(size.y, 0., abs(p.x));
    d         *= mask;

    vec3 col = d * sin(vec3(3., 3., 1.) * colorOffset + p.x);
    return vec4(col, d);
}

vec4 randomLineAt(vec2 p, float t) {
    float ti          = floor(t * freq);
    float tf          = fract(t * freq);
    float seed        = ti;

    vec2  offset      = hash12(seed) * 0.1 - 0.05;
    vec2  size        = vec2(0.005, 0.4) * (hash12(seed) + 1.);
    float colorOffset = tf;
    float angle       = hash11(seed) * PI * 20.;

    return line(p - offset, size, angle, colorOffset);
}

void main() {
    vec2 fragCoord = gl_FragCoord.xy;
    vec2 uv = (fragCoord - iResolution * .5) / iResolution.y;

    vec3 col = vec3(0.);

    col += randomLineAt(uv, iTime).rgb;
    col += randomLineAt(uv, iTime + 0.07).rgb;
    col += randomLineAt(uv, iTime + 0.14).rgb;

    for (int i = 1; i <= TRAILS; i++) {
        float pastTime = iTime - float(i) / freq;
        float decay    = pow(0.9, float(i));
        col += randomLineAt(uv, pastTime).rgb          * decay;
        col += randomLineAt(uv, pastTime + 0.07).rgb   * decay;
        col += randomLineAt(uv, pastTime + 0.14).rgb   * decay;
    }

    fragColor = vec4(col, 1.0);
}