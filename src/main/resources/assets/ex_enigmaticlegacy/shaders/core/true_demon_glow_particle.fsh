#version 150

in vec4 vertexColor;
in vec2 texCoords;

out vec4 fragColor;

uniform float radius;
uniform float feather;
uniform float gamma;
uniform float exposure;
uniform int Layer;

const vec3 WHITE_CORE_COLOR = vec3(1.00);
const vec3 BRIGHT_MAGENTA_COLOR = vec3(0.18, 0.62, 1.00);

void main() {
    vec2 p = texCoords * 2.0 - 1.0;
    float r = length(p);

    if (r > 1.0) {
        fragColor = vec4(0.0);
        return;
    }

    float safeFeather = clamp(feather, 0.04, 0.42);
    float blackRadius = max(radius * 2.00, 0.095);
    float whiteRadius = max(radius * 4.20, 0.245);
    float blackFeather = max(0.014, blackRadius * 0.28);
    float whiteFeather = max(0.052, (whiteRadius - blackRadius) * 0.62);
    float magentaFadeStart = clamp(max(radius * 6.70, whiteRadius + whiteFeather * 1.15), whiteRadius + whiteFeather * 0.35, 0.62);
    float magentaFadeEnd = clamp(magentaFadeStart + safeFeather * 1.05, magentaFadeStart + 0.18, 0.80);

    float blackCore = 1.0 - smoothstep(blackRadius - blackFeather, blackRadius + blackFeather, r);
    float afterBlack = smoothstep(blackRadius - blackFeather * 0.35, blackRadius + blackFeather * 1.35, r);
    float beforeMagenta = 1.0 - smoothstep(whiteRadius - whiteFeather, whiteRadius + whiteFeather, r);
    float whiteCore = afterBlack * beforeMagenta;
    float magentaIn = smoothstep(whiteRadius - whiteFeather, whiteRadius + whiteFeather, r);
    float magentaOut = 1.0 - smoothstep(magentaFadeStart, magentaFadeEnd, r);
    float magentaLayer = magentaIn * magentaOut;

    float layerAlpha;
    vec3 layerColor;
    if (Layer == 0) {
        layerAlpha = blackCore * 0.96;
        layerColor = vec3(0.0);
    } else if (Layer == 1) {
        layerAlpha = whiteCore * 0.80;
        layerColor = WHITE_CORE_COLOR * exposure;
    } else {
        layerAlpha = magentaLayer * 0.56;
        layerColor = BRIGHT_MAGENTA_COLOR * exposure;
    }

    float alphaShape = pow(clamp(layerAlpha, 0.0, 1.0), 1.0 / max(gamma, 1.0e-4));
    float alphaCombined = alphaShape * vertexColor.a;
    if (alphaCombined <= 0.003) {
        fragColor = vec4(0.0);
        return;
    }

    fragColor = vec4(clamp(layerColor, 0.0, 1.0), alphaCombined);

    float n = fract(sin(dot(gl_FragCoord.xy, vec2(12.9898, 78.233))) * 43758.5453);
    fragColor.rgb += (n - 0.5) * (1.0 / 255.0) * step(1.0, float(Layer)) * step(0.02, alphaShape);
    fragColor.rgb = clamp(fragColor.rgb, 0.0, 1.0);
}
