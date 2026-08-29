#version 330 core

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform mat4 ProjMat;
uniform mat4 uView;

out vec2 vUV;
out vec4 vColor;

void main() {
    gl_Position = ProjMat * uView * vec4(Position, 1.0);
    vUV = UV0;
    vColor = Color;
}
