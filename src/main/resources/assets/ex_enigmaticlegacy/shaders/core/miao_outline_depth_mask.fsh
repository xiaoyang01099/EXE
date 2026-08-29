#version 150

uniform sampler2D Sampler0;
uniform vec4 DepthParams;

in vec2 texCoord0;
in vec3 viewPosition;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 entityColor = texture(Sampler0, texCoord0);
    if (entityColor.a <= DepthParams.w) {
        discard;
    }

    float viewDepth = max(-viewPosition.z - DepthParams.x, 0.0);
    float normalizedDepth = clamp(viewDepth / max(DepthParams.y, 1.0), 0.0, 1.0);

    fragColor = vec4(normalizedDepth, DepthParams.z, 0.0, 1.0);
}
