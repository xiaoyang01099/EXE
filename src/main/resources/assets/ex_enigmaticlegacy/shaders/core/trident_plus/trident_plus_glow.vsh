#version 330 core

// 天雷战戟蓄力蓝光顶点 shader。
// 该 shader 复用 TridentModel.renderToBuffer 写出的 NEW_ENTITY 顶点格式。
// Position 是原版三叉戟模型局部坐标，ModelViewMat 已包含物品手持姿态和当前视图变换。
// UV0 继续传给片元 shader，用于采样原版三叉戟纹理 alpha，避免蓝光覆盖到模型透明区域。
in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

// Minecraft core shader 自动写入当前模型视图矩阵和投影矩阵。
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 vUV;
out vec4 vColor;
out vec3 vNormal;

void main() {
    // 使用标准模型空间路径，确保蓝光层完全贴合 TridentModel 的真实手持姿态。
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    // 纹理 UV 用来保留原版三叉戟 alpha 轮廓。
    vUV = UV0;
    // 顶点颜色 alpha 由 Java 写入，作为整层透明度的额外倍率。
    vColor = Color;
    // 法线用于片元阶段增加轻微边缘亮度，让整把战戟更像发光体。
    vNormal = normalize(mat3(ModelViewMat) * Normal);
}
