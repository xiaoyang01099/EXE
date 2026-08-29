#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float RainbowTime;
uniform vec4 CosmicPatternUV;
uniform float CosmicYaw;
uniform float CosmicPitch;
uniform vec2 CosmicViewOffset;
uniform mat4 ProjMat;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in float gradientCoord;
in float cosmicDensity;
in float cosmicParallax;
in vec4 texProj0;
in vec3 rainbowModelPosition;

out vec4 fragColor;

float anemiaHash3D(vec3 cell, float seed) {
    // Float-domain equivalent of AnemiaSpecialEffectRender.rand(). Keeping the
    // hash in the GLSL 1.50 float domain avoids signed-integer overflow
    // differences between GPU drivers while retaining the same seeded lattice.
    return fract(sin(dot(cell, vec3(127.1, 311.7, 74.7)) + seed * 60.493) * 43758.5453123);
}

float anemiaValueNoise3D(vec3 position, float seed) {
    vec3 cell = floor(position);
    vec3 local = fract(position);
    vec3 blend = local * local * (3.0 - 2.0 * local);

    float c000 = anemiaHash3D(cell + vec3(0.0, 0.0, 0.0), seed);
    float c100 = anemiaHash3D(cell + vec3(1.0, 0.0, 0.0), seed);
    float c010 = anemiaHash3D(cell + vec3(0.0, 1.0, 0.0), seed);
    float c110 = anemiaHash3D(cell + vec3(1.0, 1.0, 0.0), seed);
    float c001 = anemiaHash3D(cell + vec3(0.0, 0.0, 1.0), seed);
    float c101 = anemiaHash3D(cell + vec3(1.0, 0.0, 1.0), seed);
    float c011 = anemiaHash3D(cell + vec3(0.0, 1.0, 1.0), seed);
    float c111 = anemiaHash3D(cell + vec3(1.0, 1.0, 1.0), seed);

    float x00 = mix(c000, c100, blend.x);
    float x10 = mix(c010, c110, blend.x);
    float x01 = mix(c001, c101, blend.x);
    float x11 = mix(c011, c111, blend.x);
    float y0 = mix(x00, x10, blend.y);
    float y1 = mix(x01, x11, blend.y);
    return mix(y0, y1, blend.z);
}

float anemiaFbm3D(vec3 position, float seed) {
    // Matches AnemiaSpecialEffectRender: four octaves, 0.32 gain and 2.32
    // spatial circularity. Normalising by the accumulated amplitude keeps the
    // hue field in the same stable 0..1 range as the entity renderer.
    float amplitude = 0.5;
    float sum = 0.0;
    float normalization = 0.0;
    vec3 frequencyPosition = position;
    float octaveSeed = seed;
    for (int octave = 0; octave < 4; ++octave) {
        sum += amplitude * anemiaValueNoise3D(frequencyPosition, octaveSeed);
        normalization += amplitude;
        amplitude *= 0.32;
        frequencyPosition *= 2.32;
        octaveSeed += 131.0;
    }
    return sum / max(normalization, 0.0001);
}

vec3 adorableArmoryLogoPalette(float phase) {
    // Cyclic palette sampled from adorablearmory_logo.png. The endpoints meet
    // at cherry pink so the animated 3D field never exposes a colour seam.
    vec3 cherryPink = vec3(1.00, 0.42, 0.79);
    vec3 blossomPink = vec3(1.00, 0.72, 0.88);
    vec3 pearlPink = vec3(1.00, 0.91, 0.98);
    vec3 lavender = vec3(0.79, 0.61, 0.98);
    vec3 violetBlue = vec3(0.52, 0.34, 1.00);

    float palettePosition = fract(phase) * 5.0;
    float blend;
    if (palettePosition < 1.0) {
        blend = smoothstep(0.0, 1.0, palettePosition);
        return mix(cherryPink, blossomPink, blend);
    } else if (palettePosition < 2.0) {
        blend = smoothstep(0.0, 1.0, palettePosition - 1.0);
        return mix(blossomPink, pearlPink, blend);
    } else if (palettePosition < 3.0) {
        blend = smoothstep(0.0, 1.0, palettePosition - 2.0);
        return mix(pearlPink, lavender, blend);
    } else if (palettePosition < 4.0) {
        blend = smoothstep(0.0, 1.0, palettePosition - 3.0);
        return mix(lavender, violetBlue, blend);
    }
    blend = smoothstep(0.0, 1.0, palettePosition - 4.0);
    return mix(violetBlue, cherryPink, blend);
}

vec3 anemia3DLogoGradient(vec3 modelPosition, float spatialScale) {
    // renderRainbowCube advances its time by tick * 0.005, i.e. 0.1 per
    // second. Use the same timing and XYZ flow vector for the bow's model-space
    // volume so adjacent faces sample one continuous three-dimensional field.
    float anemiaTime = RainbowTime * 0.10;
    vec3 volumePosition = (modelPosition - vec3(0.5)) * (0.20 * spatialScale);
    volumePosition += anemiaTime * vec3(0.80, 1.20, -0.60);

    float fieldBase = anemiaFbm3D(volumePosition, 42.0);
    float fieldPhase = fract(fieldBase + anemiaTime * 0.75);
    vec3 rainbow = adorableArmoryLogoPalette(fieldPhase);

    // The active Anemia path applies this very small local-space brightness
    // LFO after the 3D rainbow lookup. It adds motion without introducing grain.
    float brightnessLfo = sin((modelPosition.x + modelPosition.z) * 0.12
            + anemiaTime * 0.60) * 0.03;
    vec3 darker = rainbow * (1.0 + brightnessLfo);
    vec3 brighter = rainbow + (vec3(1.0) - rainbow) * brightnessLfo;
    return clamp(mix(darker, brighter, step(0.0, brightnessLfo)), 0.0, 1.0);
}

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float hash13(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.zyx + 31.32);
    return fract((p.x + p.y) * p.z);
}

vec2 orientGlyph(vec2 uv, int orientation) {
    bool flip = orientation >= 4;
    int rotation = int(mod(float(orientation), 4.0));
    if (flip) {
        uv.x = 1.0 - uv.x;
    }
    if (rotation == 1) {
        return vec2(1.0 - uv.y, uv.x);
    } else if (rotation == 2) {
        return vec2(1.0 - uv.x, 1.0 - uv.y);
    } else if (rotation == 3) {
        return vec2(uv.y, 1.0 - uv.x);
    }
    return uv;
}

float sampleCosmic8(vec2 uv) {
    vec2 atlasUv = mix(CosmicPatternUV.xy, CosmicPatternUV.zw, clamp(uv, vec2(0.0), vec2(1.0)));
    vec4 texel = texture(Sampler0, atlasUv);
    return texel.r * texel.a;
}

vec4 trueDemonCosmic(vec4 projectedPosition, float externalScale) {
    const int layerCount = 12;
    const int placementRarity = 24;
    float entityTime = RainbowTime * 20.0;
    if (projectedPosition.w <= 0.0) {
        return vec4(0.0);
    }

    vec2 projectedUv = projectedPosition.xy / projectedPosition.w;
    vec2 centered = projectedUv - vec2(0.5);
    float aspect = abs(ProjMat[1][1] / max(abs(ProjMat[0][0]), 0.0001));
    centered.x *= aspect;
    centered += CosmicViewOffset * cosmicParallax;
    float safeScale = max(externalScale, 0.0001);
    float cameraPhase = CosmicYaw * 0.09 + CosmicPitch * 0.07;

    // cosmic_8 remains a white source texture only because it is used as a
    // grayscale/alpha mask. The visible glyph is pure black, while pixels
    // outside the pattern remain fully transparent.
    float cosmicAlpha = 0.0;

    for (int i = 0; i < layerCount; ++i) {
        float layer = float(i);
        float layerDepth = 1.0 + layer * 0.16;
        float layerScale = (12.0 + layer * 1.15) * safeScale;
        vec2 seed = vec2(hash12(vec2(layer, 13.7)), hash12(vec2(29.1, layer))) * 24.0;
        float layerTime = entityTime * (0.010 + layer * 0.0009);
        vec2 drift = vec2(
                sin(layerTime * 1.37 + layer * 2.11 + cameraPhase),
                cos(layerTime * 1.09 + layer * 1.73 - cameraPhase)
        ) * (0.55 + layer * 0.035);
        vec2 cosmicPlane = centered * layerDepth * layerScale + seed + drift;
        vec2 cell = floor(cosmicPlane);
        vec2 local = fract(cosmicPlane);
        vec3 hashSeed = vec3(cell, layer);
        int symbol = int(floor(hash13(hashSeed) * float(placementRarity)));
        if (symbol != 0) {
            continue;
        }

        int orientation = int(floor(hash13(hashSeed + vec3(11.3, 29.7, 5.1)) * 8.0));
        float glyph = sampleCosmic8(orientGlyph(local, orientation));
        float edge = smoothstep(0.0, 0.06, local.x)
                * (1.0 - smoothstep(0.94, 1.0, local.x))
                * smoothstep(0.0, 0.06, local.y)
                * (1.0 - smoothstep(0.94, 1.0, local.y));
        float layerFade = 1.0 - layer / float(layerCount);
        float layerStrength = edge * (0.40 + layerFade * 0.78);
        float alpha = glyph * layerStrength;
        cosmicAlpha += alpha;
    }

    cosmicAlpha = clamp(cosmicAlpha * 0.68, 0.0, 1.0);
    return vec4(0.0, 0.0, 0.0, cosmicAlpha);
}

void main() {
    vec4 source = texture(Sampler0, texCoord0);
    vec4 normalColor = source * vertexColor * ColorModulator;
    if (normalColor.a < 0.1) {
        discard;
    }

    // Read the original atlas texel at LOD 0 for the mask. This keeps filtering
    // and mipmaps from turning nearby pale pink pixels into false #ffffff hits.
    ivec2 atlasSize = textureSize(Sampler0, 0);
    ivec2 atlasTexel = clamp(
            ivec2(floor(texCoord0 * vec2(atlasSize))),
            ivec2(0),
            atlasSize - ivec2(1)
    );
    vec4 maskSource = texelFetch(Sampler0, atlasTexel, 0);
    bool exactWhite = all(greaterThan(maskSource.rgb, vec3(0.9999))) && maskSource.a > 0.0;
    float whiteMask = exactWhite ? 1.0 : 0.0;

    vec4 color = normalColor;
    if (whiteMask > 0.001) {
        // GUI is the only context whose encoded parallax is zero. The Anemia
        // field is already much broader than the former analytic ribbons, so a
        // moderate context compensation preserves visible 3D variation without
        // squeezing it into dense stripes on the small icon.
        float guiContext = 1.0 - step(0.125, cosmicParallax);
        float rainbowSpatialScale = mix(1.0, 0.62, guiContext);
        vec3 spatialSurface = anemia3DLogoGradient(
                rainbowModelPosition, rainbowSpatialScale
        )
                * ColorModulator.rgb;
        float surfaceStrength = whiteMask * 0.94;
        color.rgb = mix(normalColor.rgb, spatialSurface, surfaceStrength);

        // The same projected/clip-space Cosmic field used by ScarletLoraAlysia.
        // Only externalScale differs per ItemDisplayContext; the coordinate
        // system itself remains identical.
        vec4 cosmicLayer = trueDemonCosmic(texProj0, cosmicDensity);
        color.rgb = mix(color.rgb, cosmicLayer.rgb, cosmicLayer.a);

        color.a = normalColor.a;
    }

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
