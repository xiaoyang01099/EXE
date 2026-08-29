#version 330 core

// 星辰裁决法阵顶点 shader。
// CPU 只提交少量四边形顶点，真正的圆环、刻度和符文都在片段 shader 中按 UV 计算。
in vec3 Position;
in vec4 Color;
in vec2 UV0;

// uView 由队列显式写入，ProjMat 由 Minecraft core shader 系统写入。
uniform mat4 uView;
uniform mat4 ProjMat;

// vUV 保存四边形局部坐标，vColor.r 保存层类型，vColor.a 保存该层基础透明度。
out vec2 vUV;
out vec4 vColor;

void main() {
    gl_Position = ProjMat * uView * vec4(Position, 1.0);
    vUV = UV0;
    vColor = Color;
}
