#version 330 core

in vec2 textureCoords;

layout(location = 0) out vec4 out_Colour;
layout(location = 1) out vec4 bloom_Colour;

uniform sampler2D targetDepthMaskTexture;
uniform sampler2D atlasTexture;
uniform vec2 screenSize;
uniform float time;
uniform vec4 outlineParams;
uniform vec4 depthParams;
uniform vec4 noiseParams;
uniform vec4 fireColorParams;
uniform vec4 fireEdgeParams;
uniform vec4 fireWarpParams;
uniform vec4 distanceFadeParams;
uniform vec4 noiseSpriteUv;
uniform vec4 gradientSpriteUv;
uniform vec4 visibleColor;
uniform vec4 bloomColor;

const float PI2 = 6.28318530718;

vec2 atlasUv(vec4 spriteUv, vec2 localUv) {
    vec2 uv = fract(localUv);
    return mix(spriteUv.xy, spriteUv.zw, uv);
}

// 当前样式是否是闪闪果实火焰描边，只有火焰描边才启用噪声和渐变颜色。
bool isFireStyle() {
    return outlineParams.w >= 0.5;
}

// 根据目标归一化深度缩小描边半径，普通和火焰描边都使用同一套距离衰减。
float distanceScale(float targetDepth) {
    if (distanceFadeParams.w <= 0.0) {
        return 1.0;
    }

    float fadeStart = min(distanceFadeParams.x, distanceFadeParams.y - 0.0001);
    float fadeEnd = max(distanceFadeParams.y, fadeStart + 0.0001);
    float minScale = clamp(distanceFadeParams.z, 0.05, 1.0);
    float fade = smoothstep(fadeStart, fadeEnd, clamp(targetDepth, 0.0, 1.0));
    return mix(1.0, minScale, fade);
}

// 采样单通道噪声，用于推动火焰边缘断裂和 yellow_gradient 颜色坐标。
float sampleFireNoiseValue(vec2 uv) {
    if (!isFireStyle()) {
        return 0.0;
    }

    vec2 noiseUv = uv * max(noiseParams.w, 0.001) + noiseParams.xy * time;
    return texture(atlasTexture, atlasUv(noiseSpriteUv, noiseUv)).r;
}

// 采样 UE5 式边缘位移噪声，NoiseIntensity 控制位移幅度，Tilling 控制噪声密度。
vec2 sampleFireWarpOffset(vec2 uv) {
    if (!isFireStyle()) {
        return vec2(0.0);
    }
    if (fireWarpParams.x <= 0.0 || fireWarpParams.y <= 0.0) {
        return vec2(0.0);
    }

    vec2 noiseUv = uv * max(noiseParams.w, 0.001) + noiseParams.xy * time;
    vec2 noiseGB = texture(atlasTexture, atlasUv(noiseSpriteUv, noiseUv)).gb;
    vec2 centered = noiseGB * 2.0 - 1.0;
    return centered * fireWarpParams.x * max(fireWarpParams.z, 0.0);
}

// 径向采样目标深度图，可选 sampleOffset 用于火焰 warped edge。
float detectMiaoEdgeWithOffset(vec2 uv, vec2 sampleOffset) {
    vec4 centerData = texture(targetDepthMaskTexture, uv);
    float centerDepth = centerData.r;
    float centerMask = centerData.g;
    float sampleCountFloat = clamp(outlineParams.y, 1.0, 16.0);
    int sampleCount = int(sampleCountFloat);

    vec2 pixel = vec2(1.0) / max(screenSize, vec2(1.0));
    float referenceScale = max(screenSize.x / 1920.0, 0.5);
    vec2 baseRadius = pixel * max(outlineParams.x, 0.0) * referenceScale;

    float edge = 0.0;
    for (int i = 0; i < 16; i++) {
        if (i >= sampleCount) {
            break;
        }

        float angle = (float(i) + 0.5) / sampleCountFloat * PI2;
        vec2 direction = vec2(cos(angle), sin(angle));

        // 先用基础半径探测目标深度，再按目标 depth 缩小本方向的真实采样半径。
        vec2 probeUv = clamp(uv + sampleOffset + direction * baseRadius, vec2(0.0), vec2(1.0));
        float probeDepth = texture(targetDepthMaskTexture, probeUv).r;
        float targetDepth = max(centerDepth, probeDepth);
        vec2 radius = baseRadius * distanceScale(targetDepth);

        // sampleOffset 为 0 时是稳定 clean edge，传入噪声偏移时是 UE5 式 warped edge。
        vec2 sampleUv = clamp(uv + sampleOffset + direction * radius, vec2(0.0), vec2(1.0));
        vec4 sampleData = texture(targetDepthMaskTexture, sampleUv);
        float sampleDepth = sampleData.r;
        float sampleMask = sampleData.g;

        // depthEdge 对应 UE5 的深度差检测，目标外 sampleMask 为 0 时不会凭空出边。
        float depthDiff = abs(sampleDepth - centerDepth);
        float depthEdge = step(depthParams.x, depthDiff) * step(depthParams.z, sampleMask);

        // maskEdge 是当前管线的兜底：目标外部中心点为 0，周围采到目标 mask 时仍能形成稳定轮廓。
        float maskEdge = max(sampleMask - centerMask, 0.0);
        edge = max(edge, max(depthEdge, maskEdge));
    }

    return clamp(edge * max(outlineParams.z, 0.0), 0.0, 1.0);
}

// 火焰描边混合稳定 clean edge 和带噪声位移的 warped edge，恢复 UE5 式边缘抖动。
float resolveFireWarpedEdge(vec2 uv, float cleanEdge) {
    if (!isFireStyle()) {
        return cleanEdge;
    }

    vec2 warpOffset = sampleFireWarpOffset(uv);
    float warpedEdge = detectMiaoEdgeWithOffset(uv, warpOffset);
    float warpMix = clamp(fireWarpParams.y, 0.0, 1.0);

    // 以 clean edge 保底，避免 warped edge 偶发漏采时整段轮廓消失。
    return mix(cleanEdge, max(cleanEdge, warpedEdge), warpMix);
}

// 火焰只在 edge 之后做后置扰动，用噪声控制明暗跳动、边缘断裂和柔和过渡。
float shapeFireEdge(float baseEdge, float noiseValue) {
    if (!isFireStyle()) {
        return baseEdge;
    }

    float noiseStrength = max(fireEdgeParams.x, 0.0);
    float cutoff = clamp(fireEdgeParams.y, 0.0, 1.0);
    float breakup = max(fireEdgeParams.z, 0.0);
    float softness = max(fireEdgeParams.w, 0.0001);
    float centeredNoise = noiseValue * 2.0 - 1.0;

    // flicker 只调制边缘强度，不改变实际找边位置。
    float flicker = max(0.0, 1.0 + centeredNoise * noiseStrength);

    // breakupGate 让低 edge 区域按噪声产生断裂，高 edge 区域保持主体轮廓稳定。
    float breakupInput = baseEdge + noiseValue * breakup + noiseParams.z * 0.25;
    float breakupGate = smoothstep(cutoff, cutoff + softness, breakupInput);
    return clamp(baseEdge * flicker * breakupGate, 0.0, 1.0);
}

// 火焰描边使用 yellow_gradient 生成随时间流动的颜色，普通描边继续使用固定样式颜色。
vec3 resolveVisibleColor(float edge, float noiseValue) {
    if (!isFireStyle()) {
        return visibleColor.rgb;
    }

    float gradientU = fract(edge * fireColorParams.y + noiseValue * fireColorParams.z + time * fireColorParams.x);
    vec3 fireColor = texture(atlasTexture, atlasUv(gradientSpriteUv, vec2(gradientU, 0.5))).rgb;
    return fireColor * max(fireColorParams.w, 0.0);
}

void main() {
    float cleanEdge = detectMiaoEdgeWithOffset(textureCoords, vec2(0.0));
    if (!isFireStyle() && cleanEdge <= 0.0) {
        discard;
    }

    float warpedBaseEdge = resolveFireWarpedEdge(textureCoords, cleanEdge);
    float fireNoise = sampleFireNoiseValue(textureCoords);
    float edge = shapeFireEdge(warpedBaseEdge, fireNoise);
    if (edge <= 0.0) {
        discard;
    }

    vec3 visibleRgb = resolveVisibleColor(edge, fireNoise);
    vec3 bloomRgb = isFireStyle() ? visibleRgb : bloomColor.rgb;
    float visibleAlpha = clamp(visibleColor.a * edge, 0.0, 1.0);
    float bloomAlpha = clamp(bloomColor.a * edge, 0.0, 1.0);

    out_Colour = vec4(visibleRgb * visibleAlpha, visibleAlpha);
    bloom_Colour = vec4(bloomRgb * bloomAlpha, bloomAlpha);
}
