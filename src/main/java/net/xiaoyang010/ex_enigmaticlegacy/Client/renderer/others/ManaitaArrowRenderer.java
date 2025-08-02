package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.others;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.ManaitaArrow;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

public class ManaitaArrowRenderer extends ArrowRenderer<ManaitaArrow> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/arrow/lip.png");

    public ManaitaArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ManaitaArrow entity) {
        return TEXTURE;
    }
}
