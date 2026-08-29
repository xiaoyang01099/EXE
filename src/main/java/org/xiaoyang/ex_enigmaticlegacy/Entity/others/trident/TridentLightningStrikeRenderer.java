package org.xiaoyang.ex_enigmaticlegacy.Entity.others.trident;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

// TridentLightningStrikeRenderer 是落点雷电实体空渲染器，视觉由实体客户端 tick 提交到后处理闪电队列。
public class TridentLightningStrikeRenderer extends EntityRenderer<TridentLightningStrikeEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(Exe.MODID, "textures/item/fly_sword.png"); // 空渲染器占位纹理。

    public TridentLightningStrikeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(TridentLightningStrikeEntity entity) {
        return TEXTURE;
    }
}
