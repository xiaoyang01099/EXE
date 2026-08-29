#version 430 core

in vec4 vColor;
in vec2 vCoreTexCoord;
in vec2 vBloomTexCoord;
flat in vec3 vShapeSeed;
flat in int vShapeType;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 bloomColor;

const float BLOOM_CORE_STRENGTH = 0.25; // 核心写入 Bloom source 的强度。
const float BLOOM_HALO_STRENGTH = 0.40; // 外圈光晕写入 Bloom source 的强度。
const float BLOOM_HALO_RADIUS = 0.45;   // SDF 形状边缘向外扩散的光晕半径。
const float BLOOM_EDGE_WIDTH = 0.54;    // SDF 边缘过渡宽度，越大边缘越柔。

float sdCircle(vec2 p) {
    return length(p) - 0.5;
}

float sdSquare(vec2 p) {
    vec2 d = abs(p) - vec2(0.5);
    return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0);
}

float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

vec2 randomPolygonVertex(int index, int vertexCount, vec3 seed) {
    float i = float(index);
    float n = float(vertexCount);
    float baseAngle = 6.2831853 * i / n;
    float angleJitter = (rand(seed.xy + vec2(i * 11.37, seed.z)) - 0.5) * 6.2831853 / n * 0.62;
    float radius = mix(0.32, 0.52, rand(seed.yz + vec2(i * 5.91, seed.x)));
    float angle = baseAngle + angleJitter + seed.z;
    return vec2(cos(angle), sin(angle)) * radius;
}

float sdRandomPolygon(vec2 p, vec3 seed) {
    int vertexCount = rand(seed.xz + vec2(19.19, 7.31)) < 0.5 ? 4 : 5;
    float minDistance = 10.0;
    bool inside = false;

    for (int i = 0; i < 5; i++) {
        if (i >= vertexCount) {
            break;
        }

        int nextIndex = (i + 1) % vertexCount;
        vec2 v0 = randomPolygonVertex(i, vertexCount, seed);
        vec2 v1 = randomPolygonVertex(nextIndex, vertexCount, seed);
        vec2 edge = v1 - v0;
        vec2 toPoint = p - v0;
        float h = clamp(dot(toPoint, edge) / max(dot(edge, edge), 0.0001), 0.0, 1.0);
        minDistance = min(minDistance, length(toPoint - edge * h));

        if (((v0.y > p.y) != (v1.y > p.y)) &&
            (p.x < (v1.x - v0.x) * (p.y - v0.y) / (v1.y - v0.y) + v0.x)) {
            inside = !inside;
        }
    }

    return inside ? -minDistance : minDistance;
}

float sdTriangle(vec2 p) {
    const float k = 1.7320508;
    p.x = abs(p.x) - 0.5;
    p.y = p.y + 0.2886751;
    if (p.x + k * p.y > 0.0) {
        p = vec2(p.x - k * p.y, -k * p.x - p.y) / 2.0;
    }
    p.x -= clamp(p.x, -1.0, 0.0);
    return -length(p) * sign(p.y);
}

float sdHeart(vec2 p) {
    vec2 q = vec2(p.x * 2.15, (p.y + 0.04) * 2.15);
    float x = q.x;
    float y = q.y;
    float a = x * x + y * y - 0.72;
    float heart = a * a * a - x * x * y * y * y;
    float grad = max(length(vec2(
        6.0 * x * a * a - 2.0 * x * y * y * y,
        6.0 * y * a * a - 3.0 * x * x * y * y
    )), 0.001);
    return heart / grad / 2.15;
}

float sdStar(vec2 p) {
    const float PI = 3.14159265;
    float minDistance = 10.0;
    bool inside = false;

    for (int i = 0; i < 10; i++) {
        float a0 = -PI * 0.5 + float(i) * PI / 5.0;
        float a1 = -PI * 0.5 + float(i + 1) * PI / 5.0;
        float r0 = (i % 2 == 0) ? 0.48 : 0.22;
        float r1 = ((i + 1) % 2 == 0) ? 0.48 : 0.22;
        vec2 v0 = vec2(cos(a0), sin(a0)) * r0;
        vec2 v1 = vec2(cos(a1), sin(a1)) * r1;
        vec2 edge = v1 - v0;
        vec2 toPoint = p - v0;
        float h = clamp(dot(toPoint, edge) / dot(edge, edge), 0.0, 1.0);
        minDistance = min(minDistance, length(toPoint - edge * h));

        if (((v0.y > p.y) != (v1.y > p.y)) &&
            (p.x < (v1.x - v0.x) * (p.y - v0.y) / (v1.y - v0.y) + v0.x)) {
            inside = !inside;
        }
    }

    return inside ? -minDistance : minDistance;
}

float shapeDistance(vec2 p, int shapeType) {
    if (shapeType == 11) {
        return sdCircle(p);
    }
    if (shapeType == 12) {
        return sdHeart(p);
    }

    if (shapeType == 2) {
        return sdTriangle(p);
    }
    if (shapeType == 3) {
        return sdRandomPolygon(p, vShapeSeed);
    }

    if (shapeType == 4) {
        return sdStar(p);
    }
    return sdRandomPolygon(p, vShapeSeed);
}

void main() {
    // vCoreTexCoord 使用固定粒子 UV，Bloom 范围不再通过放大顶点控制。
    vec2 p = vCoreTexCoord - vec2(0.5);
    float dist = shapeDistance(p, vShapeType);

    float aa = max(fwidth(dist), 0.001);
    float alpha = 1.0 - smoothstep(-aa, aa, dist);
    float edgeMask = smoothstep(-BLOOM_EDGE_WIDTH, 0.0, dist);

    // 心形属于曲线轮廓，单独给更小半径避免轮廓被 Bloom 完全糊掉。
    float haloRadius = vShapeType > 10 ? 0.1 : BLOOM_HALO_RADIUS;
    float haloStrength = vShapeType > 10 ? 0.25 : BLOOM_HALO_STRENGTH;

    // vBloomTexCoord 只用于四边形边缘淡化，避免 Bloom source 出现硬矩形边。
    float quadEdge = 1.0 - smoothstep(0.46, 0.5, max(abs(vBloomTexCoord.x - 0.5), abs(vBloomTexCoord.y - 0.5)));
    float haloAlpha = (1.0 - smoothstep(0.0, haloRadius, max(dist, 0.0))) * edgeMask * vColor.a * quadEdge;

    if (alpha <= 0.003 && haloAlpha <= 0.003) {
        discard;
    }

    float finalAlpha = vColor.a * alpha;
    vec4 particleColor = vec4(vColor.rgb * finalAlpha, finalAlpha);
    float bloomAlpha = finalAlpha * BLOOM_CORE_STRENGTH + haloAlpha * haloStrength;

    // CA0 stores the visible particle. CA1 adds a soft edge halo for the bloom blur pass.
    fragColor = particleColor;
    bloomColor = vec4(vColor.rgb * bloomAlpha, bloomAlpha);
}
