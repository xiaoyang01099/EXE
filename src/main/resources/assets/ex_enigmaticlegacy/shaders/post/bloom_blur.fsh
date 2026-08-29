#version 330 core

uniform sampler2D inputTexture;
uniform vec2 direction;
// 控制单向高斯采样偏移的半径倍率，用于适度扩大半分辨率 Bloom 范围。
uniform float BlurRadius;

in vec2 textureCoords;
out vec4 out_Colour;

const float weights[5] = float[](
    0.227027,
    0.1945946,
    0.1216216,
    0.054054,
    0.016216
);

void main() {
    // 输入是半分辨率 Ping-Pong 纹理，因此 texelSize 已自动对应半分辨率像素间距。
    vec2 texelSize = 1.0 / vec2(textureSize(inputTexture, 0));
    vec4 color = texture(inputTexture, textureCoords) * weights[0];

    for (int i = 1; i < 5; i++) {
        // 按当前方向、纹理像素间距与可调半径计算对称的高斯采样偏移。
        vec2 offset = direction * texelSize * float(i) * BlurRadius;
        color += texture(inputTexture, textureCoords + offset) * weights[i];
        color += texture(inputTexture, textureCoords - offset) * weights[i];
    }

    out_Colour = color;
}
