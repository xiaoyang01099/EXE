#version 330 core

// 全屏四边形的局部 position 由 BloomRender 的共享 quad 提供。
in vec2 position;

// 传给片元 shader 的归一化纹理坐标，覆盖完整 Bloom source。
out vec2 textureCoords;

void main(void) {
    // 不进行相机投影，直接绘制覆盖当前半分辨率 FBO 的全屏三角形条带。
    gl_Position = vec4(position, 0.0, 1.0);
    // position 的范围是 -1 到 1，转换为纹理采样需要的 0 到 1。
    textureCoords = position * 0.5 + 0.5;
}
