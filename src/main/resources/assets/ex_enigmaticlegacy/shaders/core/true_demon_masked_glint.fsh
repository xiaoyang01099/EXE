#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform float GlintAlpha;

in float vertexDistance;
in vec2 glintTexCoord;
in vec2 baseTexCoord;

out vec4 fragColor;

void main() {
    // Use the same strict LOD-0 #ffffff test as the rainbow base shader so the
    // two passes can never disagree because of bilinear filtering or mipmaps.
    ivec2 atlasSize = textureSize(Sampler1, 0);
    ivec2 atlasTexel = clamp(
            ivec2(floor(baseTexCoord * vec2(atlasSize))),
            ivec2(0),
            atlasSize - ivec2(1)
    );
    vec4 baseSample = texelFetch(Sampler1, atlasTexel, 0);
    bool exactWhite = all(greaterThan(baseSample.rgb, vec3(0.9999))) && baseSample.a > 0.0;

    // Reserve the original #ffffff pixels exclusively for the bow rainbow shader.
    if (exactWhite) {
        discard;
    }

    vec4 color = texture(Sampler0, glintTexCoord) * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    float fade = linear_fog_fade(vertexDistance, FogStart, FogEnd) * GlintAlpha;
    fragColor = vec4(color.rgb * fade, color.a);
}
