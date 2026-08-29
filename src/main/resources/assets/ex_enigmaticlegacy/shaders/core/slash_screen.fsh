#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 TexelSize;
uniform float Progress;
uniform float Time;
uniform vec2 MotionBlurVelocityPx;
uniform float MotionBlurIntensity;
uniform float MotionBlurFocusRadius;
uniform float MotionBlurContraction;
uniform float PressureWarpStrength;
uniform float ShockwaveStrength;
uniform float ShockwaveProgress;
uniform float UomStrength;
uniform float UomRate;
uniform float UomEdgeWeight;
uniform float UomContrast;
uniform float UomThresholdStrength;
uniform float UomCenterFlash;
uniform float UomInvertStrobe;
uniform float UomMonoStrobe;
uniform float UomWhiteFlash;
uniform float UomVignetteStrength;
uniform float EdgeStart;
uniform float EdgeEnd;
uniform float SlashImpactStrength;
uniform vec2 SlashImpactDirection;
uniform float SlashImpactOffsetPixels;
uniform float SlashImpactMix;
uniform float SlashImpactCenterStrength;
uniform float SlashImpactEdgeStart;
uniform float SlashImpactEdgeEnd;
uniform float SlashImpactCompression;
uniform float SlashImpactEdgeDarken;
uniform float SlashImpactContrast;

in vec2 texCoord;
out vec4 fragColor;

const int MOTION_BLUR_SAMPLE_COUNT = 16;

vec3 sampleScene(vec2 uv) {
    vec2 mirroredUv = 1.0 - abs(1.0 - mod(uv, vec2(2.0)));
    return texture(DiffuseSampler, clamp(mirroredUv, vec2(0.0), vec2(1.0))).rgb;
}

float luminance(vec3 color) {
    return dot(color, vec3(0.299, 0.587, 0.114));
}

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

float easeOutQuart(float progress) {
    float inverse = 1.0 - saturate(progress);
    return 1.0 - inverse * inverse * inverse * inverse;
}

float pressureShockwaveProfile(float delta, float frontWidth, float trailWidth) {
    float behindFront = max(-delta, 0.0);

    float sharpFront = smoothstep(-frontWidth * 0.35, frontWidth * 0.05, delta);
    sharpFront *= 1.0 - smoothstep(frontWidth * 0.05, frontWidth * 1.20, delta);
    sharpFront = sharpFront * sharpFront * (3.0 - 2.0 * sharpFront);

    float pressureWall = smoothstep(0.0, frontWidth * 0.85, behindFront);
    pressureWall *= 1.0 - smoothstep(trailWidth * 0.58, trailWidth, behindFront);

    float trailingRelief = smoothstep(trailWidth * 0.24, trailWidth * 0.54, behindFront);
    trailingRelief *= 1.0 - smoothstep(trailWidth * 0.54, trailWidth * 1.18, behindFront);

    return clamp(sharpFront * 0.82 + pressureWall * 0.54 - trailingRelief * 0.46, -0.58, 1.12);
}

float shockwaveEnergy(float progress, float ringRadius) {
    float t = saturate(progress);
    float ignition = smoothstep(0.0, 0.025, t);
    float impactFalloff = 0.30 + 0.70 * pow(1.0 - t, 1.65);
    float screenExitFade = 1.0 - smoothstep(1.08, 1.30, ringRadius);
    return ignition * impactFalloff * screenExitFade;
}

float thresholdWeight(vec3 color, float threshold) {
    float low = clamp(threshold, 0.0, 0.98);
    float high = min(1.0, low + 0.22);
    return smoothstep(low, high, luminance(color));
}

vec2 pressureWarpUv(vec2 uv) {
    float strength = clamp(PressureWarpStrength, 0.0, 1.25);
    float shockwaveStrength = clamp(ShockwaveStrength, 0.0, 1.25);
    if (strength <= 0.0001 && shockwaveStrength <= 0.0001) {
        return uv;
    }

    vec2 center = vec2(0.5);
    vec2 d = uv - center;
    vec2 pixelDelta = d / TexelSize;
    float screenRadius = max(length(vec2(0.5) / TexelSize), 1.0);
    float pixelRadius = length(pixelDelta);
    float radius = clamp(pixelRadius / screenRadius, 0.0, 1.0);

    float centerContraction = (1.0 - smoothstep(0.04, 0.76, radius)) * smoothstep(0.010, 0.18, radius);
    float edgeExpansion = smoothstep(0.28, 1.00, radius);
    float radialScale = 1.0 + strength * (centerContraction * 0.24 - edgeExpansion * 0.17);
    vec2 warpedUv = center + d * radialScale;

    if (shockwaveStrength <= 0.0001 || pixelRadius <= 0.001) {
        return warpedUv;
    }

    vec2 pixelDir = pixelDelta / pixelRadius;
    float shockProgress = clamp(ShockwaveProgress, 0.0, 1.0);
    float shockTravel = easeOutQuart(shockProgress);
    float ringRadius = mix(0.032, 1.08, shockTravel);
    float frontWidth = mix(0.018, 0.034, shockTravel);
    float trailWidth = mix(0.105, 0.190, shockTravel);
    float pressureProfile = pressureShockwaveProfile(radius - ringRadius, frontWidth, trailWidth);
    float lifeEnergy = shockwaveEnergy(shockProgress, ringRadius);
    float impactKick = 1.0 + 0.34 * (1.0 - smoothstep(0.0, 0.20, shockProgress));
    float shockPixels = pressureProfile * lifeEnergy * impactKick * shockwaveStrength * mix(86.0, 34.0, shockTravel);
    shockPixels = clamp(shockPixels, -58.0, 86.0);
    return warpedUv + pixelDir * shockPixels * TexelSize;
}

float edgeMotionMask(vec2 uv) {
    float contraction = clamp(MotionBlurContraction, 0.0, 1.0);
    vec2 toPixel = uv - vec2(0.5);
    vec2 edgeVector = abs(toPixel) * 2.0;
    float edgeDistance = clamp(max(edgeVector.x, edgeVector.y), 0.0, 1.0);
    float centerGuard = clamp(MotionBlurFocusRadius, 0.0, 0.95);
    float innerRadius = mix(0.92, centerGuard, contraction);
    return clamp((edgeDistance - innerRadius) / max(1.0 - innerRadius, 0.001), 0.0, 1.0);
}

float motionMaskAt(vec2 uv) {
    vec2 texel = TexelSize * 1.35;
    float mask = edgeMotionMask(uv);
    mask = max(mask, edgeMotionMask(uv + vec2(texel.x, 0.0)) * 0.82);
    mask = max(mask, edgeMotionMask(uv - vec2(texel.x, 0.0)) * 0.82);
    mask = max(mask, edgeMotionMask(uv + vec2(0.0, texel.y)) * 0.82);
    mask = max(mask, edgeMotionMask(uv - vec2(0.0, texel.y)) * 0.82);
    return mask;
}

vec3 edgeMotionBlurScene(vec2 uv, vec2 sourceUv, vec3 sourceColor) {
    float intensity = clamp(MotionBlurIntensity, 0.0, 1.25);
    float velocityPixels = length(MotionBlurVelocityPx);
    vec2 toCenterPixels = (vec2(0.5) - uv) / TexelSize;
    float centerDistancePixels = length(toCenterPixels);
    vec2 velocityUv = centerDistancePixels > 0.001? (toCenterPixels / centerDistancePixels) * velocityPixels * TexelSize: vec2(0.0);
    float velocityLength = length(velocityUv);
    float baseMask = motionMaskAt(uv);

    if (intensity <= 0.0001 || velocityLength <= 0.000001 || baseMask <= 0.0001) {
        return sourceColor;
    }

    vec3 colorSum = vec3(0.0);
    float weightSum = 0.0;
    float coverage = 0.0;

    for (int i = 0; i < MOTION_BLUR_SAMPLE_COUNT; ++i) {
        float t = float(i) / float(MOTION_BLUR_SAMPLE_COUNT - 1);
        vec2 sampleUv = sourceUv + velocityUv * t;
        float mask = motionMaskAt(uv + velocityUv * t);
        if (mask <= 0.001) {
            continue;
        }

        float shutter = clamp(1.0 - max(t - 0.72, 0.0) / 0.28, 0.0, 1.0);
        float weight = mask * shutter;
        colorSum += sampleScene(sampleUv) * weight;
        weightSum += weight;
        coverage += weight;
    }

    if (weightSum <= 0.0001) {
        return sourceColor;
    }

    vec3 blurred = colorSum / weightSum;
    float blendAmount = clamp(coverage * 0.22 * intensity, 0.0, 0.96);
    return mix(sourceColor, blurred, blendAmount);
}

void main() {
    vec2 center = vec2(0.5);
    vec2 aspect = vec2(1.15, 1.0);
    vec2 d = texCoord - center;
    float dist = length(d * aspect);
    vec2 pressureUv = pressureWarpUv(texCoord);
    float slashImpact = clamp(SlashImpactStrength, 0.0, 1.0);
    vec2 slashImpactDirection = SlashImpactDirection;
    float slashImpactDirectionLength = length(slashImpactDirection);
    slashImpactDirection = slashImpactDirectionLength <= 0.0001
        ? vec2(1.0, 0.0)
        : slashImpactDirection / slashImpactDirectionLength;
    float slashImpactOffsetEnvelope = sqrt(slashImpact);
    vec2 slashImpactOffset = slashImpactDirection * TexelSize * max(SlashImpactOffsetPixels, 0.0) * slashImpactOffsetEnvelope;
    vec2 slashImpactUv = center + (pressureUv - center) * (1.0 + max(SlashImpactCompression, 0.0) * slashImpactOffsetEnvelope);
    vec3 centeredScene = sampleScene(slashImpactUv);
    vec3 splitScene = vec3(
        sampleScene(slashImpactUv + slashImpactOffset).r,
        centeredScene.g,
        sampleScene(slashImpactUv - slashImpactOffset).b
    );
    float slashImpactEdge = mix(
        clamp(SlashImpactCenterStrength, 0.0, 1.0),
        1.0,
        smoothstep(SlashImpactEdgeStart, max(SlashImpactEdgeStart + 0.001, SlashImpactEdgeEnd), dist)
    );
    float slashImpactBlend = clamp(slashImpact * SlashImpactMix * slashImpactEdge, 0.0, 1.0);
    vec3 rawScene = mix(centeredScene, splitScene, slashImpactBlend);
    float slashImpactBorder = smoothstep(0.18, 0.78, dist);
    rawScene *= 1.0 - clamp(SlashImpactEdgeDarken * slashImpact * slashImpactBorder, 0.0, 0.35);
    rawScene = (rawScene - 0.5) * (1.0 + max(SlashImpactContrast, 0.0) * slashImpact) + 0.5;
    vec3 originalScene = edgeMotionBlurScene(texCoord, pressureUv, rawScene);
    float edge = smoothstep(EdgeStart, EdgeEnd, dist);
    edge = edge * edge;

    float uom = clamp(UomStrength, 0.0, 1.45);
    float edgeUom = mix(1.0, edge, clamp(UomEdgeWeight, 0.0, 1.0));
    float flashGate = step(0.50, fract(Time * UomRate));
    float fastGate = step(0.74, fract(Time * UomRate * 2.0 + 0.17));
    float bright = thresholdWeight(originalScene, 0.62);
    float impactTone = clamp(max(max(UomInvertStrobe, UomWhiteFlash), UomMonoStrobe * UomThresholdStrength), 0.0, 1.0);
    float toneDriver = max(uom, impactTone);

    vec3 color = originalScene;
    float contrast = 1.0 + UomContrast * (
        uom * (0.42 + edgeUom * 0.20 + bright * 0.42)
        + impactTone * (0.92 + edgeUom * 0.20 + bright * 0.36)
    );
    color = (color - 0.5) * contrast + 0.5;

    float centerFlash = exp(-dist * dist * 15.0) * UomCenterFlash * max(0.72 + flashGate * 0.28, impactTone * (0.95 + fastGate * 0.20));
    float impactFlash = bright * edgeUom * (uom * 0.18 + impactTone * 0.24);
    color += vec3(centerFlash + impactFlash);

    float hardStreak = 0.5 + 0.5 * sin(atan(d.y, d.x) * 18.0 + Time * UomRate * 0.72);
    float impactLum = luminance(color) + bright * 0.38 + impactFlash * 0.52 + hardStreak * edgeUom * 0.07;
    float hardThreshold = 0.52 - flashGate * 0.16 - toneDriver * 0.075 - impactTone * 0.10;
    vec3 mangaMono = vec3(step(hardThreshold, impactLum));

    float invertPulse = step(0.50, fract(Time * UomRate + 0.35));
    mangaMono = mix(mangaMono, vec3(1.0) - mangaMono, clamp(UomInvertStrobe * (0.88 + invertPulse * 0.12), 0.0, 1.0));

    float inkDriver = max(uom, impactTone * 0.75);
    float centerInk = exp(-dist * dist * 30.0) * (uom * (0.54 + flashGate * 0.34) + impactTone * 0.38);
    float verticalInk = exp(-abs(d.x) * 26.0) * smoothstep(0.04, 0.46, abs(d.y)) * inkDriver;
    float centerFadeInk = (1.0 - bright) * exp(-dist * dist * 9.0) * (uom * 0.20 + impactTone * 0.14);
    mangaMono = mix(mangaMono, vec3(0.0), clamp(centerInk + verticalInk * 0.68 + centerFadeInk, 0.0, 1.0));

    float monoImpact = UomMonoStrobe * UomThresholdStrength;
    float monoAmount = clamp(
        uom * monoImpact * (0.76 + flashGate * 0.24)
        + monoImpact * (0.82 + impactTone * 0.18),
        0.0,
        1.0
    );
    color = mix(color, mangaMono, monoAmount);

    float whiteAmount = clamp(UomWhiteFlash * (0.72 + fastGate * 0.28) * (0.30 + edgeUom * 0.34 + bright * 0.25), 0.0, 1.0);
    color = mix(color, vec3(1.0), whiteAmount);

    float burstDriver = clamp(abs(UomWhiteFlash) + abs(UomMonoStrobe) + abs(UomCenterFlash) + abs(UomThresholdStrength) + abs(UomInvertStrobe), 0.0, 1.0);
    float burstActive = smoothstep(0.001, 0.030, uom) * smoothstep(0.001, 0.050, burstDriver);
    float burstProgress = smoothstep(0.04, 0.86, Progress);
    float burstFade = 1.0 - smoothstep(0.60, 0.98, Progress);
    float horizontalDistance = abs(d.x) * 1.16;
    float verticalDistance = abs(d.y);
    float slashReach = mix(0.05, 0.72, burstProgress);
    float slashWidth = mix(0.045, 0.16, burstProgress);
    float slashBand = 1.0 - smoothstep(slashWidth, slashWidth + 0.18, verticalDistance);
    float burstWave = 1.0 - smoothstep(0.0, mix(0.08, 0.16, burstProgress), abs(horizontalDistance - slashReach));
    burstWave *= slashBand;
    float burstCore = 1.0 - smoothstep(slashReach, slashReach + 0.14, horizontalDistance);
    burstCore *= 1.0 - smoothstep(slashWidth * 1.35, slashWidth + 0.22, verticalDistance);
    burstCore *= 0.58 + (1.0 - burstProgress) * 0.24;
    float burstMask = clamp((burstWave + burstCore) * burstActive * burstFade, 0.0, 1.0);

    vec3 burstScene = originalScene;
    color = mix(color, burstScene, burstMask);

    vec2 border = abs(d) * 2.0;
    float edgeBand = max(border.x, border.y);
    float frameVignette = smoothstep(0.42, 0.98, edgeBand);
    float cornerVignette = smoothstep(0.62, 1.26, length(border));
    float distanceVignette = smoothstep(0.36, 0.92, dist);
    float vignette = clamp(max(frameVignette, max(cornerVignette * 0.92, distanceVignette * distanceVignette * 0.46)), 0.0, 1.0);

    float sceneLum = max(luminance(originalScene), luminance(color));
    float daylightBoost = smoothstep(0.36, 0.94, sceneLum);
    float breakPressure = clamp(uom * (0.70 + edgeUom * 0.30), 0.0, 1.0);
    float multiplyAmount = clamp(UomVignetteStrength * breakPressure * (0.82 + daylightBoost * 0.72) * vignette, 0.0, 0.94);
    vec3 multiplyShadow = mix(vec3(1.0), vec3(0.018, 0.016, 0.026), multiplyAmount);
    color *= multiplyShadow;

    fragColor = vec4(clamp(color, vec3(0.0), vec3(1.0)), 1.0);
}
