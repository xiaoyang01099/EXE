#version 430 core

struct ParticleMaterialGpu {
    vec4 baseSpriteUV;
    vec4 noiseSpriteUV0;
    vec4 noiseSpriteUV1;
    vec4 topDissolveSpriteUV;
    vec4 noiseParams;
    vec4 bloomParams;
    vec4 flags;
};

layout(std430, binding = 2) readonly buffer ParticleMaterialBuffer {
    ParticleMaterialGpu materials[];
};

in vec4 vColor;
in vec2 vTexCoord;
flat in int vMaterialId;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 bloomColor;

uniform sampler2D Sampler0;
uniform float uTime;

const float OPACITY_CUTOFF = 0.003; // 最终透明度裁剪阈值，避免黑色区域写入颜色。

// 将局部 UV 映射到 atlas sprite 内，并内缩半个 texel 防止线性过滤串色。
vec2 atlasClampInset(vec2 localUv, vec4 spriteUv) {
    vec2 atlasSize = vec2(textureSize(Sampler0, 0));
    vec2 halfTexel = 0.5 / atlasSize;
    vec2 spriteMin = spriteUv.xy + halfTexel;
    vec2 spriteMax = spriteUv.zw - halfTexel;
    return mix(spriteMin, spriteMax, clamp(localUv, 0.0, 1.0));
}

void main() {
    ParticleMaterialGpu material = materials[vMaterialId];
    vec2 atlasUv = atlasClampInset(vTexCoord, material.baseSpriteUV);

    float mask = texture(Sampler0, atlasUv).r;
    float opacity = mask * vColor.a * material.bloomParams.w;
    if (opacity <= OPACITY_CUTOFF) {
        discard;
    }

    // CA0 使用预乘 alpha 输出，保持与现有 GPU 粒子混合方式一致。
    vec3 visibleColor = vColor.rgb * opacity;
    fragColor = vec4(visibleColor, opacity);

    // Bloom 保持贴图形状，halo 只轻微补亮软边，避免变成圆形光团。
    float bloomAlpha = opacity * material.bloomParams.x + mask * vColor.a * material.bloomParams.y;
    bloomColor = vec4(vColor.rgb * bloomAlpha, bloomAlpha);
}
