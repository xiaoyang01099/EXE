#version 330 core

in vec2 position;

out vec2 textureCoords;

void main(void) {
    // 全屏 quad 顶点已经是裁剪空间坐标，直接输出到屏幕。
    gl_Position = vec4(position, 0.0, 1.0);
    // 把 -1..1 的裁剪空间坐标转换成 0..1 的纹理坐标。
    textureCoords = position * 0.5 + 0.5;
}
