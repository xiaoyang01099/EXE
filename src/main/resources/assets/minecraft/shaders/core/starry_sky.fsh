#version 150

uniform sampler2D Sampler0;
uniform float Time;
uniform vec3 BlockPos;
in vec2 texCoord;
in vec3 worldPos;
out vec4 fragColor;

// ========== 基础噪声函数 ==========

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float hash3d(vec3 p) {
    p = fract(p * vec3(0.1031, 0.1030, 0.0973));
    p += dot(p, p.yxz + 33.33);
    return fract((p.x + p.y) * p.z);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;

    for(int i = 0; i < 6; i++) {
        value += amplitude * noise(p * frequency);
        frequency *= 2.0;
        amplitude *= 0.5;
    }

    return value;
}

// ========== 旋转矩阵 ==========

mat2 rotate2D(float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return mat2(c, -s, s, c);
}

// ========== 增强星星生成（带颜色和大小变化）==========

vec3 stars(vec2 uv, float threshold, float seed) {
    vec2 gridUV = uv * 150.0;
    vec2 gridID = floor(gridUV);
    vec2 gridPos = fract(gridUV);

    float starRandom = hash(gridID + seed + BlockPos.xy);

    if(starRandom > threshold) {
        vec2 starPos = vec2(
            hash(gridID * 2.0 + BlockPos.xy),
            hash(gridID * 3.0 + BlockPos.xz)
        );
        float dist = length(gridPos - starPos);

        // 动态大小变化
        float sizeVariation = sin(Time * 0.5 + starRandom * 6.28) * 0.5 + 0.5;
        float starSize = 0.008 + hash(gridID * 4.0) * 0.015 * (0.8 + sizeVariation * 0.4);
        float brightness = smoothstep(starSize, 0.0, dist);

        // 复杂闪烁效果
        float twinkleSpeed = 1.5 + hash(gridID * 5.0) * 2.0;
        float twinkle1 = sin(Time * twinkleSpeed + starRandom * 6.28);
        float twinkle2 = sin(Time * twinkleSpeed * 0.7 + starRandom * 3.14);
        float twinkle = 0.5 + 0.3 * twinkle1 + 0.2 * twinkle2;

        // 星星颜色变化（蓝、白、黄、红）
        float colorSeed = hash(gridID * 6.0);
        vec3 starColor;
        if(colorSeed < 0.3) {
            starColor = vec3(0.7, 0.8, 1.0); // 蓝白色
        } else if(colorSeed < 0.6) {
            starColor = vec3(1.0, 1.0, 1.0); // 纯白色
        } else if(colorSeed < 0.85) {
            starColor = vec3(1.0, 0.95, 0.7); // 黄色
        } else {
            starColor = vec3(1.0, 0.7, 0.6); // 红色
        }

        // 光晕效果
        float halo = smoothstep(starSize * 3.0, 0.0, dist) * 0.3;

        return starColor * (brightness * twinkle + halo * 0.5);
    }

    return vec3(0.0);
}

// ========== 流星效果 ==========

vec3 shootingStars(vec2 uv) {
    vec3 color = vec3(0.0);

    // 多条流星
    for(int i = 0; i < 3; i++) {
        float meteorSeed = float(i) * 123.456;
        float meteorTime = Time * 0.3 + meteorSeed;

        // 流星出现的周期性
        float meteorCycle = fract(meteorTime * 0.1);
        if(meteorCycle > 0.8) continue;

        // 流星起始位置（右上角）
        vec2 meteorStart = vec2(
            0.8 + hash(vec2(meteorSeed, 0.0)) * 0.3,  // x: 0.8-1.1
            0.8 + hash(vec2(meteorSeed, 1.0)) * 0.3   // y: 0.8-1.1
        );

        // 流星运动（从右上到左下，45度角）
        float progress = fract(meteorTime);
        vec2 meteorPos = meteorStart + vec2(-progress * 1.2, -progress * 1.2);  // 改成相同速度的对角线

        // 流星方向（45度角，从右上到左下）
        vec2 meteorDir = normalize(vec2(-1.0, -1.0));
        float meteorLength = 0.15;

        // 计算点到流星轨迹的距离
        vec2 toMeteor = uv - meteorPos;
        float alongTrail = dot(toMeteor, meteorDir);
        float perpDist = length(toMeteor - meteorDir * alongTrail);

        // 流星形状（拖尾在后面）
        if(alongTrail > -meteorLength && alongTrail < 0.0) {
            float trailFade = (alongTrail + meteorLength) / meteorLength;
            float thickness = 0.003 * trailFade;
            float meteorBrightness = smoothstep(thickness, 0.0, perpDist) * trailFade;

            // 流星颜色（白色到蓝色渐变）
            vec3 meteorColor = mix(vec3(0.6, 0.8, 1.0), vec3(1.0, 1.0, 1.0), trailFade);
            color += meteorColor * meteorBrightness * 2.0;
        }
    }

    return color;
}


// ========== 旋转星系效果 ==========

vec3 galaxy(vec2 uv) {
    vec2 center = vec2(0.5, 0.5);
    vec2 toCenter = uv - center;
    float dist = length(toCenter);
    float angle = atan(toCenter.y, toCenter.x);

    // 旋转星系
    float rotation = Time * 0.15;
    float spiral = angle + rotation + dist * 8.0;

    // 星系臂
    float arm1 = sin(spiral * 3.0) * 0.5 + 0.5;
    float arm2 = sin(spiral * 3.0 + 2.094) * 0.5 + 0.5; // 120度偏移
    float arm3 = sin(spiral * 3.0 + 4.188) * 0.5 + 0.5; // 240度偏移

    float arms = max(max(arm1, arm2), arm3);

    // 星系形状
    float galaxyShape = smoothstep(0.5, 0.1, dist) * smoothstep(0.0, 0.1, dist);
    float galaxyPattern = arms * galaxyShape;

    // 添加噪声细节
    float detail = fbm(uv * 10.0 + Time * 0.02);
    galaxyPattern *= 0.7 + detail * 0.3;

    // 星系颜色（中心亮黄，外围蓝紫）
    vec3 centerColor = vec3(1.0, 0.9, 0.6);
    vec3 edgeColor = vec3(0.4, 0.3, 0.8);
    vec3 galaxyColor = mix(edgeColor, centerColor, 1.0 - dist * 2.0);

    return galaxyColor * galaxyPattern * 0.4;
}

// ========== 脉动星云 ==========

vec3 pulsatingNebula(vec2 uv) {
    // 多层移动
    vec2 movingUV1 = uv + vec2(Time * 0.01, Time * 0.005);
    vec2 movingUV2 = uv - vec2(Time * 0.008, Time * 0.012);

    // 旋转效果
    vec2 centered = uv - 0.5;
    vec2 rotated = rotate2D(Time * 0.05) * centered + 0.5;

    // 多层星云
    float nebula1 = fbm(movingUV1 * 2.0);
    float nebula2 = fbm(movingUV2 * 3.0);
    float nebula3 = fbm(rotated * 4.0);

    // 脉动效果
    float pulse = sin(Time * 0.5) * 0.5 + 0.5;
    float pulse2 = sin(Time * 0.7 + 1.57) * 0.5 + 0.5;

    // 星云形状
    vec2 center = uv - vec2(0.5, 0.5);
    float dist = length(center);
    float nebulaMask = smoothstep(0.7, 0.2, dist);

    float nebulaPattern = (nebula1 * 0.4 + nebula2 * 0.3 + nebula3 * 0.3) * nebulaMask;
    nebulaPattern *= 0.8 + pulse * 0.4;

    // 动态颜色渐变
    float colorShift = sin(Time * 0.3) * 0.5 + 0.5;
    vec3 color1 = vec3(0.1, 0.2, 0.9);  // 深蓝
    vec3 color2 = vec3(0.6, 0.2, 0.8);  // 紫色
    vec3 color3 = vec3(0.9, 0.3, 0.5);  // 粉红
    vec3 color4 = vec3(0.3, 0.8, 0.9);  // 青色

    vec3 nebulaColor;
    float mixFactor = uv.y + colorShift * 0.3;
    if(mixFactor < 0.33) {
        nebulaColor = mix(color1, color2, mixFactor * 3.0);
    } else if(mixFactor < 0.66) {
        nebulaColor = mix(color2, color3, (mixFactor - 0.33) * 3.0);
    } else {
        nebulaColor = mix(color3, color4, (mixFactor - 0.66) * 3.0);
    }

    // 添加脉动光晕
    float innerGlow = smoothstep(0.4, 0.0, dist) * pulse2 * 0.3;
    nebulaColor += vec3(0.8, 0.6, 1.0) * innerGlow;

    return nebulaColor * nebulaPattern * 0.9;
}

// ========== 动态尘埃粒子 ==========

vec3 dustParticles(vec2 uv) {
    vec3 dust = vec3(0.0);

    for(int i = 0; i < 50; i++) {
        float seed = float(i) * 0.1234;

        // 粒子位置（缓慢漂移）
        vec2 particlePos = vec2(
            fract(hash(vec2(seed, 0.0)) + Time * 0.02),
            fract(hash(vec2(seed, 1.0)) + Time * 0.015)
        );

        float dist = length(uv - particlePos);
        float size = 0.002 + hash(vec2(seed, 2.0)) * 0.003;

        // 粒子闪烁
        float flicker = sin(Time * 2.0 + seed * 6.28) * 0.5 + 0.5;

        float particle = smoothstep(size, 0.0, dist) * flicker * 0.3;
        dust += vec3(0.6, 0.7, 0.9) * particle;
    }

    return dust;
}

// ========== 深度层次效果 ==========

float depthLayers(vec2 uv) {
    vec2 center = uv - vec2(0.5, 0.5);
    float dist = length(center);

    // 多层深度
    float layer1 = smoothstep(0.8, 0.0, dist) * 0.15;
    float layer2 = smoothstep(0.6, 0.2, dist) * 0.2;
    float layer3 = smoothstep(0.4, 0.0, dist) * 0.25;

    return layer1 + layer2 + layer3;
}

// ========== 主函数 ==========

void main() {
    vec2 uv = texCoord;

    // 1. 深色背景（带微弱渐变）
    vec3 bgColor = vec3(0.01, 0.02, 0.05);
    bgColor += vec3(0.02, 0.03, 0.08) * (1.0 - length(uv - 0.5));

    // 2. 深度层次
    float depth = depthLayers(uv);
    bgColor += depth;

    // 3. 旋转星系
    vec3 galaxyColor = galaxy(uv);

    // 4. 脉动星云
    vec3 nebulaColor = pulsatingNebula(uv);

    // 5. 多层星星
    vec3 starLayer1 = stars(uv, 0.985, 0.0);
    vec3 starLayer2 = stars(uv * 0.6, 0.992, 10.0);
    vec3 starLayer3 = stars(uv * 0.4, 0.997, 20.0);
    vec3 allStars = starLayer1 * 0.9 + starLayer2 * 1.2 + starLayer3 * 1.5;

    // 6. 流星
    vec3 meteors = shootingStars(uv);

    // 7. 尘埃粒子
    vec3 dust = dustParticles(uv);

    // 8. 环境光噪声（动态）
    float ambientNoise = fbm(uv * 5.0 + Time * 0.03) * 0.06;

    // 9. 合成所有效果
    vec3 finalColor = bgColor;
    finalColor += galaxyColor;
    finalColor += nebulaColor;
    finalColor += allStars;
    finalColor += meteors;
    finalColor += dust;
    finalColor += ambientNoise;

    // 10. 色调映射
    finalColor = pow(finalColor, vec3(0.85));

    // 11. 增加饱和度
    float luminance = dot(finalColor, vec3(0.299, 0.587, 0.114));
    finalColor = mix(vec3(luminance), finalColor, 0.85);

    // 12. 边缘发光（脉动）
    vec2 edgeUV = abs(uv - 0.5) * 2.0;
    float edge = max(edgeUV.x, edgeUV.y);
    float edgePulse = sin(Time * 1.5) * 0.5 + 0.5;
    float edgeGlow = smoothstep(1.0, 0.75, edge) * (0.15 + edgePulse * 0.15);
    vec3 edgeColor = vec3(0.3, 0.2, 0.6) + vec3(0.2, 0.3, 0.4) * edgePulse;
    finalColor += edgeColor * edgeGlow;

    // 13. 整体亮度脉动
    float globalPulse = sin(Time * 0.1) * 0.05 + 0.75;
    finalColor *= globalPulse;

    // 14. 防止过曝
    finalColor = clamp(finalColor, 0.0, 1.0);

    fragColor = vec4(finalColor, 1.0);
}
