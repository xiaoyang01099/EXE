#version 150

uniform sampler2D SceneSampler;
uniform vec2 TexelSize;
uniform float Strength;
uniform float OffsetPixels;
uniform float CenterClearRadius;
uniform float EdgeFullRadius;

in vec2 texCoord;
out vec4 fragColor;

vec4 sampleScene(vec2 uv) {
    vec2 mirroredUv = 1.0 - abs(1.0 - mod(uv, vec2(2.0)));
    return texture(SceneSampler, clamp(mirroredUv, vec2(0.0), vec2(1.0)));
}

void main() {
    vec2 uv = clamp(texCoord, vec2(0.0), vec2(1.0));
    vec4 scene = sampleScene(uv);
    vec2 safeTexel = max(TexelSize, vec2(0.000001));
    vec2 pixelDelta = (uv - vec2(0.5)) / safeTexel;
    float pixelRadius = length(pixelDelta);
    float shortScreenRadius = max(0.5 / max(safeTexel.x, safeTexel.y), 1.0);
    float radialDistance = pixelRadius / shortScreenRadius;
    float innerRadius = max(0.0, CenterClearRadius);
    float outerRadius = max(innerRadius + 0.001, EdgeFullRadius);
    float radialAlpha = smoothstep(innerRadius, outerRadius, radialDistance);
    radialAlpha = radialAlpha * radialAlpha * (3.0 - 2.0 * radialAlpha);

    vec2 radialDirection = pixelRadius > 0.0001 ? pixelDelta / pixelRadius : vec2(0.0);
    vec2 splitOffset = radialDirection * max(0.0, OffsetPixels) * safeTexel * radialAlpha;
    vec3 splitScene = vec3(
        sampleScene(uv + splitOffset).r,
        scene.g,
        sampleScene(uv - splitOffset).b
    );

    float effectAlpha = clamp(Strength, 0.0, 1.0) * radialAlpha;
    vec3 color = mix(scene.rgb, splitScene, effectAlpha);
    fragColor = vec4(clamp(color, vec3(0.0), vec3(1.0)), scene.a);
}
