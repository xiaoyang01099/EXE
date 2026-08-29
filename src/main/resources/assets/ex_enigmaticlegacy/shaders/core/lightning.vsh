#version 330 core

// 闪电顶点 shader：
// Java 侧直接提交世界空间四边形顶点，uView 负责把世界坐标转换到视图空间。
// Position 是路径闪电 billboard 或地面圆环条带的世界坐标。
// UV0 是局部 sprite UV，片元阶段会映射到 AkatZumaTool 自定义图集中的 lightning_256x 区域。
// Color 保存每条闪电的可见层 tint 和生命周期 alpha。
// BloomColor 复用 UV2 的两个整数打包每条闪电的 bloom tint、噪声索引和噪声强度，片元阶段再解包。
in vec3 Position;
in vec2 UV0;
in vec4 Color;
in ivec2 BloomColor;

// Minecraft core shader 会写入 ProjMat；FinalRender 额外写入当前相机 view 矩阵。
uniform mat4 ProjMat;
uniform mat4 uView;

out vec2 vUV;
out vec4 vColor;
flat out ivec2 vBloomColor;

void main() {
    // 闪电顶点已经是世界坐标，这里只做 view/projection 变换，不修改全局 ModelView。
    gl_Position = ProjMat * uView * vec4(Position, 1.0);
    // 局部 UV 原样传给片元 shader，用于主纹理采样和噪声扰动。
    vUV = UV0;
    // 每条闪电的可见颜色和透明度随顶点传递，支持同批不同颜色。
    vColor = Color;
    // bloom 颜色不做插值，避免同一四边形内部出现颜色漂移。
    vBloomColor = BloomColor;
}
