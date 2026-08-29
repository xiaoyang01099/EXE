#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec3 Normal;
uniform mat4 ProjMat;
uniform mat4 uView;
out vec2 texCoord0;
out vec3 viewPosition;
out vec4 vertexColor;

void main() {
    vec4 viewPos = vec4(Position, 1.0);
    if (Color.b > 0.5) {
        viewPos = uView * vec4(Position, 1.0);
    }

    gl_Position = ProjMat * viewPos;
    texCoord0 = UV0;
    viewPosition = viewPos.xyz;
    vertexColor = Color;
}
