package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.material;

import net.minecraft.resources.ResourceLocation;

// ParticleMaterial 保存一个 GPU 粒子材质的 CPU 侧配置，最终会上传到材质 SSBO。
public class ParticleMaterial {
    public final int id; // 材质 ID，写入 Particle.extra.w 后供渲染 Shader 查表。
    public final ParticleMaterialKey key; // 调用方使用的稳定材质 key。
    public final int pipelineId; // 渲染批次 ID，用于决定使用哪个 Shader。
    public final ResourceLocation baseTexture; // 主贴图 sprite，null 表示不采样主贴图。
    public final ResourceLocation noiseTexture0; // 第一张噪声 sprite，null 表示不采样。
    public final ResourceLocation noiseTexture1; // 第二张噪声 sprite，null 表示不采样。
    public final ResourceLocation topDissolveTexture; // 顶部消散噪声 sprite，null 表示不采样。
    public final float noiseTileX; // 噪声横向平铺倍率。
    public final float noiseTileY; // 噪声纵向平铺倍率。
    public final float noiseSpeed; // 噪声流动速度。
    public final float noiseStrength; // 噪声扰动或溶解强度。
    public final float bloomCore; // Bloom 核心强度。
    public final float bloomHalo; // Bloom 边缘光晕强度。
    public final float bloomEdgeWidth; // Bloom 边缘宽度。
    public final float alphaScale; // 透明度整体倍率。

    // 创建一个 GPU 粒子材质定义。
    public ParticleMaterial(int id, ParticleMaterialKey key, int pipelineId,
                            ResourceLocation baseTexture, ResourceLocation noiseTexture0, ResourceLocation noiseTexture1, ResourceLocation topDissolveTexture,
                            float noiseTileX, float noiseTileY, float noiseSpeed, float noiseStrength,
                            float bloomCore, float bloomHalo, float bloomEdgeWidth, float alphaScale) {
        this.id = id;
        this.key = key;
        this.pipelineId = pipelineId;
        this.baseTexture = baseTexture;
        this.noiseTexture0 = noiseTexture0;
        this.noiseTexture1 = noiseTexture1;
        this.topDissolveTexture = topDissolveTexture;
        this.noiseTileX = noiseTileX;
        this.noiseTileY = noiseTileY;
        this.noiseSpeed = noiseSpeed;
        this.noiseStrength = noiseStrength;
        this.bloomCore = bloomCore;
        this.bloomHalo = bloomHalo;
        this.bloomEdgeWidth = bloomEdgeWidth;
        this.alphaScale = alphaScale;
    }
}
