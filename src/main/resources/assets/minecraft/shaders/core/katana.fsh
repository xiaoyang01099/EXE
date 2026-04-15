#version 150

#define PI 3.14159265359

uniform float iTime;
uniform vec2  iResolution;

out vec4 fragColor;

vec2 normalizeScreenSpace(vec2 fragCoord, out float aa) {
    aa = 0.003;
    return (2. * fragCoord - iResolution.xy) / iResolution.y;
}

mat2 rotate(float a) {
    a *= PI * 2.;
    float s = sin(a);
    float c = cos(a);
    return mat2(c, -s, s, c);
}

float dot2(in vec2 v) { return dot(v, v); }

float sdTrapezoid(in vec2 p, in float r1, float r2, float he) {
    vec2  k1 = vec2(r2, he);
    vec2  k2 = vec2(r2 - r1, 2.0 * he);
    p.x      = abs(p.x);
    vec2  ca = vec2(p.x - min(p.x, (p.y < 0.0) ? r1 : r2), abs(p.y) - he);
    vec2  cb = p - k1 + k2 * clamp(dot(k1 - p, k2) / dot2(k2), 0.0, 1.0);
    float s  = (cb.x < 0.0 && ca.y < 0.0) ? -1.0 : 1.0;
    return s * sqrt(min(dot2(ca), dot2(cb)));
}

float slash(vec2 p, vec2 position, float rotation, float time) {
    if (time == 0.) return 0.;

    p += position;

    float slashShade = smoothstep(0.001, -0.001,
        sdTrapezoid(p * rotate(rotation), 0.015, 0.001, 3.));

    float slashTime          = clamp(time, 0., 0.1) * 20.;
    vec2  slideY             = p * rotate(rotation);
    float slashAnimationMask = 1. - clamp(
        smoothstep(slashTime, slashTime, slideY.y + 1.), 0., 1.);

    float fadeTime = 1. - clamp(time, 0.1, 0.9);

    return slashAnimationMask * slashShade * fadeTime;
}

void main() {
    vec2  fragCoord  = gl_FragCoord.xy;
    float loopingTime = fract(iTime / 4.);
    float aa          = 0.;
    vec2  p           = normalizeScreenSpace(fragCoord, aa);

    float shakeScale = 0.;
    shakeScale = max(shakeScale, clamp(sin((clamp(loopingTime * 8.,       0., 1.) - 1.) * 6.), 0., 1.));
    shakeScale = max(shakeScale, clamp(sin((clamp(loopingTime * 8.,       1., 2.) - 1.) * 6.), 0., 1.));
    shakeScale = max(shakeScale, clamp(sin((clamp(loopingTime * 8.,       2., 3.) - 2.) * 6.), 0., 1.));

    shakeScale = max(shakeScale, clamp(sin((clamp(loopingTime * 16.,  8.,  9.) - 2.) * 6.), 0., 1.));
    shakeScale = max(shakeScale, clamp(sin((clamp(loopingTime * 16.,  9., 10.) - 2.) * 6.), 0., 1.));
    shakeScale = max(shakeScale, clamp(sin((clamp(loopingTime * 16., 10., 11.) - 2.) * 6.), 0., 1.));
    shakeScale = max(shakeScale, clamp(sin((clamp(loopingTime * 16., 11., 12.) - 2.) * 6.), 0., 1.));
    shakeScale = max(shakeScale, clamp(sin((clamp(loopingTime * 16., 12., 13.) - 2.) * 6.), 0., 1.));

    p += vec2(
        sin(loopingTime * 150.)          * 0.05 * shakeScale,
        sin((loopingTime + 2.) * 173.)   * 0.05 * shakeScale
    );

    float v = 0.;

    v = max(v, slash(p, vec2( 0.40,  0.60), 0.67,
        loopingTime * 8.));
    v = max(v, slash(p, vec2( 0.40,  0.20), 0.47,
        clamp(loopingTime * 8., 1., 2.) - 1.));
    v = max(v, slash(p, vec2(-0.20,  0.60), 0.33,
        clamp(loopingTime * 8., 2., 3.) - 2.));

    v = max(v, slash(p, vec2( 0.25, -0.30), 0.88,
        clamp(loopingTime * 16.,  8.,  9.) - 8.));
    v = max(v, slash(p, vec2(-0.85,  0.20), 0.23,
        clamp(loopingTime * 16.,  9., 10.) - 9.));
    v = max(v, slash(p, vec2( 0.25, -0.10), 0.02,
        clamp(loopingTime * 16., 10., 11.) - 10.));
    v = max(v, slash(p, vec2( 0.15,  0.20), 0.59,
        clamp(loopingTime * 16., 11., 12.) - 11.));
    v = max(v, slash(p, vec2(-0.85,  0.70), 0.29,
        clamp(loopingTime * 16., 12., 13.) - 12.));

    fragColor = vec4(v, v, v, 1.0);
}