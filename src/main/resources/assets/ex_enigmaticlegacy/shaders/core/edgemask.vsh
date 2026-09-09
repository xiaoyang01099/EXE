#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
uniform mat4 BladeView;
uniform mat4 BladeProjection;
uniform float ScreenSpace;
out vec4 vertexColor;
out vec2 localUV;

void main() {
    gl_Position = ScreenSpace > 0.5 ? vec4(Position, 1.0) : BladeProjection * BladeView * vec4(Position, 1.0);
    vertexColor = Color;
    localUV = UV0;
}
