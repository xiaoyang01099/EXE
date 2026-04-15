#version 150

// ==================== Uniforms ====================
uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform sampler2D NoiseSampler;
uniform float Time;
uniform vec2 ScreenSize;
uniform vec3 BlackHolePos;
uniform float BlackHoleStrength;
uniform float BlackHoleRadius;
uniform float BlackHoleMass;
uniform float AccretionDiskIntensity;
uniform float AccretionDiskTemperature;
uniform vec3 ViewerVelocity;
uniform vec3 BlackHoleVelocity;
uniform float SpinParameter;
uniform float InclinationAngle;
uniform bool EnableLenseThirring;
uniform bool EnableFrameDragging;
uniform float PlanckLength;
uniform float CosmologicalConstant;

// ==================== Inputs ====================
in vec2 texCoord0;
in vec4 vertexColor;

// ==================== Outputs ====================
out vec4 fragColor;

// ==================== 物理常数 ====================
#define GRAVITATIONAL_CONSTANT 6.67430e-11
#define SPEED_OF_LIGHT 299792458.0
#define SOLAR_MASS 1.98847e30
#define PLANCK_CONSTANT 6.62607015e-34
#define BOLTZMANN_CONSTANT 1.380649e-23
#define SCHWARZSCHILD_CONSTANT (2.0 * GRAVITATIONAL_CONSTANT / (SPEED_OF_LIGHT * SPEED_OF_LIGHT))

// ==================== 数学常数 ====================
#define PI 3.14159265358979323846
#define TWO_PI 6.28318530717958647692
#define INV_PI 0.31830988618379067154
#define GOLDEN_RATIO 1.61803398874989484820

// ==================== 类型定义 ====================
struct Ray {
    vec3 origin;
    vec3 direction;
    float wavelength;
};

struct Photon {
    vec3 position;
    vec3 momentum;
    float energy;
    vec3 color;
    bool absorbed;
};

struct SpacetimeMetric {
    float g_tt;
    float g_rr;
    float g_theta_theta;
    float g_phi_phi;
    float g_t_phi;
    float a;
};

// ==================== 高级噪声函数 ====================
uint hash(uint x) {
    x = ((x >> 16u) ^ x) * 0x45d9f3bu;
    x = ((x >> 16u) ^ x) * 0x45d9f3bu;
    x = (x >> 16u) ^ x;
    return x;
}

float floatHash(uint x) {
    return float(hash(x)) / 4294967295.0;
}

float perlinNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);

    float a = floatHash(uint(i.x) + uint(i.y) * 113u);
    float b = floatHash(uint(i.x + 1.0) + uint(i.y) * 113u);
    float c = floatHash(uint(i.x) + uint(i.y + 1.0) * 113u);
    float d = floatHash(uint(i.x + 1.0) + uint(i.y + 1.0) * 113u);

    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float perlinNoise3D(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    vec3 u = f * f * (3.0 - 2.0 * f);

    float a = floatHash(uint(i.x) + uint(i.y) * 113u + uint(i.z) * 129u);
    float b = floatHash(uint(i.x + 1.0) + uint(i.y) * 113u + uint(i.z) * 129u);
    float c = floatHash(uint(i.x) + uint(i.y + 1.0) * 113u + uint(i.z) * 129u);
    float d = floatHash(uint(i.x + 1.0) + uint(i.y + 1.0) * 113u + uint(i.z) * 129u);
    float e = floatHash(uint(i.x) + uint(i.y) * 113u + uint(i.z + 1.0) * 129u);
    float f1 = floatHash(uint(i.x + 1.0) + uint(i.y) * 113u + uint(i.z + 1.0) * 129u);
    float g = floatHash(uint(i.x) + uint(i.y + 1.0) * 113u + uint(i.z + 1.0) * 129u);
    float h = floatHash(uint(i.x + 1.0) + uint(i.y + 1.0) * 113u + uint(i.z + 1.0) * 129u);

    float x1 = mix(a, b, u.x);
    float x2 = mix(c, d, u.x);
    float y1 = mix(x1, x2, u.y);

    float x3 = mix(e, f1, u.x);
    float x4 = mix(g, h, u.x);
    float y2 = mix(x3, x4, u.y);

    return mix(y1, y2, u.z);
}

float fbm(vec2 p, int octaves) {
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;

    for (int i = 0; i < octaves; i++) {
        value += amplitude * perlinNoise(p * frequency);
        amplitude *= 0.5;
        frequency *= 2.0;
    }

    return value;
}

float turbulenceNoise(vec2 p, int octaves) {
    float value = 0.0;
    float amplitude = 1.0;

    for (int i = 0; i < octaves; i++) {
        value += amplitude * abs(perlinNoise(p));
        amplitude *= 0.5;
        p *= 2.0;
    }

    return value;
}

// ==================== 广义相对论计算 ====================
float schwarzschildRadius(float mass) {
    return (2.0 * GRAVITATIONAL_CONSTANT * mass * SOLAR_MASS) / (SPEED_OF_LIGHT * SPEED_OF_LIGHT);
}

vec2 kerrHorizons(float mass, float spin) {
    float M = schwarzschildRadius(mass) * 0.5;
    float a = spin * M;
    float r_plus = M + sqrt(max(M * M - a * a, 0.0));
    float r_minus = M - sqrt(max(M * M - a * a, 0.0));
    return vec2(r_plus, r_minus);
}

SpacetimeMetric kerrMetric(float r, float theta, float mass, float spin) {
    SpacetimeMetric metric;
    float M = schwarzschildRadius(mass) * 0.5;
    float a = spin * M;
    float Delta = r * r - 2.0 * M * r + a * a;
    float Sigma = r * r + a * a * cos(theta) * cos(theta);
    float A = (r * r + a * a) * (r * r + a * a) - Delta * a * a * sin(theta) * sin(theta);

    metric.g_tt = -(1.0 - (2.0 * M * r) / Sigma);
    metric.g_rr = Sigma / max(Delta, 0.001);
    metric.g_theta_theta = Sigma;
    metric.g_phi_phi = (A / Sigma) * sin(theta) * sin(theta);
    metric.g_t_phi = -(2.0 * M * r * a * sin(theta) * sin(theta)) / Sigma;
    metric.a = a;

    return metric;
}

vec3 christoffelSymbol(vec3 position, float mass, float spin) {
    float r = length(position);
    float theta = acos(clamp(position.z / max(r, 0.001), -1.0, 1.0));

    SpacetimeMetric metric = kerrMetric(r, theta, mass, spin);

    float M = schwarzschildRadius(mass) * 0.5;
    float Gamma_rr_r = M / max(r * r - 2.0 * M * r, 0.001);
    float Gamma_theta_theta_r = -r;
    float Gamma_phi_phi_r = -r * sin(theta) * sin(theta);
    float Gamma_tt_r = M * (r - 2.0 * M) / max(r * r * r, 0.001);

    return vec3(Gamma_tt_r, Gamma_rr_r, Gamma_phi_phi_r) * 0.01;
}

vec3 geodesicEquation(vec3 position, vec3 velocity, float mass, float spin, float dt) {
    float r = length(position);
    float rs = schwarzschildRadius(mass);

    if (r < rs * 1.01) {
        return vec3(0.0);
    }

    vec3 Gamma = christoffelSymbol(position, mass, spin);

    vec3 acceleration = vec3(0.0);
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            for (int k = 0; k < 3; k++) {
                float gamma_component = Gamma[i];
                acceleration[i] -= gamma_component * velocity[j] * velocity[k];
            }
        }
    }

    vec3 newVelocity = velocity + acceleration * dt;
    vec3 newPosition = position + newVelocity * dt;

    return newPosition;
}

// ==================== 波长到颜色转换（必须在 rayMarch 之前）====================
vec3 wavelengthToRGB(float wavelength) {
    wavelength = clamp(wavelength * 1e9, 380.0, 780.0);

    vec3 color;

    if (wavelength < 440.0) {
        color.r = -(wavelength - 440.0) / (440.0 - 380.0);
        color.g = 0.0;
        color.b = 1.0;
    } else if (wavelength < 490.0) {
        color.r = 0.0;
        color.g = (wavelength - 440.0) / (490.0 - 440.0);
        color.b = 1.0;
    } else if (wavelength < 510.0) {
        color.r = 0.0;
        color.g = 1.0;
        color.b = -(wavelength - 510.0) / (510.0 - 490.0);
    } else if (wavelength < 580.0) {
        color.r = (wavelength - 510.0) / (580.0 - 510.0);
        color.g = 1.0;
        color.b = 0.0;
    } else if (wavelength < 645.0) {
        color.r = 1.0;
        color.g = -(wavelength - 645.0) / (645.0 - 580.0);
        color.b = 0.0;
    } else {
        color.r = 1.0;
        color.g = 0.0;
        color.b = 0.0;
    }

    float attenuation;
    if (wavelength < 420.0) {
        attenuation = 0.3 + 0.7 * (wavelength - 380.0) / (420.0 - 380.0);
    } else if (wavelength > 700.0) {
        attenuation = 0.3 + 0.7 * (780.0 - wavelength) / (780.0 - 700.0);
    } else {
        attenuation = 1.0;
    }

    return color * attenuation;
}

// ==================== 引力红移计算（必须在 rayMarch 之前）====================
float calculateGravitationalRedshift(float r, float mass) {
    float rs = schwarzschildRadius(mass);

    if (r <= rs) {
        return 0.001;
    }

    float redshiftFactor = 1.0 / sqrt(max(1.0 - rs / r, 0.001));

    return redshiftFactor;
}

// ==================== 光线追踪（现在可以调用上面的函数了）====================
Photon rayMarch(Ray ray, float mass, float spin, int maxSteps) {
    Photon photon;
    photon.position = ray.origin;
    photon.momentum = ray.direction * ray.wavelength;
    photon.energy = PLANCK_CONSTANT * SPEED_OF_LIGHT / max(ray.wavelength, 1e-10);
    photon.color = vec3(1.0);
    photon.absorbed = false;

    float stepSize = 0.01;
    float safetyFactor = 1.5;
    float rs = schwarzschildRadius(mass);

    for (int i = 0; i < maxSteps; i++) {
        float r = length(photon.position);

        if (r < rs * safetyFactor) {
            photon.absorbed = true;
            photon.color = vec3(0.0);
            break;
        }

        vec3 newPosition = geodesicEquation(
            photon.position,
            photon.momentum,
            mass,
            spin,
            stepSize
        );

        photon.position = newPosition;

        float redshift = calculateGravitationalRedshift(r, mass);
        float newWavelength = ray.wavelength * redshift;
        photon.energy /= max(redshift, 0.001);

        photon.color = wavelengthToRGB(newWavelength);

        if (r > 10.0 * rs) {
            break;
        }
    }

    return photon;
}

// ==================== 相对论性多普勒效应 ====================
vec3 relativisticDoppler(vec3 color, vec3 relativeVelocity, float wavelength) {
    float v = length(relativeVelocity);
    float beta = v / SPEED_OF_LIGHT;

    if (beta >= 1.0) {
        beta = 0.999;
    }

    float dopplerFactor;
    float radialVelocity = dot(normalize(relativeVelocity), vec3(0.0, 0.0, 1.0));

    if (radialVelocity > 0.0) {
        dopplerFactor = sqrt((1.0 - beta) / (1.0 + beta));
    } else {
        dopplerFactor = sqrt((1.0 + beta) / (1.0 - beta));
    }

    float newWavelength = wavelength * dopplerFactor;

    vec3 dopplerColor = wavelengthToRGB(newWavelength);

    float blendFactor = clamp(beta * 5.0, 0.0, 1.0);
    return mix(color, dopplerColor, blendFactor);
}

// ==================== 吸积盘物理模型 ====================
float accretionDiskTemperature(float r, float mass, float accretionRate) {
    float rs = schwarzschildRadius(mass);
    float r_in = 3.0 * rs;
    float r_out = 100.0 * rs;

    if (r < r_in || r > r_out) {
        return 0.0;
    }

    float temperature = AccretionDiskTemperature *
                       pow(mass * accretionRate / max(r * r * r, 0.001), 0.25);

    return temperature;
}

vec3 blackbodyRadiation(float temperature, float wavelength) {
    if (temperature < 100.0) {
        return vec3(0.0);
    }

    float h = PLANCK_CONSTANT;
    float c = SPEED_OF_LIGHT;
    float k = BOLTZMANN_CONSTANT;

    float lambda = wavelength * 1e-9;
    float exponent = (h * c) / (lambda * k * temperature);

    if (exponent > 100.0) {
        return vec3(0.0);
    }

    float planckFactor = (2.0 * h * c * c) / (pow(lambda, 5.0) * (exp(exponent) - 1.0));

    vec3 color;
    if (temperature < 1000.0) {
        color = vec3(1.0, 0.4, 0.1);
    } else if (temperature < 3000.0) {
        color = vec3(1.0, 0.6, 0.2);
    } else if (temperature < 5000.0) {
        color = vec3(1.0, 0.8, 0.5);
    } else if (temperature < 8000.0) {
        color = vec3(1.0, 0.9, 0.7);
    } else if (temperature < 15000.0) {
        color = vec3(0.8, 0.9, 1.0);
    } else {
        color = vec3(0.6, 0.7, 1.0);
    }

    float intensity = clamp(planckFactor * 1e-15, 0.0, 1.0);
    return color * intensity * AccretionDiskIntensity;
}

// ==================== 参考系拖拽 ====================
vec3 frameDraggingEffect(vec3 position, vec3 velocity, float mass, float spin) {
    if (!EnableFrameDragging || spin == 0.0) {
        return velocity;
    }

    float r = length(position);
    float theta = acos(clamp(position.z / max(r, 0.001), -1.0, 1.0));

    float Omega = (2.0 * GRAVITATIONAL_CONSTANT * mass * SOLAR_MASS * spin) /
              max(SPEED_OF_LIGHT * SPEED_OF_LIGHT * r * r * r, 0.001);

    vec3 axis = normalize(vec3(-position.y, position.x, 0.0));
    float angle = Omega * Time;

    vec3 v = velocity;
    vec3 rotatedVelocity = v * cos(angle) +
                          cross(axis, v) * sin(angle) +
                          axis * dot(axis, v) * (1.0 - cos(angle));

    return rotatedVelocity;
}

// ==================== 引力透镜效应 ====================
vec2 gravitationalLensing(vec2 uv, vec2 center, float mass, float spin) {
    vec2 dir = uv - center;
    float r = length(dir);

    if (r < 0.001) {
        return center;
    }

    float rs = schwarzschildRadius(mass);

    float deflectionAngle;

    if (r > rs * 2.0) {
        deflectionAngle = (4.0 * GRAVITATIONAL_CONSTANT * mass * SOLAR_MASS) /
                         max(SPEED_OF_LIGHT * SPEED_OF_LIGHT * r, 0.001);
    } else {
        float x = r / max(rs, 0.001);
        deflectionAngle = PI * (1.0 - exp(-0.5 * (x - 1.0)));
    }

    if (spin != 0.0) {
        float spinFactor = 1.0 + 0.5 * spin * cos(atan(dir.y, dir.x));
        deflectionAngle *= spinFactor;
    }

    float angle = atan(dir.y, dir.x) + deflectionAngle;
    float newR = r * (1.0 + 0.1 * sin(deflectionAngle * 2.0));

    vec2 distortedUV = center + vec2(cos(angle), sin(angle)) * newR;

    return distortedUV;
}

// ==================== 黑洞阴影 ====================
vec3 blackHoleShadow(vec2 uv, vec2 center, float mass, float spin) {
    vec2 dir = uv - center;
    float r = length(dir);
    float rs = schwarzschildRadius(mass);

    float photonSphere = 1.5 * rs * (1.0 + 0.2 * spin);

    float shadow;
    if (r < photonSphere * 0.8) {
        shadow = 0.0;
    } else if (r < photonSphere * 1.5) {
        shadow = smoothstep(photonSphere * 0.8, photonSphere * 1.5, r);
    } else {
        shadow = 1.0;
    }

    float asymmetry = spin * 0.3 * cos(atan(dir.y, dir.x) * 2.0);
    shadow = clamp(shadow + asymmetry, 0.0, 1.0);

    return vec3(shadow);
}

// ==================== 霍金辐射 ====================
vec3 hawkingRadiation(vec2 uv, vec2 center, float mass) {
    vec2 dir = uv - center;
    float r = length(dir);
    float rs = schwarzschildRadius(mass);

    if (r < rs * 1.5) {
        return vec3(0.0);
    }

    float hawkingTemperature = (PLANCK_CONSTANT * pow(SPEED_OF_LIGHT, 3.0)) /
                              max(8.0 * PI * GRAVITATIONAL_CONSTANT * mass * SOLAR_MASS * BOLTZMANN_CONSTANT, 1e-10);

    float intensity = exp(-(r - rs * 1.5) / (rs * 0.1)) *
                     hawkingTemperature * 1e20;

    vec3 radiationColor = vec3(0.7, 0.5, 1.0) * intensity;

    float quantumNoise = perlinNoise(uv * 100.0 + Time) * 0.1;
    radiationColor += vec3(quantumNoise);

    return radiationColor;
}

// ==================== 主渲染函数 ====================
void main() {
    vec2 screenUV = gl_FragCoord.xy / ScreenSize;
    vec2 blackHoleCenter = BlackHolePos.xy / ScreenSize;

    if (BlackHolePos.z < 0.5) {
        blackHoleCenter = vec2(0.5);
    }

    vec2 lensedUV = gravitationalLensing(
        screenUV,
        blackHoleCenter,
        BlackHoleMass,
        SpinParameter
    );

    vec4 originalColor = texture(DiffuseSampler, lensedUV);

    vec2 dir = screenUV - blackHoleCenter;
    float distToCenter = length(dir);
    float angle = atan(dir.y, dir.x);

    float rs = schwarzschildRadius(BlackHoleMass);
    vec2 horizons = kerrHorizons(BlackHoleMass, SpinParameter);

    vec3 shadow = blackHoleShadow(screenUV, blackHoleCenter, BlackHoleMass, SpinParameter);

    if (distToCenter < horizons.x * 0.8) {
        vec3 hawking = hawkingRadiation(screenUV, blackHoleCenter, BlackHoleMass);
        fragColor = vec4(hawking, 1.0);
        return;
    }

    vec3 accretionColor = vec3(0.0);

    for (int layer = 0; layer < 3; layer++) {
        float layerRadius = horizons.x * (1.5 + float(layer) * 0.8);
        float layerThickness = rs * 0.05;

        if (abs(distToCenter - layerRadius) < layerThickness) {
            float temperature = accretionDiskTemperature(
                distToCenter,
                BlackHoleMass,
                0.01
            );

            float wavelength = 500.0e-9;
            vec3 layerColor = blackbodyRadiation(temperature, wavelength);

            float orbitalSpeed = sqrt(GRAVITATIONAL_CONSTANT * BlackHoleMass * SOLAR_MASS / max(distToCenter, 0.001));
            float orbitalAngle = Time * orbitalSpeed * 0.01;

            vec3 relativeVel = vec3(cos(angle + orbitalAngle), sin(angle + orbitalAngle), 0.0) * orbitalSpeed;
            layerColor = relativisticDoppler(layerColor, relativeVel, wavelength);

            float redshift = calculateGravitationalRedshift(distToCenter, BlackHoleMass);
            layerColor *= vec3(1.0, 1.0 / redshift, 1.0 / (redshift * redshift));

            accretionColor += layerColor * (1.0 - float(layer) * 0.3);
        }
    }

    vec3 photonRingColor = vec3(0.0);
    float photonSphere = 1.5 * rs;

    for (int ring = 0; ring < 3; ring++) {
        float ringRadius = photonSphere * (1.0 + float(ring) * 0.2);
        float ringWidth = rs * 0.02;

        float ringFactor = smoothstep(ringRadius - ringWidth, ringRadius, distToCenter) -
                          smoothstep(ringRadius, ringRadius + ringWidth, distToCenter);

        if (ringFactor > 0.0) {
            float brightness = 1.0 / (1.0 + float(ring));

            float angularVariation = sin(angle * float(3 + ring) + Time * 2.0) * 0.5 + 0.5;

            vec3 ringColor;
            if (ring == 0) ringColor = vec3(1.0, 0.9, 0.8);
            else if (ring == 1) ringColor = vec3(0.9, 0.8, 1.0);
            else ringColor = vec3(0.8, 0.9, 1.0);

            photonRingColor += ringColor * ringFactor * brightness * angularVariation;
        }
    }

    vec3 frameDragColor = vec3(0.0);
    if (EnableLenseThirring && SpinParameter != 0.0) {
        vec3 position = vec3(dir, 0.0);
        vec3 velocity = vec3(-dir.y, dir.x, 0.0) * 0.1;

        vec3 draggedVelocity = frameDraggingEffect(
            position,
            velocity,
            BlackHoleMass,
            SpinParameter
        );

        float dragStrength = length(draggedVelocity - velocity);
        frameDragColor = vec3(0.3, 0.5, 0.8) * dragStrength * 0.5;
    }

    vec3 jetColor = vec3(0.0);
    if (SpinParameter > 0.5) {
        float jetAngle = angle;

        float jetCone = 0.2;
        float jetIntensity = smoothstep(horizons.x * 2.0, horizons.x * 10.0, distToCenter);

        if (abs(jetAngle) < jetCone || abs(jetAngle - PI) < jetCone) {
            float jetSpeed = 0.99 * SPEED_OF_LIGHT;

            float dopplerBeaming = 1.0 / max(1.0 - jetSpeed / SPEED_OF_LIGHT, 0.001);
            jetIntensity *= dopplerBeaming * dopplerBeaming;

            jetColor = vec3(0.6, 0.7, 1.0) * jetIntensity;

            float turbulence = fbm(dir * 10.0 + Time * 0.5, 3);
            jetColor *= (0.8 + 0.2 * turbulence);
        }
    }

    vec3 finalColor = originalColor.rgb * shadow;

    finalColor = mix(finalColor, accretionColor, 0.8);

    finalColor += photonRingColor;

    finalColor += frameDragColor;

    finalColor += jetColor;

    float globalRedshift = calculateGravitationalRedshift(distToCenter, BlackHoleMass);
    finalColor.r *= (1.0 + (globalRedshift - 1.0) * 0.3);
    finalColor.g /= globalRedshift;
    finalColor.b /= (globalRedshift * globalRedshift);

    if (length(ViewerVelocity) > 0.01) {
        finalColor = relativisticDoppler(finalColor, ViewerVelocity, 500.0e-9);
    }

    if (distToCenter < rs * 3.0) {
        float dispersion = 1.0 - smoothstep(rs * 1.5, rs * 3.0, distToCenter);

        float redShift = sin(angle + Time * 0.3) * 0.05;
        float blueShift = sin(angle - Time * 0.3) * 0.1;

        finalColor.r *= (1.0 + redShift * dispersion);
        finalColor.b *= (1.0 + blueShift * dispersion);
    }

    float timeDilation = sqrt(max(1.0 - rs / max(distToCenter, rs * 1.01), 0.001));
    float localTime = Time * timeDilation;

    if (distToCenter < rs * 2.0) {
        float pulse = sin(localTime * 5.0) * 0.5 + 0.5;
        finalColor *= (0.8 + 0.2 * pulse);
    }

    finalColor = finalColor / (1.0 + finalColor);

    finalColor = pow(finalColor, vec3(1.0/2.2));

    fragColor = vec4(finalColor, originalColor.a);
}
