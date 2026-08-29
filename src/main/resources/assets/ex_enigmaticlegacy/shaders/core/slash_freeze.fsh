#version 150

uniform sampler2D DiffuseSampler;
uniform float Desaturation;
uniform float Contrast;
uniform float Brightness;
uniform float VignetteStrength;

in vec2 texCoord;

out vec4 fragColor;

float luminance(vec3 color) {
    return dot(color, vec3(0.299, 0.587, 0.114));
}

void main() {
    vec3 scene = texture(DiffuseSampler, clamp(texCoord, vec2(0.0), vec2(1.0))).rgb;
    float gray = luminance(scene);
    vec3 color = mix(scene, vec3(gray), clamp(Desaturation, 0.0, 1.0));
    color = (color - 0.5) * max(Contrast, 0.0) + 0.5;
    color *= Brightness;

    vec2 d = texCoord - vec2(0.5);
    float edge = smoothstep(0.32, 0.82, length(d * vec2(1.16, 1.0)));
    color *= mix(1.0, 0.72, clamp(VignetteStrength, 0.0, 1.0) * edge);

    fragColor = vec4(clamp(color, vec3(0.0), vec3(1.0)), 1.0);
}
