#version 330 core

// 冲击波顶点 shader：
// Java 侧提交始终面向摄像机的世界空间 billboard quad。
// Position 是 billboard 四个角的世界坐标。
// UV0 是完整 0..1 局部 UV，片元阶段用它做 UE5 径向材质采样。
// Color 保存每个冲击波的可见层 alpha，BloomColor.x 保存每个冲击波的动画速度随机值。
in vec3 Position;
in vec2 UV0;
in vec4 Color;
in ivec2 BloomColor;

// Minecraft core shader 写入 ProjMat；FinalRender 额外写入当前相机 view 矩阵。
uniform mat4 ProjMat;
uniform mat4 uView;

out vec2 vUV;
out vec4 vColor;
flat out float vTimeSpeedRandom;

void main() {
    // 冲击波顶点已经是世界坐标，这里只做 view/projection 变换。
    gl_Position = ProjMat * uView * vec4(Position, 1.0);
    // 局部 UV 原样传给片元 shader，保证径向中心固定在 0.5/0.5。
    vUV = UV0;
    // 顶点颜色用于传递生命周期 alpha，颜色本身由材质 tint 控制。
    vColor = Color;
    // BloomColor 是 UV2 整数通道，x 量化保存 0..255 的速度随机值。
    vTimeSpeedRandom = float(BloomColor.x) / 255.0;
}
