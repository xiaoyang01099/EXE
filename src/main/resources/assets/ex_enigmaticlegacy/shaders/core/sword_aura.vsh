#version 330 core

// 剑气实例化顶点 shader：Position 是 OBJ 局部坐标，不再由 CPU 逐顶点转世界坐标。
layout(location = 0) in vec3 Position;
// UV0 保存 sword1 sprite 内部的 0 到 1 局部 UV，片段 shader 再映射到图集。
layout(location = 1) in vec2 UV0;
// InstanceModel0 到 InstanceModel3 是每个剑气实例的局部到世界矩阵四列。
layout(location = 2) in vec4 InstanceModel0;
layout(location = 3) in vec4 InstanceModel1;
layout(location = 4) in vec4 InstanceModel2;
layout(location = 5) in vec4 InstanceModel3;
// InstanceVisual: x=渐变选择，y=划出进度，z=bloom 强度，w=透明度。
layout(location = 6) in vec4 InstanceVisual;

// Minecraft 自动写入投影矩阵。
uniform mat4 ProjMat;
// 后处理队列传入的视图矩阵，用于把世界坐标转到相机空间。
uniform mat4 uView;

// 传给片段 shader 的 sword1 局部 UV。
out vec2 texCoord;
// 传给片段 shader 的实例可视参数。
out vec4 vertexColor;

void main() {
    // 用四个实例 attribute 组装 model 矩阵，减少 CPU 逐顶点变换。
    mat4 instanceModel = mat4(InstanceModel0, InstanceModel1, InstanceModel2, InstanceModel3);
    vec4 worldPosition = instanceModel * vec4(Position, 1.0);

    // 先转到视图空间，再进入投影空间。
    gl_Position = ProjMat * uView * worldPosition;
    texCoord = UV0;
    vertexColor = InstanceVisual;
}
