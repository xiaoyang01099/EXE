#version 330 core

// 光束顶点 shader：使用 Minecraft core shader 的标准矩阵 uniform。
in vec3 Position;
in vec4 Color;
in vec2 UV0;


// Minecraft ShaderInstance 会在 drawWithShader 时自动写入这两个矩阵。
uniform mat4 uView;
uniform mat4 ProjMat;

// 传给片段 shader 的插值数据：UV 控制光束截面，Color 保存每条光束的颜色和透明度。
out vec2 vUV;
out vec4 vColor;

void main() {
    gl_Position = ProjMat * uView * vec4(Position, 1.0);
    vUV = UV0;
    vColor = Color;
}
