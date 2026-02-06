#version 150

#define M_PI 3.1415926535897932384626433832795

#moj_import <fog.glsl>

const int cosmiccount = 10;
const int cosmicoutof = 50;
const float lightmix = 0.2f;

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float time;
uniform float yaw;
uniform float pitch;
uniform float externalScale;
uniform float opacity;
uniform mat2 cosmicuvs[cosmiccount];

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;
in vec3 fPos;

out vec4 fragColor;

// HSV 转 RGB 函数
vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0/3.0, 1.0/3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

mat4 rotationMatrix(vec3 axis, float angle)
{
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;

    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
                0.0,                                0.0,                                0.0,                                1.0);
}

void main (void)
{
    vec4 mask = texture(Sampler0, texCoord0.xy);
    if (mask.a < 0.0001) discard;

    float oneOverExternalScale = 1.0 / externalScale;
    int uvtiles = 16;

    // ✅ 1. 彩虹渐变背景颜色（替代原来的深色背景）
    float bgHueSpeed = 0.001;  // 背景色相变化速度
    float bgSaturation = 0.15; // 背景饱和度（低饱和度使背景柔和）
    float bgBrightness = 0.25; // 背景亮度（保持较暗）

    // 基于时间和空间位置的背景色相
    float bgHue = mod(time * bgHueSpeed + texCoord0.x * 0.1 + texCoord0.y * 0.1, 1.0);
    vec3 bgRainbow = hsv2rgb(vec3(bgHue, bgSaturation, bgBrightness));

    // 添加脉冲效果
    float pulse = mod(time, 400.0) / 400.0;
    float pulseFactor = sin(pulse * M_PI * 2.0) * 0.1 + 1.0;

    vec4 col = vec4(bgRainbow * pulseFactor, 0.9);

    // 2. 计算观察方向
    vec4 dir = normalize(vec4(-fPos, 0.0));

    // 3. 根据玩家朝向旋转方向向量
    float sb = sin(pitch);
    float cb = cos(pitch);
    dir = normalize(vec4(dir.x, dir.y * cb - dir.z * sb, dir.y * sb + dir.z * cb, 0.0));

    float sa = sin(-yaw);
    float ca = cos(-yaw);
    dir = normalize(vec4(dir.z * sa + dir.x * ca, dir.y, dir.z * ca - dir.x * sa, 0.0));

    vec4 ray;

    // 4. 多层视差循环（16层）
    for (int i = 0; i < 16; i++) {
        int mult = 16 - i;

        // 生成伪随机数
        int j = i + 7;
        float rand1 = float((j * j * 4321 + j * 8) * 2);
        int k = j + 1;
        float rand2 = float((k * k * k * 239 + k * 37) * 3);
        float rand3 = rand1 * 347.4 + rand2 * 63.4;

        // 创建随机旋转矩阵
        vec3 axis = normalize(vec3(sin(rand1), sin(rand2), cos(rand3)));
        ray = dir * rotationMatrix(axis, mod(rand3, 2.0 * M_PI));

        // 球形UV坐标
        float rawu = 0.5 + (atan(ray.z, ray.x) / (2.0 * M_PI));
        float rawv = 0.5 + (asin(ray.y) / M_PI);

        // 缩放和偏移UV
        float scale = float(mult) * 0.5 + 1.8264;
        float u = rawu * scale * externalScale;
        float v = (rawv + time * 0.0003420 * oneOverExternalScale) * scale * 0.50 * externalScale;

        // 计算瓦片位置
        int tu = int(mod(floor(u * float(uvtiles)), float(uvtiles)));
        int tv = int(mod(floor(v * float(uvtiles)), float(uvtiles)));

        // 生成符号、旋转和翻转
        int position = ((1777541 * tu) + (7649689 * tv) + (361273 * (i+31)) + 1723609) ^ 50943779;
        int symbol = int(mod(float(position), float(cosmicoutof)));
        int rotation = int(mod(pow(float(tu), float(tv)) + float(tu) + 3.0 + float(tv*i), 8.0));
        bool flip = false;
        if (rotation >= 4) {
            rotation -= 4;
            flip = true;
        }

        // 绘制cosmic符号
        if (symbol >= 0 && symbol < cosmiccount) {
            // 局部UV坐标
            float ru = clamp(mod(u, 1.0) * float(uvtiles) - float(tu), 0.0, 1.0);
            float rv = clamp(mod(v, 1.0) * float(uvtiles) - float(tv), 0.0, 1.0);

            ru = (ru - 0.5) / 1.0 + 0.5;
            rv = (rv - 0.5) / 1.0 + 0.5;

            if (flip) ru = 1.0 - ru;

            float oru = ru;
            float orv = rv;

            // UV旋转
            if (rotation == 1) {
                oru = 1.0 - rv;
                orv = ru;
            } else if (rotation == 2) {
                oru = 1.0 - ru;
                orv = 1.0 - rv;
            } else if (rotation == 3) {
                oru = rv;
                orv = 1.0 - ru;
            }

            // 获取符号UV边界
            float umin = cosmicuvs[symbol][0][0];
            float umax = cosmicuvs[symbol][1][0];
            float vmin = cosmicuvs[symbol][0][1];
            float vmax = cosmicuvs[symbol][1][1];

            // 计算最终采样坐标
            vec2 cosmictex;
            cosmictex.x = umin * (1.0 - oru) + umax * oru;
            cosmictex.y = vmin * (1.0 - orv) + vmax * orv;

            // 采样符号纹理
            vec4 tcol = texture(Sampler0, cosmictex);

            // Alpha值（两极淡出）
            float a = tcol.r * (0.5 + (1.0 / float(mult)) * 1.0) *
                     (1.0 - smoothstep(0.20, 0.48, abs(rawv - 0.5)));

            // ✅ 符号彩虹渐变参数
            float hueSpeed = 0.00246;   // 色相变化速度
            float hueSpread = 0.0924;   // 层间色相差值
            float saturation = 0.45;     // 饱和度（比背景高）
            float brightness = 1.2;      // 亮度（更亮）

            // 计算彩虹色相（时间+层数+空间位置）
            float hue = mod(time * hueSpeed + float(i) * hueSpread + rawu * 0.05 + rawv * 0.05, 1.0);
            vec3 rainbowColor = hsv2rgb(vec3(hue, saturation, brightness));

            // 叠加彩虹颜色
            col = col + vec4(rainbowColor, 1.0) * a;
        }
    }

    // 应用光照
    vec3 shade = vertexColor.rgb * lightmix + vec3(1.0 - lightmix);
    col.rgb *= shade;

    // 应用遮罩和透明度
    col.a *= mask.r * opacity;

    // 限制颜色范围
    col = clamp(col, 0.0, 1.0);

    fragColor = linear_fog(col * ColorModulator, vertexDistance, FogStart, FogEnd, FogColor);
}