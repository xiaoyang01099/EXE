#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float Time;
uniform vec3 BlackHolePos;
uniform float BlackHoleMass;

out vec2 texCoord0;
out vec4 vertexColor;

// 计算引力势（牛顿近似）
float calculateGravitationalPotential(vec3 pos, vec3 blackHolePos, float mass) {
    float G = 6.67430e-11;
    float M_sun = 1.98847e30;
    float r = distance(pos, blackHolePos);

    if (r < 0.001) return 0.0;

    return -(G * mass * M_sun) / r;
}

void main() {
    vec4 viewPos4 = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPos4;

    vec3 worldPos = Position;

    float gravitationalPotential = calculateGravitationalPotential(
        worldPos,
        BlackHolePos,
        BlackHoleMass
    );

    float tidalForce = gravitationalPotential * 1e-10;
    vec3 offset = normalize(worldPos - BlackHolePos) * tidalForce;

    if (length(BlackHolePos) > 0.001) {
        vec4 offsetPos = ModelViewMat * vec4(Position + offset, 1.0);
        gl_Position = ProjMat * offsetPos;
    }

    texCoord0 = UV0;
    vertexColor = Color;
}
