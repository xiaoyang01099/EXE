#version 150

uniform sampler2D SceneSampler;
uniform vec2 TexelSize;
uniform vec2 SourceTexelSize;
uniform float SourcePaddingPixels;
uniform vec2 LineStart;
uniform vec2 LineEnd;
uniform float LineWidthPixels;
uniform float LineStrengthPixels;
uniform float LineAlpha;
uniform float DirectionFlip;
uniform float CenterFadeInnerRadius;
uniform float CenterFadeOuterRadius;
uniform float CenterFadeMinStrength;
uniform float VoronoiStrength;
uniform float VoronoiAge;
uniform float VoronoiSeed;
uniform float VoronoiCellScale;
uniform float VoronoiWarpPixels;
uniform float VoronoiCrackWidthPixels;
uniform float VoronoiCrackAlpha;
uniform float VoronoiCrackColor;
uniform float VoronoiCrackRevealThreshold;
uniform float VoronoiCenterInnerRadius;
uniform float VoronoiCenterOuterRadius;
uniform float VoronoiCenterFalloffPower;
uniform float VoronoiSpreadStartRadius;
uniform float VoronoiSpreadEndRadius;
uniform float VoronoiSpreadTicks;
uniform float VoronoiSpreadFeatherRadius;

in vec2 texCoord;
out vec4 fragColor;

const float BASE_SLICE_WEIGHT = 0.96;
const float LINE_TANGENT_WEIGHT = 0.055;
const float SCREEN_VERTICAL_SLICE_WEIGHT = 0.24;
const float SCREEN_VERTICAL_WAVE_WEIGHT = 0.05;
const float OFFSET_PADDING_MARGIN_PIXELS = 2.0;
const float BAND_EDGE_AA_PIXELS = 1.25;
const float SLICE_NORMAL_JITTER_WEIGHT = 0.26;
const float SLICE_TANGENT_JITTER_WEIGHT = 0.18;
const float SLICE_CROSS_SHEAR_WEIGHT = 0.13;
const float CRACK_REVEAL_CELL_JITTER = 0.055;

vec3 sampleScene(vec2 uv) {
    vec2 screenSize = 1.0 / max(TexelSize, vec2(1.0e-6));
    vec2 sourcePixel = uv * screenSize + vec2(SourcePaddingPixels);
    vec2 safeUv = clamp(sourcePixel * SourceTexelSize, SourceTexelSize * 1.5, vec2(1.0) - SourceTexelSize * 1.5);
    return texture(SceneSampler, safeUv).rgb;
}

float hash12(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7))) * 43758.5453123);
}

float valueNoise(float value, float seed) {
    float base = floor(value);
    float fraction = fract(value);
    float a = hash12(vec2(base, seed));
    float b = hash12(vec2(base + 1.0, seed));
    float blend = fraction * fraction * (3.0 - 2.0 * fraction);
    return mix(a, b, blend) * 2.0 - 1.0;
}

float voronoiHash12(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + VoronoiSeed * 0.017) * 43758.5453123);
}

vec2 voronoiRandom2(vec2 value) {
    return vec2(voronoiHash12(value + vec2(17.3, 41.9)), voronoiHash12(value + vec2(73.1, 11.7)));
}

void nearestVoronoi(vec2 p, out float nearestDistance, out float secondDistance, out vec2 nearestCell) {
    vec2 baseCell = floor(p);
    nearestDistance = 10000.0;
    secondDistance = 10000.0;
    nearestCell = baseCell;

    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 cell = baseCell + vec2(float(x), float(y));
            vec2 point = cell + voronoiRandom2(cell) * 0.82 + vec2(0.09);
            float cellDistance = length(point - p);
            if (cellDistance < nearestDistance) {
                secondDistance = nearestDistance;
                nearestDistance = cellDistance;
                nearestCell = cell;
            } else if (cellDistance < secondDistance) {
                secondDistance = cellDistance;
            }
        }
    }
}

float voronoiEffectAmount(vec2 uv) {
    float strength = clamp(VoronoiStrength, 0.0, 1.0);
    if (strength <= 0.001 || VoronoiCellScale <= 0.001) {
        return 0.0;
    }

    vec2 centeredUv = (uv - vec2(0.5)) * 2.0;
    float centerDistance = length(centeredUv);
    float outerRadius = max(VoronoiCenterOuterRadius, VoronoiCenterInnerRadius + 0.001);
    float centerMask = 1.0 - smoothstep(max(VoronoiCenterInnerRadius, 0.0), outerRadius, centerDistance);
    centerMask = pow(clamp(centerMask, 0.0, 1.0), max(VoronoiCenterFalloffPower, 0.001));

    float spreadProgress = smoothstep(0.0, max(VoronoiSpreadTicks, 0.001), max(VoronoiAge, 0.0));
    float spreadRadius = mix(max(VoronoiSpreadStartRadius, 0.0), max(VoronoiSpreadEndRadius, VoronoiSpreadStartRadius + 0.001), spreadProgress);
    float spreadMask = 1.0 - smoothstep(spreadRadius, spreadRadius + max(VoronoiSpreadFeatherRadius, 0.001), centerDistance);
    return strength * centerMask * spreadMask;
}

void main() {
    vec2 uv = texCoord;
    vec2 screenSize = 1.0 / max(TexelSize, vec2(1.0e-6));
    float minDimension = max(min(screenSize.x, screenSize.y), 1.0);
    vec3 scene = sampleScene(uv);

    vec2 lineOffset = vec2(0.0);
    float lineSampleBlend = 0.0;
    float alpha = clamp(LineAlpha, 0.0, 1.0);
    if (alpha > 0.001 && LineStrengthPixels > 0.001 && LineWidthPixels > 0.001) {
        vec2 p = uv * screenSize;
        vec2 a = LineStart * screenSize;
        vec2 b = LineEnd * screenSize;
        vec2 ab = b - a;
        float lenSq = dot(ab, ab);
        if (lenSq > 1.0) {
            float lineLength = sqrt(lenSq);
            vec2 dir = ab / lineLength;
            vec2 normal = vec2(-dir.y, dir.x);

            float t = clamp(dot(p - a, ab) / lenSq, 0.0, 1.0);
            vec2 closest = a + ab * t;
            float signedDistance = dot(p - closest, normal);
            float distance = abs(signedDistance);

            float width = max(LineWidthPixels, 1.0);
            float edgeAa = min(BAND_EDGE_AA_PIXELS, width * 0.49);
            float bandMask = 1.0 - smoothstep(width - edgeAa, width + edgeAa, distance);
            float centerDistance = length(p - screenSize * 0.5) / max(minDimension, 1.0);
            float centerFadeInner = max(CenterFadeInnerRadius, 0.0);
            float centerFadeOuter = max(CenterFadeOuterRadius, centerFadeInner + 0.001);
            float centerFade = mix(clamp(CenterFadeMinStrength, 0.0, 1.0), 1.0, smoothstep(centerFadeInner, centerFadeOuter, centerDistance));
            float influence = bandMask * alpha * centerFade;
            if (influence > 0.001) {
                float side = signedDistance < 0.0 ? -1.0 : 1.0;
                float flip = DirectionFlip < 0.0 ? -1.0 : 1.0;
                float lineCell = floor(t * lineLength / 8.0);
                float lineSeed = floor(LineStart.x * 8192.0 + LineStart.y * 4096.0 + LineEnd.x * 2048.0 + LineEnd.y * 1024.0);
                float noise = hash12(vec2(lineCell, lineSeed)) * 2.0 - 1.0;
                float wave = sin(t * lineLength * 0.31 + noise * 6.2831853);
                float bandPosition = clamp(signedDistance / width, -1.0, 1.0);
                float coarseNoise = valueNoise(t * lineLength / 24.0, lineSeed * 0.013 + 3.7);
                float fineNoise = valueNoise(t * lineLength / 9.0 + distance * 0.035, lineSeed * 0.021 + 11.3);
                float shearNoise = valueNoise(t * lineLength / 14.0 + signedDistance * 0.08, lineSeed * 0.031 + 29.9);

                float strengthPixels = LineStrengthPixels * influence;
                float normalJitter = coarseNoise * SLICE_NORMAL_JITTER_WEIGHT + fineNoise * 0.06 + bandPosition * shearNoise * SLICE_CROSS_SHEAR_WEIGHT + noise * 0.04;
                float tangentJitter = shearNoise * SLICE_TANGENT_JITTER_WEIGHT + fineNoise * 0.04;
                float normalPixels = (side * BASE_SLICE_WEIGHT + normalJitter) * flip * strengthPixels;
                float tangentPixels = (wave * LINE_TANGENT_WEIGHT + tangentJitter) * flip * strengthPixels;
                float verticalSign = (side * flip + sign(shearNoise) * 0.35);
                float verticalPixels = (verticalSign * SCREEN_VERTICAL_SLICE_WEIGHT + wave * SCREEN_VERTICAL_WAVE_WEIGHT) * strengthPixels;
                vec2 offsetPixels = normal * normalPixels + dir * tangentPixels + vec2(0.0, verticalPixels);
                float maxOffsetPixels = max(SourcePaddingPixels - OFFSET_PADDING_MARGIN_PIXELS, 1.0);
                float offsetLength = length(offsetPixels);
                if (offsetLength > maxOffsetPixels) {
                    offsetPixels *= maxOffsetPixels / offsetLength;
                }
                lineOffset = offsetPixels * TexelSize;
                lineSampleBlend = 1.0;
            }
        }
    }

    float voronoiEffect = voronoiEffectAmount(uv);
    vec2 voronoiOffset = vec2(0.0);
    float voronoiSampleBlend = 0.0;
    float crackVisibility = 0.0;
    if (voronoiEffect > 0.001) {
        vec2 cellPoint = uv * screenSize / minDimension * VoronoiCellScale;
        float nearestDistance;
        float secondDistance;
        vec2 nearestCell;
        nearestVoronoi(cellPoint, nearestDistance, secondDistance, nearestCell);

        vec2 cellRandom = voronoiRandom2(nearestCell + vec2(101.7, 313.9));
        float cellAngle = cellRandom.x * 6.2831853;
        float cellAmount = 0.35 + cellRandom.y * 1.15;
        float pulse = 0.92 + 0.08 * sin(VoronoiAge * 0.43 + voronoiHash12(nearestCell + vec2(9.0, 27.0)) * 6.2831853);
        vec2 cellDirection = vec2(cos(cellAngle), sin(cellAngle));
        voronoiOffset = cellDirection * VoronoiWarpPixels * cellAmount * pulse * voronoiEffect * TexelSize;
        voronoiSampleBlend = 1.0;

        float crackWidth = max(VoronoiCrackWidthPixels / minDimension * VoronoiCellScale, 0.0001);
        float crackMask = 1.0 - step(crackWidth, secondDistance - nearestDistance);
        float crackRevealThreshold = max(VoronoiCrackRevealThreshold, 0.0);
        float crackCellJitter = voronoiHash12(nearestCell + vec2(191.7, 5.3)) * CRACK_REVEAL_CELL_JITTER;
        float crackReveal = step(crackRevealThreshold + crackCellJitter, voronoiEffect);
        crackVisibility = crackMask * crackReveal * clamp(VoronoiCrackAlpha, 0.0, 1.0);
    }

    float sampleBlend = max(lineSampleBlend, voronoiSampleBlend);
    if (sampleBlend <= 0.001 && crackVisibility <= 0.001) {
        fragColor = vec4(scene, 1.0);
        return;
    }

    vec3 shifted = sampleScene(uv + lineOffset + voronoiOffset);
    vec3 color = mix(scene, shifted, clamp(sampleBlend, 0.0, 1.0));
    color = mix(color, vec3(clamp(VoronoiCrackColor, 0.0, 1.0)), crackVisibility);
    fragColor = vec4(clamp(color, vec3(0.0), vec3(1.0)), 1.0);
}
