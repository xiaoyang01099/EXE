#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 Position;
in vec2 UV0;

out vec2 texCoord;
out vec3 localPos;

void main() {
    texCoord = UV0;
    localPos = Position;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}