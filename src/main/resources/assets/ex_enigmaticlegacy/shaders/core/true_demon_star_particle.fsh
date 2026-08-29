#version 150

in vec4 vertexColor;
in vec2 texCoords;

out vec4 fragColor;

void main() {
    vec2 p = texCoords * 2.0 - 1.0;
    float d = length(p);

    if (d > 1.0) {
        fragColor = vec4(0.0);
        return;
    }

    float core = 1.0 - smoothstep(0.00, 0.19, d);
    float halo = (1.0 - smoothstep(0.12, 0.58, d)) * 0.42;

    float axis = min(abs(p.x), abs(p.y));
    float rayAlong = max(abs(p.x), abs(p.y));
    float crossRay = (1.0 - smoothstep(0.012, 0.072, axis)) * (1.0 - smoothstep(0.10, 1.00, rayAlong));

    float diagonalAxis = min(abs(p.x + p.y), abs(p.x - p.y)) * 0.7071068;
    float diagonalAlong = max(abs(p.x + p.y), abs(p.x - p.y)) * 0.7071068;
    float diagonalRay = (1.0 - smoothstep(0.010, 0.064, diagonalAxis)) * (1.0 - smoothstep(0.14, 0.86, diagonalAlong));

    float shape = max(max(core, halo), max(crossRay * 0.72, diagonalRay * 0.26));
    shape = pow(clamp(shape, 0.0, 1.0), 1.06);

    float pulsedAlpha = shape * vertexColor.a;
    float coreAlpha = core * vertexColor.r;
    float alpha = max(pulsedAlpha, coreAlpha);
    if (alpha <= 0.002) {
        fragColor = vec4(0.0);
        return;
    }

    fragColor = vec4(vec3(1.0), alpha);
}
