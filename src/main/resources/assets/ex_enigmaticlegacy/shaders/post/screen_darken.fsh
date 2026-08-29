#version 330 core

in vec2 textureCoords;

out vec4 out_Colour;

uniform sampler2D SceneTexture;
uniform float DarkenStrength;

void main(void) {
    // 只采样原版场景颜色，不读取本模组 mainFBO 和 bloom。
    vec4 sceneColor = texture(SceneTexture, textureCoords);
    // DarkenStrength 越大，原版场景越暗；自己的粒子会在后续 pass 再叠加回来。
    float darken = clamp(DarkenStrength, 0.0, 0.95);
    out_Colour = vec4(sceneColor.rgb * (1.0 - darken), sceneColor.a);
}
