#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float Time;        // 改为 Time，匹配JSON
uniform float ColorCycle;  // 可以用于额外的颜色循环控制

out vec4 vertexColor;
out vec2 texCoord0;
out vec2 texCoord1;
out float time;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexColor = Color;
    time = Time * 24000.0; // 使用 Time 而不是 GameTime

    // 第一层纹理坐标 - 模拟原版的glTexGeni效果
    texCoord0 = UV0;
    texCoord0.x += sin(time * 0.001) * 0.1;
    texCoord0.y += cos(time * 0.0015) * 0.1;

    // 第二层纹理坐标 - 不同的移动模式
    texCoord1 = UV0;
    texCoord1.x += cos(time * 0.0008) * 0.05;
    texCoord1.y += sin(time * 0.0012) * 0.05;

    // 添加深度变化，可以结合ColorCycle来控制
    texCoord1 *= 1.0 + sin(time * 0.0005 + ColorCycle) * 0.2;
}