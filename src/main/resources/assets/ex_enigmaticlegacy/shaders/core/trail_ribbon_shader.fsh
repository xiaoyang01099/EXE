#version 330

in vec2 texCoord;
layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 bloomColor;

uniform vec4 SpriteUV0;
uniform sampler2D Sampler0;
uniform float GameTime;

vec2 remapUV(vec2 localUV, vec4 spriteUV) {
    return spriteUV.xy + localUV * (spriteUV.zw - spriteUV.xy);
}

float hash(vec2 p) {
    p = fract(p * vec2(234.34, 435.345));
    p += dot(p, p + 34.23);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + vec2(1, 0));
    float c = hash(i + vec2(0, 1));
    float d = hash(i + vec2(1, 1));
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float amp = 0.5;
    float freq = 1.0;
    for (int i = 0; i < 6; i++) {
        v += amp * noise(p * freq);
        freq *= 2.1;
        amp *= 0.5;
    }
    return v;
}


void main() {
    // ---------- 可调参数 ----------
    float uSpeed   = 3.0;   // 流速 [1~10]
    float uDensity = 7.0;   // 浓度 [1~10]
    float uSpread  = 3.0;   // 扩散 [1~10]
    // ------------------------------

    float iTime = GameTime * 1200.0;
    vec2 uv = clamp(texCoord, 0.0, 1.0);
    vec2 coluv = vec2(uv.y, 1.0 - uv.y);
    vec4 col = texture(Sampler0, remapUV(coluv, SpriteUV0));
    uv.x = uv.x - 0.5;
    uv.y = 1.0 - uv.y;

    float t = -iTime * uSpeed;

    // 横向高斯衰减（控制光束宽度）
    float spreadF = 0.12 + uSpread * 0.055;
    float xFalloff = uv.x / (spreadF + uv.y * uSpread * 0.12);
    float sideFade = exp(-xFalloff * xFalloff * (8.0 - uSpread * 0.5));

    // UV扰动（让边缘卷曲不规则）
    float distortX = fbm(vec2(uv.x * 2.5, uv.y * 3.0 - t * 1.1)) - 0.5;
    float distortY = fbm(vec2(uv.x * 2.5 + 3.7, uv.y * 3.0 - t * 0.9)) - 0.5;
    float distAmt  = 0.18 + uSpread * 0.025;
    vec2 distUV = vec2(
    uv.x + distortX * distAmt,
    uv.y + distortY * distAmt * 0.5
    );

    // 三层FBM叠加（大轮廓 + 中层细节 + 边缘湍流）
    float n1 = fbm(vec2(distUV.x * 1.8, distUV.y * 2.2 - t));
    float n2 = fbm(vec2(distUV.x * 3.5 + 1.3, distUV.y * 4.0 - t * 1.4));
    float n3 = fbm(vec2(distUV.x * 7.0 + 5.1, distUV.y * 6.0 - t * 1.8));

    float fog = n1 * 0.55 + n2 * 0.30 + n3 * 0.15;
    fog = pow(fog, 1.0 + (1.0 - uDensity * 0.08));

    // 纵向渐变（底部出现，顶部消散）
    float yFade = smoothstep(0.0, 0.08, uv.y)
    * (1.0 - smoothstep(0.6, 1.05, uv.y));
    float tailFade = smoothstep(0.05, 0.25, uv.y);

    float alpha = fog * sideFade * yFade * tailFade * (0.6 + uDensity * 0.07);
    alpha = clamp(alpha, 0.0, 1.0);

    // 中轴高亮glow
    float glow = exp(-abs(uv.x) / (0.04 + uSpread * 0.01)) * 0.3 * yFade;


    // 边缘冷色反光
    float rim = exp(-abs(uv.x) / (spreadF * 2.2)) * 0.25 * yFade;
    col.rgb += vec3(0.8, 0.92, 1.0) * rim;

    // 计算实际亮度（取 RGB 最大通道或用 luminance）
    float luminance = dot(col.rgb, vec3(0.299, 0.587, 0.114));
    float contentAlpha = smoothstep(0.02, 1.0, luminance); // 有内容才不透明

    // 加法混合输出（Shadertoy背景默认黑色，叠加即发光）
    //    fragColor = vec4(col.rgb * alpha, alpha);
    float finalAlpha = clamp(contentAlpha * alpha, 0.0, 1.0);
    float visibleAlpha = smoothstep(0.04, 0.18, finalAlpha);
    if (visibleAlpha <= 0.001) {
        discard;
    }

    float colorFade = mix(0.35, 1.0, smoothstep(0.08, 0.65, finalAlpha));
    fragColor = vec4(col.rgb * colorFade *1.3, visibleAlpha);
    // 飞剑实体拖尾只保留 CA0 加法叠色，CA1 明确输出空颜色避免贡献 Bloom。
    bloomColor = vec4(0.0);
}
