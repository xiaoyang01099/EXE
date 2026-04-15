package org.xiaoyang.ex_enigmaticlegacy.Client.renderer.others;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.ManaitaArrow;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

public class ManaitaArrowRenderer extends ArrowRenderer<ManaitaArrow> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(Exe.MODID, "textures/entity/arrow/lip.png");

    public ManaitaArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ManaitaArrow entity) {
        return TEXTURE;
    }
}
