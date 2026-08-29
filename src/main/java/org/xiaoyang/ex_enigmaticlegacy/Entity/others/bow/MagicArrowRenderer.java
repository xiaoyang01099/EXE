package org.xiaoyang.ex_enigmaticlegacy.Entity.others.bow;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

// MagicArrowRenderer 复用原版箭贴图渲染魔法箭。
public class MagicArrowRenderer extends ArrowRenderer<MagicArrowEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/projectiles/arrow.png");

    public MagicArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(MagicArrowEntity entity) {
        return TEXTURE;
    }
}
