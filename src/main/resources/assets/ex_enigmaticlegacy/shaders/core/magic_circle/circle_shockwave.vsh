#version 330 core

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in ivec2 BloomColor;
uniform mat4 ProjMat;
uniform mat4 uView;
out vec2 vUV;
out vec4 vColor;
flat out float vTimeSpeedRandom;

void main() {
    gl_Position = ProjMat * uView * vec4(Position, 1.0);
    vUV = UV0;
    vColor = Color;
    vTimeSpeedRandom = float(BloomColor.x) / 255.0;
}
