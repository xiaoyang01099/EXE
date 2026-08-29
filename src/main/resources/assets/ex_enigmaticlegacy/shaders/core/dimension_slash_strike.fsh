#version 330 core

in vec2 vUV;
in vec4 vColor;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 bloomColor;
uniform vec4 EffectParams;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

void main() {
    // UV.y 是斩击宽度方向，中心白色，边缘蓝色。
    float along = clamp(vUV.x, 0.0, 1.0);
    float cross = abs(vUV.y - 0.5) * 2.0;
    float endFade = smoothstep(0.0, 0.08, along) * (1.0 - smoothstep(0.88, 1.0, along));
    float core = exp(-cross * cross * 26.0);
    float glow = exp(-cross * cross * 4.2);
    float slashLine = smoothstep(0.92, 1.0, hash(vec2(floor(along * 18.0), floor(vUV.y * 5.0)) + EffectParams.x * 0.01));
    float alpha = (core * 0.98 + glow * 0.42 + slashLine * 0.12) * endFade * vColor.a;
    if (alpha < 0.004) {
        discard;
    }

    vec3 coreColor = vec3(0.92, 0.98, 1.0);
    vec3 edgeColor = vec3(0.18, 0.58, 1.0);
    vec3 color = mix(edgeColor, coreColor, clamp(core + 0.25, 0.0, 1.0));

    fragColor = vec4(color * alpha, alpha);
    bloomColor = vec4((coreColor * core + edgeColor * glow) * alpha * EffectParams.y, alpha);
}
