#version 330 core

// 输入可以是 mainFBO.CA1 的全分辨率 Bloom source，也可以是低分辨率远景 Bloom 结果。
uniform sampler2D inputTexture;

// 顶点 shader 传入的全屏归一化采样坐标。
in vec2 textureCoords;

// 当前目标 FBO 的唯一颜色输出，目标尺寸由 Java 侧绑定的 FBO 决定。
out vec4 out_Colour;

void main() {
    // 按输入纹理尺寸计算一个原始像素对应的 UV 距离，降采样和回叠上采样都复用同一套权重。
    vec2 sourceTexelSize = 1.0 / vec2(textureSize(inputTexture, 0));
    // 保留中心样本的高频亮度，避免细闪电、小粒子和远景 Bloom 回叠时被纯邻域平均过度软化。
    vec4 center = texture(inputTexture, textureCoords);
    // 围绕当前像素中心采样输入纹理的 2x2 相邻像素，降低缩小时的闪烁和放大回叠时的块状感。
    vec2 halfTexel = sourceTexelSize * 0.5;
    vec4 neighborhood = texture(inputTexture, textureCoords + vec2(-halfTexel.x, -halfTexel.y));
    neighborhood += texture(inputTexture, textureCoords + vec2(halfTexel.x, -halfTexel.y));
    neighborhood += texture(inputTexture, textureCoords + vec2(-halfTexel.x, halfTexel.y));
    neighborhood += texture(inputTexture, textureCoords + vec2(halfTexel.x, halfTexel.y));
    // 中心占 0.5，四个邻域各占 0.125，总权重保持为 1，避免改变整体 Bloom 能量。
    out_Colour = center * 0.5 + neighborhood * 0.125;
}
