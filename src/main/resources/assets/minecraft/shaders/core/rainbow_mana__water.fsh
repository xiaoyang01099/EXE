#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float GameTime;

in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord2;

out vec4 fragColor;

// HSV to RGB conversion function
vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    if (color.a < 0.1) {
        discard;
    }

    // 时间转换为刻度
    float time = GameTime * 24000;

    // 保持原有的波动效果
    float wave = sin(time / 80.0 + sin(texCoord0.x / 5 + (texCoord0.y - cos(time / 300.0) * 0.25) / ((1 - sin(time / 200.0) / 10.0) * 5)) * sin(time / 600.0) * 1000) * 0.5 + 0.5;

    // 创建彩虹色相循环 (0.0 到 1.0)
    float hue = fract(time / 1000.0 + texCoord0.x * 0.5 + texCoord0.y * 0.3);

    // 饱和度和明度可以根据波动调整
    float saturation = 0.8 + wave * 0.2;
    float value = 0.9 + wave * 0.1;

    // 转换HSV到RGB
    vec3 rainbowColor = hsv2rgb(vec3(hue, saturation, value));

    // 设置透明度 - 让效果更透明
    float transparency = 0.6; // 调整这个值来控制透明度 (0.0 = 完全透明, 1.0 = 完全不透明)

    // 将彩虹色与原纹理混合，并应用透明度
    fragColor = vec4(rainbowColor * color.rgb, color.a * transparency) * ColorModulator;
}