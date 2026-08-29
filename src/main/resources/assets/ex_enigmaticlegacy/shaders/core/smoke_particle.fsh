#version 330 core

// 烟雾局部 UV。
in vec2 texCoord;
// Java 侧传入的可见烟雾颜色。
in vec4 visibleColor;
// Java 侧传入的 bloom 颜色和强度。
in vec4 bloomColorData;
// x=生成时间，y=生命周期，z=帧偏移，w=帧率。
in vec4 animData;
// 每个粒子的稳定随机值。
in float randomValue;
// 粒子到内部光源的距离衰减结果，负数表示沿用普通烟雾颜色流程。
in float internalLight;

// CA0 输出可见烟雾，CA1 输出 bloom source。
layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 bloomColor;

// x=客户端时间，y=可播放帧数，z=列数，w=行数。
uniform vec4 GlobalParams;
// x/y=mainFBO 尺寸，用于把 gl_FragCoord 映射到屏幕 UV。
uniform vec4 ScreenSize;
// x=alpha 阈值，y=alpha 软边范围，z=alpha 曲线，w=底部淡出高度。
uniform vec4 SmokeMaskParams;
// x=启用标记，y=soft particle 近距离，z=soft particle 远距离，w=预留。
uniform vec4 SoftParticleParams;
// 独立 smoke.png 纹理采样器，位于 textures/entity/smoke.png。
uniform sampler2D Sampler0;
// 当前 mainFBO 的深度纹理，用于 soft particle 深度交界淡出。
uniform sampler2D SceneDepthSampler;

void main() {
    float time = GlobalParams.x;
    float playableFrames = max(GlobalParams.y, 1.0);
    float columns = max(GlobalParams.z, 1.0);
    float rows = max(GlobalParams.w, 1.0);
    float age = max(time - animData.x, 0.0);
    float life = max(animData.y, 0.01);
    float lifeProgress = clamp(age / life, 0.0, 1.0);

    // 只循环前 playableFrames 帧，跳过 smoke.png 末尾过强的消散帧。
    float frame = mod(floor(age * animData.w + animData.z), playableFrames);
    float tileX = mod(frame, columns);
    float tileY = floor(frame / columns);
    vec2 frameUv = (texCoord + vec2(tileX, tileY)) / vec2(columns, rows);
    vec4 smokeSample = texture(Sampler0, clamp(frameUv, 0.0, 1.0));

    // smoke.png 现在是透明背景独立纹理，直接使用 alpha 作为主体透明度。
    float textureAlpha = smokeSample.a;
    float alphaCutoff = SmokeMaskParams.x;
    float smokeSoftness = max(SmokeMaskParams.y, 0.001);
    float smokeGamma = max(SmokeMaskParams.z, 0.001);
    float smokeMask = smoothstep(alphaCutoff, alphaCutoff + smokeSoftness, textureAlpha);
    smokeMask = pow(smokeMask, smokeGamma);
    float smokeDetail = clamp((textureAlpha - alphaCutoff) / smokeSoftness, 0.0, 1.0);
    float localRadius = distance(texCoord, vec2(0.5)) * 2.0;
    float softAlpha = smoothstep(1.0, 0.22, localRadius);
    float bottomFade = smoothstep(0.0, max(SmokeMaskParams.w, 0.001), texCoord.y);
    vec2 screenUV = gl_FragCoord.xy / max(ScreenSize.xy, vec2(1.0));
    float sceneDepth = texture(SceneDepthSampler, clamp(screenUV, 0.0, 1.0)).r;
    float depthDiff = sceneDepth - gl_FragCoord.z;
    float softParticle = mix(1.0, smoothstep(SoftParticleParams.y, SoftParticleParams.z, depthDiff), step(0.5, SoftParticleParams.x));
    float fadeIn = smoothstep(0.0, 0.22, lifeProgress);
    float fadeOut = 1.0 - smoothstep(0.74, 1.0, lifeProgress);
    float edgeBreak = mix(0.72, 1.0, fract(randomValue * 13.73 + floor(frame) * 0.017));
    float alpha = textureAlpha * smokeMask * softAlpha * bottomFade * softParticle * visibleColor.a * fadeIn * fadeOut * edgeBreak;
    // 方案 B 径向渐变：中心保持厚实，边缘降低透明度，让烟雾外沿更薄、更接近消散。
    float radialT = clamp(localRadius, 0.0, 1.0);
    float centerLight = smoothstep(0.95, 0.05, radialT);
    float edgeFade = smoothstep(0.55, 1.0, radialT);
    float radialAlpha = mix(1.0, 0.78, edgeFade);
    alpha *= radialAlpha;

    if (alpha <= 0.003) {
        discard;
    }

    // 普通烟雾继续使用原有密度和径向渐变，避免本次天雷云环光照修改影响其它烟雾效果。
    float volumeShade = mix(0.68, 1.18, pow(textureAlpha, 0.70));
    float radialShade = mix(1.0 + centerLight * 0.10, 0.82, edgeFade);
    vec3 normalVisible = visibleColor.rgb * volumeShade * radialShade;

    // internalLight 为负数时关闭假体积光照；天雷粒子即使衰减到 0 也保持启用并显示深色实体烟雾。
    float internalLightingEnabled = step(0.0, internalLight);
    float distanceLight = pow(clamp(internalLight, 0.0, 1.0), 1.35);
    float density = pow(clamp(textureAlpha, 0.0, 1.0), 0.75);

    // 中等密度区域更容易透出内部光，过厚区域进行吸收，避免高 alpha 云块直接变成纯白色。
    float thinScatter = smoothstep(0.08, 0.48, density);
    float thickAbsorption = 1.0 - smoothstep(0.62, 1.0, density) * 0.32;
    float lightScatter = clamp(distanceLight * thinScatter * thickAbsorption, 0.0, 1.0);

    // 远离内部光源时迅速压到深蓝烟雾，靠近时混合白蓝光色，制造云体被内部能量照亮的错觉。
    vec3 internalLightColor = vec3(0.82, 0.94, 1.00);
    float fakeVolumeShade = mix(0.86, 1.08, density);
    vec3 darkSmokeColor = visibleColor.rgb * 0.24 * fakeVolumeShade * radialShade;
    vec3 litSmokeColor = mix(visibleColor.rgb, internalLightColor, 0.72) * fakeVolumeShade * radialShade;
    vec3 volumeVisible = mix(darkSmokeColor, litSmokeColor, lightScatter);
    vec3 visible = mix(normalVisible, volumeVisible, internalLightingEnabled);

    // 普通烟雾沿用原 bloom；天雷云环的 bloom 随内部光照距离和烟雾密度快速衰减。
    vec3 normalEmissiveColor = mix(visible, bloomColorData.rgb, 0.35);
    float volumeEmissiveStrength = bloomColorData.a
            * pow(distanceLight, 1.20)
            * smoothstep(0.06, 0.42, density);
    vec3 volumeEmissiveColor = mix(internalLightColor, bloomColorData.rgb, 0.45);
    vec3 emissiveColor = mix(normalEmissiveColor, volumeEmissiveColor, internalLightingEnabled);
    float emissiveStrength = mix(bloomColorData.a, volumeEmissiveStrength, internalLightingEnabled);
    vec3 emissive = emissiveColor * emissiveStrength;
    fragColor = vec4(visible, alpha);
    bloomColor = vec4(emissive, alpha * emissiveStrength);
}
