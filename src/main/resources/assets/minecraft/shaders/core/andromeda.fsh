#version 150

uniform float iTime;
uniform vec2  iResolution;
uniform sampler2D Sampler0;

out vec4 fragColor;

const float RETICULATION  = 10.;
const float NB_ARMS       = 4.;
const float COMPR         = .13;
const float SPEED         = .1;
const float GALAXY_R      = 1.7 / 2.;
const float BULB_R        = 0.8 / 2.5;
const float BULB_BLACK_R  = 1. / 3.0;
const vec3  GALAXY_COL    = vec3(.3, .3, 0.3);
const vec3  BULB_COL      = vec3(1.4, 2.0, 1.8);
const vec3  BULB_BLACK_COL= vec3(0., 0., 0.);
const vec3  SKY_COL       = .2 * vec3(.1, .3, .4);

#define Pi 3.1415927

float tex(vec2 uv) {
    float n = texture(Sampler0, uv).r;
    return 1. - abs(2. * n - 1.);
}

float hash21(vec2 p) {
    p = fract(p * vec2(234.34, 435.345));
    p += dot(p, p + 34.23);
    return fract(p.x * p.y);
}

float starTex(vec2 uv) {
    vec2  i = floor(uv * 80.);
    float r = hash21(i);
    return r > 0.96 ? pow((r - 0.96) / 0.04, 3.0) : 0.;
}

float noise(vec2 uv) {
    float v  = 0.;
    float a  = -SPEED * iTime;
    float co = cos(a), si = sin(a);
    mat2  M  = mat2(co, -si, si, co);
    float s  = 1.;

    for (int i = 0; i < 7; i++) {
        uv  = M * uv;
        float b = tex(uv * s);
        v  += (1. / s) * pow(b, RETICULATION);
        s  *= 2.;
    }
    return v / 2.;
}

void main() {
    vec2 fragCoord = gl_FragCoord.xy;
    vec2 uv = fragCoord.xy / iResolution.y - vec2(.8, .5);
    vec3 col;

    float rho   = length(uv);
    float ang   = atan(uv.y, uv.x);
    float shear = 2. * log(max(rho, 0.0001));
    float c = cos(shear), s = sin(shear);
    mat2  R = mat2(c, -s, s, c);

    float r;
    r = rho / GALAXY_R;      float dens       = exp(-r * r);
    r = rho / BULB_R;        float bulb       = exp(-r * r);
    r = rho / BULB_BLACK_R;  float bulb_black = exp(-r * r);

    float phase = NB_ARMS * (ang - shear);
    ang = ang - COMPR * cos(phase) + SPEED * iTime;
    uv  = rho * vec2(cos(ang), sin(ang));

    float spires = 0.3 + NB_ARMS * COMPR * sin(phase);
    dens *= .7 * spires;

    float gaz      = noise(.05 * 3.2 * R * uv);
    float gaz_trsp = pow((1. - gaz * dens), 2.);

    float stars1 = starTex(uv + .5);
    float stars2 = texture(Sampler0, fract(uv * 0.5 + 0.5)).r * 0.3;
    float stars  = pow(1. - (1. - stars1) * (1. - stars2), 5.);

    col = mix(SKY_COL,
              gaz_trsp * (1.7 * GALAXY_COL) + 1.2 * stars,
              dens);
    col = mix(col, 1.5  * BULB_COL,        0.12 * bulb);
    col = mix(col, 1.9  * BULB_BLACK_COL,  2.30 * bulb_black);

    fragColor = vec4(col, 1.);
}