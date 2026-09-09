#version 150

uniform float GameTime;
in vec4 vColor;
in vec2 vUv;
in float vDist;
out vec4 fragColor;

float hash11(float n) { return fract(sin(n * 78.233) * 43758.5453123); }

float noise11(float x) {
    float i = floor(x);
    float f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    return mix(hash11(i), hash11(i + 1.0), f);
}

void main() {
    if (vUv.y > 1.5) {
        vec2 q = vec2(vUv.x, vUv.y - 2.0) * 2.0 - 1.0;
        float r = length(q);
        if (r > 1.0) discard;

        float falloff = 1.0 - r;
        float core = pow(falloff, 5.0);
        float halo = pow(falloff, 1.8);
        float wide = pow(falloff, 0.9) * 0.35;

        vec3 col = vec3(1.0) * core * 1.5
                 + vColor.rgb * halo * 1.0
                 + vColor.rgb * wide * 0.8;

        float a = (core * 0.85 + halo * 0.7 + wide) * vColor.a;
        if (a < 0.004) discard;
        fragColor = vec4(col, clamp(a, 0.0, 1.0));
        return;
    }

    float d = clamp(abs(vUv.x * 2.0 - 1.0), 0.0, 1.0);
    float t = GameTime * 24000.0;

    float flick = 0.72
        + 0.20 * noise11(vUv.y * 24.0 + t * 2.7)
        + 0.14 * noise11(vUv.y * 71.0 - t * 5.1);

    float core  = pow(1.0 - d, 12.0);
    float inner = pow(1.0 - d, 3.5);
    float glow  = pow(1.0 - d, 1.3) * 0.42;

    float distBoost = 1.0 + clamp(vDist / 90.0, 0.0, 1.0) * 0.55;

    vec3 col = vec3(1.0) * core * 1.9
             + vColor.rgb * inner * 1.05
             + vColor.rgb * glow * 0.85;

    float a = (core * 1.15 + inner * 0.5 + glow) * vColor.a * flick * distBoost;

    if (a < 0.004) discard;
    fragColor = vec4(col, clamp(a, 0.0, 1.0));
}
