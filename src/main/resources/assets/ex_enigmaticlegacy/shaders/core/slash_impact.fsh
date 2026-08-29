#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 TexelSize;
uniform float Time;
uniform float FrameStrength;
uniform float InvertStrength;
uniform float ThresholdStrength;
uniform float CompressionStrength;
uniform float CoreStrength;
uniform float VignetteStrength;

in vec2 texCoord;
out vec4 fragColor;

vec3 sampleScene(vec2 uv) {
    return texture(DiffuseSampler, clamp(uv, vec2(0.0), vec2(1.0))).rgb;
}

float luminance(vec3 color) {
    return dot(color, vec3(0.299, 0.587, 0.114));
}

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

void main() {
    float frame = saturate(FrameStrength);
    vec2 center = vec2(0.5);
    vec2 d = texCoord - center;
    vec2 aspect = vec2(1.16, 1.0);
    float dist = length(d * aspect);

    float pressureMask = (1.0 - smoothstep(0.04, 0.92, dist)) * smoothstep(0.002, 0.14, dist);
    float shockRipple = sin(dist * 72.0 - Time * 90.0) * 0.0026 * frame;
    vec2 warpedUv = center + d * (1.0 + CompressionStrength * pressureMask + shockRipple);
    vec3 scene = sampleScene(warpedUv);

    float gray = luminance(scene);
    vec3 color = mix(scene, vec3(gray), frame);
    float contrast = 1.0 + frame * 5.8;
    color = (color - 0.5) * contrast + 0.5;

    float contrastLum = luminance(color);
    float threshold = 0.50 - frame * 0.045 + sin(Time * 48.0) * 0.018 * frame;
    vec3 hardMono = vec3(step(threshold, contrastLum));
    color = mix(color, hardMono, saturate(ThresholdStrength));
    color = mix(color, vec3(1.0) - color, saturate(InvertStrength));

    float pressureWall = smoothstep(0.05, 0.28, dist) * (1.0 - smoothstep(0.42, 0.90, dist));
    float edgeShadow = smoothstep(0.22, 1.02, dist);
    float cornerShadow = smoothstep(0.46, 1.28, length(d * vec2(1.75, 1.30)));
    float darken = saturate(VignetteStrength * (edgeShadow * 0.72 + cornerShadow * 0.52 + pressureWall * 0.42));
    color *= mix(vec3(1.0), vec3(0.012, 0.011, 0.017), darken);

    float core = exp(-dist * dist * 42.0) * CoreStrength;
    float pin = exp(-dist * dist * 220.0) * CoreStrength * 0.72;
    color += vec3(core + pin);

    fragColor = vec4(clamp(color, vec3(0.0), vec3(1.0)), 1.0);
}
