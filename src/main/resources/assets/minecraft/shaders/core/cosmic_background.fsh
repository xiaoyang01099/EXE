#version 150

uniform sampler2D Sampler0; // end_sky texture
uniform sampler2D Sampler1; // end_portal texture
uniform float Time;         // 改为 Time，匹配JSON
uniform float ColorCycle;   // 额外的颜色循环控制

in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord1;
in float time;

out vec4 fragColor;

// HSB to RGB conversion
vec3 hsb2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    // 获取基础纹理颜色
    vec4 skyColor = texture(Sampler0, texCoord0);
    vec4 portalColor = texture(Sampler1, texCoord1);

    // 创建动态的宇宙效果
    float timeScale = time * 0.02;

    // 多层次的色彩变化，结合ColorCycle
    float hue1 = fract(timeScale + texCoord0.x * 0.5 + ColorCycle * 0.1);
    float hue2 = fract(timeScale * 1.3 + texCoord0.y * 0.7 + ColorCycle * 0.2);
    float hue3 = fract(timeScale * 0.7 + length(texCoord0 - 0.5) * 2.0 + ColorCycle * 0.15);

    // 转换为RGB
    vec3 color1 = hsb2rgb(vec3(hue1, 1.0, 1.0));
    vec3 color2 = hsb2rgb(vec3(hue2, 0.8, 1.0));
    vec3 color3 = hsb2rgb(vec3(hue3, 0.9, 0.8));

    // 创建深度层次
    float depth1 = sin(time * 0.0001 + texCoord0.x * 10.0) * 0.5 + 0.5;
    float depth2 = cos(time * 0.00015 + texCoord0.y * 8.0) * 0.5 + 0.5;
    float depth3 = sin(time * 0.00008 + length(texCoord0) * 15.0) * 0.5 + 0.5;

    // 混合所有层
    vec3 mixedColor = mix(color1, color2, depth1);
    mixedColor = mix(mixedColor, color3, depth2 * 0.5);

    // 应用原始纹理作为细节
    vec3 textureDetail = mix(skyColor.rgb, portalColor.rgb, depth3);
    mixedColor = mix(mixedColor, textureDetail, 0.3);

    // 添加闪烁效果，使用ColorCycle增加变化
    float flicker = sin(time * 0.0003 + ColorCycle) * 0.1 + 0.9;
    mixedColor *= flicker;

    // 应用顶点颜色和透明度
    fragColor = vec4(mixedColor * vertexColor.rgb, vertexColor.a * 0.8);
}