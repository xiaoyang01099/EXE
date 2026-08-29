#version 330 core

in vec2 textureCoords;

out vec4 out_Colour;

uniform sampler2D colourTexture;
uniform sampler2D mainTexture;
uniform sampler2D bloomTexture;
uniform float bloomStrength;
// DimensionEffect: x=蓝色重影强度，y=灰化强度，z=Voronoi碎片错位强度，w=碎片推进进度。
uniform vec4 DimensionEffect;
// DimensionGlass: x=视觉随机种子，y=兼容旧白闪参数且当前为0，z=径向模糊强度，w=灰白对比度提升。
uniform vec4 DimensionGlass;
// DimensionField: x=RGB色散强度，y=边缘暗角强度，z=领域壁强度，w=Voronoi碎片错位基础强度。
uniform vec4 DimensionField;

const float contrast = 0.3;

// 简单哈希，用于生成稳定的碎片强度和下落延迟。
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7)) + DimensionGlass.x * 0.013) * 43758.5453);
}

// 二维哈希，用于给 Voronoi 碎片生成特征点和独立位移方向。
vec2 hash2(vec2 p) {
    vec2 q = vec2(
        dot(p, vec2(127.1, 311.7)),
        dot(p, vec2(269.5, 183.3))
    );
    return fract(sin(q + DimensionGlass.x * 0.013) * 43758.5453);
}

// 扰乱碎片采样空间，只使用线性旋转和缩放，保持碎片边缘更接近直线斩切。
vec2 shatterUv(vec2 uv) {
    vec2 centered = uv - vec2(0.5);
    float angle = 0.48 + hash(vec2(DimensionGlass.x, 91.0)) * 0.55;
    float s = sin(angle);
    float c = cos(angle);
    vec2 rotated = vec2(centered.x * c - centered.y * s, centered.x * s + centered.y * c);
    return rotated * vec2(1.36, 0.78) + vec2(0.5);
}

// 根据 UV 距离屏幕中心的方向计算色散采样偏移，蓝紫领域阶段让画面边缘轻微错色。
vec3 sampleChromatic(vec2 uv, float strength) {
    vec2 fromCenter = uv - vec2(0.5);
    vec2 dir = normalize(fromCenter + vec2(0.0001));
    float edge = smoothstep(0.05, 0.82, length(fromCenter) * 1.42);
    vec2 offset = dir * edge * strength * 0.006;
    float r = texture(colourTexture, clamp(uv + offset, 0.001, 0.999)).r;
    float g = texture(colourTexture, uv).g;
    float b = texture(colourTexture, clamp(uv - offset * 1.25, 0.001, 0.999)).b;
    return vec3(r, g, b);
}

// 计算屏幕空间 Voronoi 碎片，x=边界距离，yz=碎片 id，w=到碎片中心距离。
vec4 voronoiShard(vec2 uv, float density) {
    vec2 gridUv = uv * density;
    vec2 baseCell = floor(gridUv);
    vec2 local = fract(gridUv);
    float nearestDist = 999.0;
    float secondDist = 999.0;
    vec2 nearestCell = vec2(0.0);

    // 只查 3x3 邻域，短窗口后处理里成本可控。
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 offsetCell = vec2(float(x), float(y));
            vec2 cellId = baseCell + offsetCell;
            vec2 point = offsetCell + hash2(cellId) - local;
            float dist = dot(point, point);
            if (dist < nearestDist) {
                secondDist = nearestDist;
                nearestDist = dist;
                nearestCell = cellId;
            } else if (dist < secondDist) {
                secondDist = dist;
            }
        }
    }

    float edgeDistance = sqrt(secondDist) - sqrt(nearestDist);
    return vec4(edgeDistance, nearestCell, sqrt(nearestDist));
}

// 混合多层 Voronoi，打破单一密度带来的整齐摆放感。
vec4 selectVoronoiShard(vec2 uv) {
    vec2 warpedUv = shatterUv(uv);
    vec2 seedOffset = hash2(vec2(DimensionGlass.x * 0.001, 13.7)) * 0.17;
    vec4 bigShard = voronoiShard(warpedUv + seedOffset, 7.0);
    vec4 midShard = voronoiShard(warpedUv * 1.31 + vec2(0.31, -0.23), 12.0);
    vec4 smallShard = voronoiShard(warpedUv * 0.92 + vec2(-0.19, 0.37), 18.0);
    float pick = hash(bigShard.yz + floor(uv * 3.0) + vec2(9.0));
    return pick < 0.36 ? bigShard : (pick < 0.78 ? midShard : smallShard);
}

// 根据 Voronoi 边界距离生成透明扭曲遮罩，不再把边缘画成明显白线。
float cutEdgeMask(vec2 uv) {
    float cut = clamp(DimensionEffect.z, 0.0, 1.0);
    if (cut <= 0.001) {
        return 0.0;
    }
    vec4 shard = selectVoronoiShard(uv);
    float progress = clamp(DimensionEffect.w, 0.0, 1.0);
    float edgeWidth = mix(0.014, 0.008, progress);
    float edgeLife = 1.0 - smoothstep(0.96, 1.0, progress);
    return (1.0 - smoothstep(0.001, edgeWidth, shard.x)) * cut * edgeLife;
}

// 计算单块碎片独立下落进度，每块有不同的启动延迟和下落时长，避免整屏同步拖拽。
float cutFallProgress(vec2 uv) {
    float cut = clamp(DimensionEffect.z, 0.0, 1.0);
    if (cut <= 0.001) {
        return 0.0;
    }
    float progress = DimensionEffect.w;
    vec4 shard = selectVoronoiShard(uv);
    float fallStart = 0.98 + hash(shard.yz + 23.0) * 0.10;
    float fallDuration = 0.16 + hash(shard.yz + 61.0) * 0.18;
    return clamp((progress - fallStart) / fallDuration, 0.0, 1.0);
}

// 根据 Voronoi 碎片 id 做终结下落，同一碎片内部保持连续；斩击结束前不做扭动位移。
vec2 applyCutUv(vec2 uv) {
    float cut = clamp(DimensionEffect.z, 0.0, 1.0);
    if (cut <= 0.001) {
        return uv;
    }
    float strength = cut * clamp(DimensionField.w, 0.0, 1.0);
    vec4 shard = selectVoronoiShard(uv);
    float localFall = cutFallProgress(uv);
    float fallEase = localFall * localFall * (3.0 - 2.0 * localFall);
    float sideDrift = (hash(shard.yz + 7.7) - 0.5) * (0.012 + hash(shard.yz + 81.0) * 0.030) * fallEase;
    float fallDistance = (0.18 + hash(shard.yz + 44.6) * 0.46) * fallEase;
    vec2 offset = vec2(sideDrift, fallDistance) * strength;
    return clamp(uv + offset, 0.001, 0.999);
}

// 对原画面做短窗口径向拉伸采样，用于灰白爆发瞬间的冲击感。
vec3 sampleZoomBlur(vec2 uv, float strength) {
    if (strength <= 0.001) {
        return texture(colourTexture, uv).rgb;
    }
    vec2 center = vec2(0.5);
    vec2 dir = uv - center;
    vec3 color = vec3(0.0);
    color += texture(colourTexture, clamp(uv - dir * strength * 0.012, 0.001, 0.999)).rgb * 0.20;
    color += texture(colourTexture, clamp(uv - dir * strength * 0.026, 0.001, 0.999)).rgb * 0.20;
    color += texture(colourTexture, clamp(uv - dir * strength * 0.044, 0.001, 0.999)).rgb * 0.18;
    color += texture(colourTexture, clamp(uv - dir * strength * 0.066, 0.001, 0.999)).rgb * 0.16;
    color += texture(colourTexture, clamp(uv - dir * strength * 0.092, 0.001, 0.999)).rgb * 0.14;
    color += texture(colourTexture, uv).rgb * 0.12;
    return color;
}

void main(void){

    vec2 effectUv = applyCutUv(textureCoords);
    float cut = clamp(DimensionEffect.z, 0.0, 1.0);
    float cutProgress = clamp(DimensionEffect.w, 0.0, 1.0);
    float edgeMask = cutEdgeMask(textureCoords);
    float shake = smoothstep(0.78, 0.84, cutProgress) * (1.0 - smoothstep(0.90, 0.96, cutProgress)) * cut;
    vec2 shakeOffset = (hash2(vec2(floor(cutProgress * 50.0), DimensionGlass.x)) * 2.0 - 1.0) * shake * 0.012;
    effectUv = clamp(effectUv + shakeOffset, 0.001, 0.999);
    float chromatic = clamp(DimensionField.x, 0.0, 1.0);
    float zoom = clamp(DimensionGlass.z, 0.0, 1.0);
    vec3 baseScene = chromatic > 0.001 ? sampleChromatic(effectUv, chromatic) : texture(colourTexture, effectUv).rgb;
    baseScene = mix(baseScene, sampleZoomBlur(effectUv, zoom), zoom * 0.72);
    vec4 mcColor = vec4(baseScene, texture(colourTexture, effectUv).a);
    vec4 mainColor = texture(mainTexture, effectUv);
    vec3 bloomColor = texture(bloomTexture, effectUv).rgb * bloomStrength;

    float alpha = clamp(mainColor.a, 0.0, 1.0);
    // mainTexture stores premultiplied particle color, so composite it before adding bloom light.
    vec3 blendedColor = mainColor.rgb + mcColor.rgb * (1.0 - alpha) + bloomColor;

    // 蓝色重影阶段：沿屏幕中心向外做多层偏移采样，避免只剩单纯染蓝。
    float blue = clamp(DimensionEffect.x, 0.0, 1.0);
    if (blue > 0.001) {
        vec2 fromCenter = textureCoords - vec2(0.5);
        vec2 ghostDir = normalize(fromCenter + vec2(0.0001));
        vec3 ghostA = texture(colourTexture, clamp(effectUv + ghostDir * 0.014 + vec2(0.006, -0.003), 0.001, 0.999)).rgb;
        vec3 ghostB = texture(colourTexture, clamp(effectUv - ghostDir * 0.024 + vec2(-0.010, 0.005), 0.001, 0.999)).rgb;
        vec3 ghostC = texture(colourTexture, clamp(effectUv + vec2(-ghostDir.y, ghostDir.x) * 0.018, 0.001, 0.999)).rgb;
        vec3 ghost = (ghostA * 0.45 + ghostB * 0.35 + ghostC * 0.20) * vec3(0.50, 0.78, 1.18);
        vec3 tinted = blendedColor * vec3(0.72, 0.88, 1.18) + ghost * 0.58;
        blendedColor = mix(blendedColor, tinted, blue);
    }

    // 蓝紫领域暗角和领域壁：边缘压暗，叠加一圈偏蓝紫的半透明弧面。
    float vignette = clamp(DimensionField.y, 0.0, 1.0);
    float wall = clamp(DimensionField.z, 0.0, 1.0);
    vec2 centeredUv = textureCoords - vec2(0.5);
    float radial = length(centeredUv) * 1.42;
    if (vignette > 0.001) {
        float edgeDark = smoothstep(0.36, 0.94, radial);
        blendedColor = mix(blendedColor, blendedColor * vec3(0.58, 0.68, 1.05) + vec3(0.04, 0.08, 0.26), edgeDark * vignette * 0.62);
    }
    if (wall > 0.001) {
        float wallMask = smoothstep(0.58, 0.88, radial) * (1.0 - smoothstep(0.92, 1.08, radial));
        blendedColor += vec3(0.10, 0.34, 0.86) * wallMask * wall * 0.55;
    }

    // 终结阶段：降低饱和度，配合 Voronoi 碎片透明扭曲边缘形成被切碎的压迫感。
    float gray = clamp(DimensionEffect.y, 0.0, 1.0);
    if (gray > 0.001) {
        float luma = dot(blendedColor, vec3(0.299, 0.587, 0.114));
        blendedColor = mix(blendedColor, vec3(luma) * vec3(0.68, 0.72, 0.78), gray);
    }

    // 灰白爆发高对比，让中心斩碎阶段更接近参考视频的黑白冲击。
    float contrastBoost = clamp(DimensionGlass.w, 0.0, 1.0);
    if (contrastBoost > 0.001) {
        blendedColor = mix(blendedColor, (blendedColor - vec3(0.5)) * 1.85 + vec3(0.5), contrastBoost);
    }

    // Voronoi 边界只做轻微透明暗边，终结窗口再执行碎片下落。
    blendedColor = mix(blendedColor, blendedColor * vec3(0.80, 0.86, 0.96), edgeMask * 0.10);
    float fallFade = smoothstep(0.72, 1.0, cutFallProgress(textureCoords)) * cut;
    blendedColor = mix(blendedColor, blendedColor * vec3(0.44, 0.46, 0.52), fallFade * 0.30);

    // 兼容旧白闪参数；次元斩当前配置为 0，终结不再发白上升。
    float flash = clamp(DimensionGlass.y, 0.0, 1.0);
    blendedColor = mix(blendedColor, vec3(1.0), flash * 0.58);
    out_Colour = vec4(blendedColor, mcColor.a);
//    out_Colour = mainColor;
//    out_Colour = vec4((mcColor.rgb - vec3(0.5)) * (3 + contrast) + vec3(0.5), mcColor.a);
//    out_Colour = mcColor;
}
