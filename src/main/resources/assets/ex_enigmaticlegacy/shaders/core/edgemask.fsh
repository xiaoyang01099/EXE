#version 150

uniform sampler2D MaskTexture;
uniform float MaskMode;
uniform float TrailCoordinates;
uniform float EffectStyle;
in vec4 vertexColor;
in vec2 localUV;
out vec4 fragColor;

void main() {
    float coverage = 1.0;
    if (MaskMode > 0.5) {
        vec4 texel = texture(MaskTexture, localUV);
        coverage = MaskMode < 1.5 ? texel.a : texel.r;
    }
    coverage *= vertexColor.a;
    if (coverage < 0.004 || vertexColor.r < 0.004) discard;
    vec2 effectUV = TrailCoordinates > 0.5 ? clamp(localUV, 0.0, 1.0) : vec2(0.0, 1.0);
    float styleStrength=EffectStyle>.5?.5+vertexColor.r*.5:vertexColor.r*.49;
    fragColor = vec4(effectUV, styleStrength, coverage);
}
