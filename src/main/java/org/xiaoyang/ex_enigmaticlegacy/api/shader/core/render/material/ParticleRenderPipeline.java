package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.material;

// ParticleRenderPipeline 定义 GPU 粒子渲染批次，Shader 只能按批次切换，不能按单个粒子切换。
public class ParticleRenderPipeline {
    public static final int SDF_BASIC = 0; // 旧版 SDF 形状粒子 Shader 批次。
    public static final int LIGHT_EFFECT = 1; // 三噪声光效粒子 Shader 批次。
    public static final int DIRECTED_LIGHT_EFFECT = 2; // 世界空间定向三噪声光效粒子 Shader 批次。
    public static final int MAGIC_CIRCLE_ENERGY = 3; // 水平法阵能量粒子 Shader 批次。
    public static final int EX_SWORD_WAVE = 4; // 根据粒子方向固定在世界竖直平面的 EX 剑气批次。
    public static final int STAR_TEXTURE = 5; // 始终朝向相机的星星贴图粒子批次。
    public static final int RISING_SHOCKWAVE = 6; // 程序化上窄下宽圆台上升冲击波批次。
    public static final int COUNT = 7; // 当前已注册的渲染批次数量。

    public ParticleRenderPipeline() {}
}
