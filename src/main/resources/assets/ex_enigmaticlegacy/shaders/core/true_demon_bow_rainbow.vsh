#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 IViewRotMat;
uniform int FogShape;
uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out float gradientCoord;
out float cosmicDensity;
out float cosmicParallax;
out vec4 texProj0;
out vec3 rainbowModelPosition;

void main() {
    vec4 clipPos = ProjMat * ModelViewMat * vec4(Position, 1.0);
    gl_Position = clipPos;
    texProj0 = vec4(clipPos.xy * 0.5 + clipPos.w * 0.5, clipPos.zw);
    vertexDistance = fog_distance(ModelViewMat, IViewRotMat * Position, FogShape);

    gradientCoord = Color.r;
    cosmicDensity = Color.g * 16.0;
    cosmicParallax = Color.b;
    rainbowModelPosition = Position;
    vec4 neutralTint = vec4(1.0, 1.0, 1.0, Color.a);
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, neutralTint)
            * texelFetch(Sampler2, UV2 / 16, 0);
    texCoord0 = UV0;
}
