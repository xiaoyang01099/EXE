#version 150
// [VanillaCopy] position_color_tex_lightmap.fsh, changes noted

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord2;

out vec4 fragColor;

uniform float GameTime;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    if (color.a < 0.1) {
        discard;
    }

    // Botania - Enhanced color effects
    float time = GameTime * 24000;
    
    float tx = (texCoord0.x + 0.075) * 10;
    float ty = (texCoord0.y) * 10;
    
    // 为每个颜色通道创建不同的动画
    float animR = ((sin((time + sin(tx) * 200 + sin(ty) * 75) / 8.0) + 1.0) * 0.5) * 0.3;
    float animG = ((sin((time + sin(tx) * 250 + sin(ty) * 50) / 6.0) + 1.0) * 0.5) * 0.4;
    float animB = ((cos((time + sin(tx) * 50 + sin(ty) * 100) / 6.0) + 1.0) * 0.5) * 0.4;

    // 保留原始颜色并添加动画效果
    vec3 enhancedColor = color.rgb + vec3(animR, animG, animB);
    
    // 可选：轻微增强饱和度
    float luminance = dot(enhancedColor, vec3(0.299, 0.587, 0.114));
    enhancedColor = mix(vec3(luminance), enhancedColor, 1.2); // 1.2 = 饱和度增强
    
    fragColor = vec4(enhancedColor, color.a * 1.4) * ColorModulator;
}