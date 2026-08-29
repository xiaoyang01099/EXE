#version 330 core

// 手持飞剑透明模型顶点 shader。
// item renderer 阶段已经把当前 PoseStack 和 RenderSystem ModelView 矩阵组合为 FlySwordModelViewMat。
// 后处理阶段重放 baked quad 时不再反推玩家手部位置，直接使用缓存矩阵投影到屏幕。
in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec3 Normal;

uniform mat4 ProjMat;
uniform mat4 FlySwordModelViewMat;
uniform vec4 MainSpriteUV;

out vec2 vLocalUV;
out float vModelAlpha;
out vec3 vViewPosition;
out vec3 vViewNormal;
out float vSwordGradient;

// Minecraft baked model 顶点以 1/16 方块为单位，乘 16 后恢复模型 JSON 的局部坐标。
const float MODEL_COORDINATE_SCALE = 16.0;
const float GRADIENT_START_MODEL_Y = -1.95;
const float GRADIENT_END_MODEL_Y = 5.55;

void main() {
    // Position 是 baked model 的局部坐标，矩阵包含第一/第三人称手持变换。
    vec4 viewPosition = FlySwordModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPosition;

    // baked quad 的 UV 已经是 item/block atlas 坐标，片元阶段可直接采样。
    // 把飞剑 sprite 的 atlas UV 还原到 0 到 1，供两张噪声独立平铺和流动。
    vec2 mainSpriteSize = max(MainSpriteUV.zw - MainSpriteUV.xy, vec2(0.000001));
    vLocalUV = (UV0 - MainSpriteUV.xy) / mainSpriteSize;
    // 顶点 RGB 不参与透明飞剑计算，只把 Color.a 传给片元阶段控制模型透明度。
    vModelAlpha = Color.a;
    // 按整把飞剑的局部 Y 长度生成连续比例，旋转与缩放不会改变渐变位置。
    float modelLocalY = Position.y * MODEL_COORDINATE_SCALE;
    float gradientLength = max(GRADIENT_END_MODEL_Y - GRADIENT_START_MODEL_Y, 0.0001);
    vSwordGradient = clamp((modelLocalY - GRADIENT_START_MODEL_Y) / gradientLength, 0.0, 1.0);
    // 观察空间位置和逆转置法线用于片元阶段计算菲尼尔，兼容手持矩阵中的非均匀缩放。
    vViewPosition = viewPosition.xyz;
    mat3 normalMatrix = transpose(inverse(mat3(FlySwordModelViewMat)));
    vViewNormal = normalize(normalMatrix * Normal);
}
