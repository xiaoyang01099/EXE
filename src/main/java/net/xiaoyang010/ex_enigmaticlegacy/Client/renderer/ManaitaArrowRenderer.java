package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

public class ManaitaArrowRenderer extends ArrowRenderer<AbstractArrow> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/arrow/lip.png");

    public ManaitaArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractArrow entity) {
        return TEXTURE;
    }
}
