#version 150
uniform sampler2D ParticleTexture;
uniform sampler2D LaserTexture;
uniform sampler2D SpikeTexture;
in vec4 vertexColor;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    int material = int(floor(texCoord.x / 2.0));
    vec2 uv = vec2(texCoord.x - float(material * 2), texCoord.y);
    vec4 texel = material == 0 ? vec4(1.0) : material == 3 ? texture(SpikeTexture, uv)
        : material == 4 ? texture(LaserTexture, uv) : texture(ParticleTexture, uv);
    vec4 color = texel * vertexColor;
    if (color.a < 0.003) discard;
    fragColor = vec4(color.rgb * color.a, material == 2 || material == 4 ? 0.0 : color.a);
}
