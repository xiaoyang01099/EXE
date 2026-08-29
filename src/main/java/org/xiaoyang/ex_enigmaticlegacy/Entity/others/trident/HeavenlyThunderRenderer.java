package org.xiaoyang.ex_enigmaticlegacy.Entity.others.trident;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

// HeavenlyThunderRenderer 是天雷法阵实体空渲染器，真实视觉由实体客户端 tick 提交到后处理队列。
public class HeavenlyThunderRenderer extends EntityRenderer<HeavenlyThunderEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(Exe.MODID, "textures/item/fly_sword.png"); // 空渲染器占位纹理。

    public HeavenlyThunderRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(HeavenlyThunderEntity entity) {
        return TEXTURE;
    }
}
