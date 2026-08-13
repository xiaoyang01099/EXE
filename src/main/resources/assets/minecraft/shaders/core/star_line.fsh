#version 150

#define S(a, b, t) smoothstep(a, b, t)
#define NUM_LAYERS 4.0

uniform float iTime;
uniform vec2  iResolution;
uniform vec2  iPlayerPos;
uniform vec2  iPlayerVel;
uniform float iHaloRadius;
uniform float iRevealTime;
uniform float iGeometryType;

in vec2 texCoord;
in vec3 localPos;

out vec4 fragColor;

float N21(vec2 p) {
    vec3 a = fract(
        vec3(p.xyx) *
        vec3(213.897, 653.453, 253.098)
    );

    a += dot(a, a.yzx + 79.76);

    return fract((a.x + a.y) * a.z);
}

vec2 GetPos(vec2 id, vec2 offs, float t) {
    float n  = N21(id + offs);
    float n1 = fract(n * 10.0);
    float n2 = fract(n * 100.0);
    float a  = t + n;

    return offs + vec2(
        sin(a * n1),
        cos(a * n2)
    ) * 0.4;
}

float df_line(vec2 a, vec2 b, vec2 p) {
    vec2 pa = p - a;
    vec2 ba = b - a;

    float denominator = max(dot(ba, ba), 0.000001);
    float h = clamp(
        dot(pa, ba) / denominator,
        0.0,
        1.0
    );

    return length(pa - ba * h);
}

float line(vec2 a, vec2 b, vec2 uv) {
    float r1 = 0.04;
    float r2 = 0.01;

    float d  = df_line(a, b, uv);
    float d2 = length(a - b);

    float fade = S(1.5, 0.5, d2);
    fade += S(0.05, 0.02, abs(d2 - 0.75));

    return S(r1, r2, d) * fade;
}

float NetLayer(vec2 st, float n, float t) {
    vec2 id = floor(st) + n;
    st = fract(st) - 0.5;

    vec2 p[9];
    int idx = 0;

    for (float y = -1.0; y <= 1.0; y++) {
        for (float x = -1.0; x <= 1.0; x++) {
            p[idx] = GetPos(id, vec2(x, y), t);
            idx++;
        }
    }

    float m = 0.0;
    float sparkle = 0.0;

    for (int i = 0; i < 9; i++) {
        m += line(p[4], p[i], st);

        float d = length(st - p[i]);
        d = max(d, 0.001);

        float sparklePoint = 0.005 / (d * d);
        sparklePoint *= S(1.0, 0.7, d);

        float pulse = sin(
            (
                fract(p[i].x) +
                fract(p[i].y) +
                t
            ) * 5.0
        ) * 0.4 + 0.6;

        pulse = pow(max(pulse, 0.0), 20.0);
        sparklePoint *= pulse;

        sparkle += sparklePoint;
    }

    m += line(p[1], p[3], st);
    m += line(p[1], p[5], st);
    m += line(p[7], p[5], st);
    m += line(p[7], p[3], st);

    float sparklePhase =
        (sin(t + n) + sin(t * 0.1)) * 0.25 + 0.5;

    sparklePhase += pow(
        sin(t * 0.1) * 0.5 + 0.5,
        50.0
    ) * 5.0;

    m += sparkle * sparklePhase;

    return m;
}

vec3 getBaseColor() {
    float hueShift = iTime * 0.05;

    vec3 coolA = vec3(0.15, 0.40, 1.00);
    vec3 coolB = vec3(0.00, 0.90, 0.85);
    vec3 coolC = vec3(0.55, 0.10, 0.90);

    float blend1 =
        sin(hueShift) * 0.5 + 0.5;

    float blend2 =
        sin(hueShift + 2.094) * 0.5 + 0.5;

    return mix(
        coolA,
        mix(coolB, coolC, blend2),
        blend1
    );
}

vec4 renderStarField(vec2 uv, bool diskMask) {
    float dist = length(uv);

    float radialFade = 1.0;
    float centerDim = 1.0;

    if (diskMask) {
        radialFade =
            1.0 -
            pow(
                clamp(dist / 1.6, 0.0, 1.0),
                2.5
            );

        centerDim =
            1.0 -
            pow(clamp(dist, 0.0, 1.5), 2.0) *
            0.2;
    }

    float t = iTime * 0.1;
    float s = sin(t);
    float c = cos(t);

    mat2 rot = mat2(
         c, -s,
         s,  c
    );

    vec2 st = uv * rot;

    float m = 0.0;

    for (
        float i = 0.0;
        i < 1.0;
        i += 1.0 / NUM_LAYERS
    ) {
        float z = fract(t + i);
        float size = mix(15.0, 1.0, z);

        float fade =
            S(0.0, 0.6, z) *
            S(1.0, 0.8, z);

        m += fade * NetLayer(
            st * size,
            i,
            iTime
        );
    }

    float fft =
        sin(iTime * 2.0) * 0.1 + 0.2;

    float glow =
        -uv.y * fft * 1.5;

    vec3 baseCol = getBaseColor();

    vec3 col = baseCol * m;
    col += baseCol * glow * 0.3;
    col *= centerDim;

    float reveal =
        clamp(iRevealTime, 0.0, 20.0) /
        20.0;

    col *= reveal;
    col *= radialFade;

    if (diskMask) {
        float edgeBase =
            1.0 -
            abs(dist - 0.95) / 0.35;

        float edgeHighlight =
            pow(max(edgeBase, 0.0), 3.0) *
            0.12;

        edgeHighlight *=
            S(1.4, 0.6, dist) *
            S(0.4, 1.0, dist);

        col +=
            baseCol *
            edgeHighlight *
            reveal;
    }

    float contentAlpha =
        clamp(length(col) * 1.2, 0.0, 1.0);

    float baseAlpha =
        0.12 *
        reveal *
        radialFade;

    float alpha =
        max(baseAlpha, contentAlpha);

    alpha *= radialFade;

    return vec4(col, alpha);
}

vec4 renderSphereField(vec3 position) {
    vec3 direction = normalize(position);

    vec2 uvX = direction.yz;
    vec2 uvY = direction.xz;
    vec2 uvZ = direction.xy;

    vec3 weights = pow(
        abs(direction),
        vec3(4.0)
    );

    float weightSum =
        weights.x +
        weights.y +
        weights.z;

    weights /= max(weightSum, 0.0001);

    vec4 fieldX = renderStarField(uvX, false);
    vec4 fieldY = renderStarField(uvY, false);
    vec4 fieldZ = renderStarField(uvZ, false);

    vec3 color =
        fieldX.rgb * weights.x +
        fieldY.rgb * weights.y +
        fieldZ.rgb * weights.z;

    float alpha =
        fieldX.a * weights.x +
        fieldY.a * weights.y +
        fieldZ.a * weights.z;

    vec3 baseCol = getBaseColor();
    float verticalGlow =
        pow(1.0 - abs(direction.y), 3.0) *
        0.035;

    float reveal =
        clamp(iRevealTime, 0.0, 20.0) /
        20.0;

    color +=
        baseCol *
        verticalGlow *
        reveal;

    return vec4(color, alpha);
}

void main() {
    if (iGeometryType > 0.5) {
        fragColor = renderSphereField(localPos);
    } else {
        vec2 uv =
            (texCoord - vec2(0.5)) *
            2.0;

        fragColor = renderStarField(uv, true);
    }
}