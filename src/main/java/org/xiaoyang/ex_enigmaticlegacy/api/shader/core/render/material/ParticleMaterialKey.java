package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.material;

// ParticleMaterialKey 定义 GPU 粒子可选材质，调用方通过 key 选择贴图、噪声和渲染 Shader。
public enum ParticleMaterialKey {
    DEFAULT_SDF, // 默认 SDF 几何粒子，兼容旧版圆形、心形和星形等形状。
    LIGHT_EFFECT, // 三噪声光效粒子，复用金色螺旋的噪声贴图链路。
    DIRECTED_LIGHT_EFFECT, // 定向三噪声光效粒子，世界空间固定矩形不朝向相机。
    MAGIC_CIRCLE_ENERGY, // 法阵能量基础粒子，使用 tex_pattern66 与 tex_pattern59。
    SHOCKWAVE_MAGIC_CIRCLE, // 冲击波法阵粒子，使用 trail_2 与 tex_pattern59。
    EX_SWORD_WAVE, // EX 剑气粒子，使用 ex_wave1、ex_wave2 与 noise_054。
    STAR_TEXTURE, // 星星材质粒子，使用 ai_star R 通道作为透明度。
    RISING_SHOCKWAVE // 上升冲击波圆台材质粒子，使用 t_fx_tile_0016 和程序化圆台网格。
}
