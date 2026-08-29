#version 330 core

// 烟雾实例化顶点 shader：Position 是静态 quad 局部坐标，范围为 -0.5 到 0.5。
layout(location = 0) in vec3 Position;
// UV0 是静态 quad 的局部 UV，范围为 0 到 1。
layout(location = 1) in vec2 UV0;
// InstanceCenterSize: xyz=粒子中心，w=粒子尺寸。
layout(location = 2) in vec4 InstanceCenterSize;
// InstanceColor: rgba=CA0 可见烟雾颜色和透明度。
layout(location = 3) in vec4 InstanceColor;
// InstanceBloom: rgb=CA1 bloom 源颜色，a=bloom 强度。
layout(location = 4) in vec4 InstanceBloom;
// InstanceAnim: x=生成时间，y=生命周期，z=帧偏移，w=帧率。
layout(location = 5) in vec4 InstanceAnim;
// InstanceMotion: x=旋转初始角，y=旋转速度，z=随机值，w=保留。
layout(location = 6) in vec4 InstanceMotion;

// Minecraft 投影矩阵，直接自管 VAO 绘制时由 Java 手动写入。
uniform mat4 ProjMat;
// 后处理队列传入的视图矩阵，用于世界坐标转相机空间。
uniform mat4 uView;
// 当前相机右方向，用于展开 billboard。
uniform vec4 CameraRight;
// 当前相机上方向，用于展开 billboard。
uniform vec4 CameraUp;
// x=客户端时间，y=可播放帧数，z=列数，w=行数。
uniform vec4 GlobalParams;

// 传给片段 shader 的局部 UV。
out vec2 texCoord;
// 传给片段 shader 的可见层颜色。
out vec4 visibleColor;
// 传给片段 shader 的 bloom 颜色和强度。
out vec4 bloomColorData;
// 传给片段 shader 的动画参数。
out vec4 animData;
// 传给片段 shader 的随机参数。
out float randomValue;
// 传给片段 shader 的内部光照强度，负数表示当前粒子不启用假体积光照。
out float internalLight;

void main() {
    float age = max(GlobalParams.x - InstanceAnim.x, 0.0);
    float rotation = InstanceMotion.x + age * InstanceMotion.y;
    float cosRot = cos(rotation);
    float sinRot = sin(rotation);
    vec2 local = Position.xy;
    vec2 rotatedLocal = vec2(
            local.x * cosRot - local.y * sinRot,
            local.x * sinRot + local.y * cosRot
    );

    // 使用相机 right/up 展开 billboard，保证烟雾始终面向相机。
    vec3 worldOffset = CameraRight.xyz * rotatedLocal.x * InstanceCenterSize.w
            + CameraUp.xyz * rotatedLocal.y * InstanceCenterSize.w;
    vec3 worldPosition = InstanceCenterSize.xyz + worldOffset;

    gl_Position = ProjMat * uView * vec4(worldPosition, 1.0);
    texCoord = UV0;
    visibleColor = InstanceColor;
    bloomColorData = InstanceBloom;
    animData = InstanceAnim;
    randomValue = InstanceMotion.z;
    internalLight = InstanceMotion.w;
}
