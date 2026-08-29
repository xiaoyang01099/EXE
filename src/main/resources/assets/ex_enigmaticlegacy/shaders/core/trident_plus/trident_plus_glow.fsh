#version 330 core

// 天雷战戟蓄力蓝光片元 shader。
// 首版直接在普通物品渲染阶段叠加蓝色自发光层，不强依赖后处理 FBO。
// 如果当前 render target 拥有 bloom 附件，location 1 也会写入蓝色 bloom source。
in vec2 vUV;
in vec4 vColor;
in vec3 vNormal;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 bloomColor;

// 原版三叉戟纹理采样器，由 RenderType 的 TextureStateShard 绑定。
uniform sampler2D Sampler0;

// x=客户端时间秒，y=蓄力进度，z=蓝光强度，w=满蓄力标记。
uniform vec4 GlowParams;

void main() {
    // 只使用原版纹理 alpha 保留模型轮廓，RGB 不参与蓝光颜色。
    vec4 baseSample = texture(Sampler0, vUV);
    if (baseSample.a < 0.01) {
        discard;
    }

    float time = GlowParams.x;
    float chargeProgress = clamp(GlowParams.y, 0.0, 1.0);
    float glowStrength = clamp(GlowParams.z, 0.0, 2.0);
    float fullyCharged = step(0.5, GlowParams.w);

    // 蓄力中慢脉冲，满蓄力后加快脉冲，增强“充满电”的反馈。
    float chargePulse = 0.76 + 0.24 * sin(time * 8.0);
    float fullPulse = 0.74 + 0.26 * sin(time * 18.0);
    float pulse = mix(chargePulse, fullPulse, fullyCharged);

    // 使用视线空间法线做轻微边缘亮度，中心也保留基础蓝光，避免只剩轮廓。
    float facing = abs(normalize(vNormal).z);
    float rim = pow(1.0 - clamp(facing, 0.0, 1.0), 1.7);
    float bodyGlow = 0.42 + rim * 0.58;

    // 蓝光透明度随蓄力进度增长，满蓄力额外提高强度。
    float fullBoost = mix(1.0, 1.35, fullyCharged);
    float alpha = clamp(chargeProgress * glowStrength * pulse * bodyGlow * fullBoost * vColor.a * baseSample.a, 0.0, 1.0);
    if (alpha < 0.004) {
        discard;
    }

    vec3 blue = vec3(0.18, 0.65, 1.0);
    vec3 visible = blue * alpha * 1.35;
    vec3 bloom = blue * alpha * mix(2.4, 3.4, fullyCharged);

    fragColor = vec4(visible, alpha);
    bloomColor = vec4(bloom, alpha);
}
