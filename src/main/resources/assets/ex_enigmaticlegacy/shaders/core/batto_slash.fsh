#version 330 core

in vec2 vUV;
in vec4 vColor;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 bloomColor;
uniform vec4 MaterialParams;
uniform vec4 PannerParams;
uniform sampler2D Sampler0;
uniform vec4 MainSpriteUV;
uniform vec4 TexBSpriteUV;
uniform vec4 MaskSpriteUV;

vec2 localToAtlasUV(vec2 localUV, vec4 spriteUV) {
    return spriteUV.xy + fract(localUV) * (spriteUV.zw - spriteUV.xy);
}

void main() {
    float time = MaterialParams.x;
    float bloomStrength = clamp(MaterialParams.y, 0.0, 6.0);
    float noiseStrength = clamp(MaterialParams.z, 0.0, 0.35);
    float intensity = clamp(MaterialParams.w, 0.0, 8.0);

    // Java 侧把实体生命周期进度编码到了顶点颜色 r 通道。
    float progress = clamp(vColor.r, 0.0, 1.0);
    float entityAlpha = clamp(vColor.a, 0.0, 1.0);
    vec2 uv0 = clamp(vUV, 0.0, 1.0);
    // revealU 只用于控制刀光从哪一侧出现，不影响原始纹理采样方向。
    float revealU = 1.0 - uv0.x;

    // Pipeline A：复用 radial mask 的 G 通道扰动主纹理 UV，减少一组 noise sprite 传入。
    vec2 noiseAUV = uv0 + PannerParams.xy * time;
    float noiseA = texture(Sampler0, localToAtlasUV(noiseAUV, MaskSpriteUV)).g;
    vec2 uvA = uv0 + vec2((noiseA - 0.5) * noiseStrength, 0.0);
    vec4 mainSample = texture(Sampler0, localToAtlasUV(uvA, MainSpriteUV));
    vec3 emissiveA = mainSample.rgb * vec3(0.5, 0.12, 0.5);

    // Pipeline B：radial mask 使用另一组 panner，扰动 TexB 和透明度 mask，输出互换后的蓝色自发光。
    vec2 noiseBUV = uv0 + PannerParams.zw * time;
    float noiseB = texture(Sampler0, localToAtlasUV(noiseBUV, MaskSpriteUV)).g;
    vec2 uvB = uv0 + vec2((noiseB - 0.5) * noiseStrength, 0.0);
    vec3 texB = texture(Sampler0, localToAtlasUV(uvB, TexBSpriteUV)).rgb;
    float opacityMask = texture(Sampler0, localToAtlasUV(uvB, MaskSpriteUV)).r;
    // TexB 纹理本身可能不是蓝色图，先取亮度当遮罩，再乘蓝色系数，保证蓝色层能稳定出现。
    float texBLuminance = max(max(texB.r, texB.g), texB.b);
    vec3 emissiveB = texBLuminance * vec3(0.5, 0.2, 6.0);

    // OBJ 贴图亮度继续作为刀光形状遮罩，避免 mask 单独决定整片透明度。
    float mainLuminance = max(max(mainSample.r, mainSample.g), mainSample.b);
    float shapeAlpha = smoothstep(0.02, 0.18, max(mainSample.a, mainLuminance));

    // 让刀光从头到尾出现，完全出现后随实体生命周期渐隐。
    // revealDuration 越小出现越快，生命周期加长到 40 tick 后用 0.10 维持约 4 tick 显现。
    float revealDuration = 0.10;
    float revealHead = clamp(progress / revealDuration, 0.0, 1.0);
    float revealMask = 1.0 - smoothstep(revealHead, revealHead + 0.08, revealU);
    float revealRim = (1.0 - smoothstep(0.0, 0.06, abs(revealU - revealHead))) * (1.0 - smoothstep(0.62, 0.78, progress));

    // 纵向中心更亮，内外边缘自然淡出，模拟半透明导光材质。
    float radialFade = smoothstep(0.02, 0.22, uv0.y) * (1.0 - smoothstep(0.78, 0.98, uv0.y));
    // 消失阶段复用噪声图做 dissolve，不改 40 tick 生命周期，只让后半段消散更早完成。
    float dissolveProgress = smoothstep(0.7, 0.88, progress);
    vec2 dissolveUV = uv0 * 2.0 + vec2(time * 0.12, -time * 0.08);
    float dissolveNoise = texture(Sampler0, localToAtlasUV(dissolveUV, MaskSpriteUV)).g;
    // dissolveProgress 为 0 时保留大部分像素，为 1 时逐步吃掉高噪声区域。
    float dissolveMask = smoothstep(dissolveProgress - 0.10, dissolveProgress + 0.10, dissolveNoise);
    float dissolveEdge = (1.0 - smoothstep(0.0, 0.08, abs(dissolveNoise - dissolveProgress))) * dissolveProgress;
    float alpha = opacityMask * radialFade * shapeAlpha * revealMask * entityAlpha * dissolveMask;
    if (alpha <= 0.003) {
        discard;
    }

    // Unlit 材质直接输出自发光，reveal rim 使用蓝紫色，避免边缘只剩紫色。
    vec3 rimColor = vec3(0.30, 0.58, 1.75);
    vec3 emissive = (emissiveA + emissiveB) * intensity + rimColor * revealRim + rimColor * dissolveEdge * 0.85;
    vec3 bloom = emissive * bloomStrength * (0.68 + revealRim * 1.35);

    fragColor = vec4(emissive, alpha);
    bloomColor = vec4(bloom * alpha, alpha);
}
