package org.xiaoyang.ex_enigmaticlegacy.Client.renderer.tile;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.xiaoyang.ex_enigmaticlegacy.Client.model.SacabambaspisModel;
import org.xiaoyang.ex_enigmaticlegacy.Entity.biological.SacabambaspisEntity;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

public class SacabambaspisRender extends MobRenderer<SacabambaspisEntity, SacabambaspisModel<SacabambaspisEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Exe.MODID, "textures/entity/sacabambaspis.png");

    public SacabambaspisRender(EntityRendererProvider.Context context) {
        super(context, new SacabambaspisModel<>(context.bakeLayer(SacabambaspisModel.LAYER_LOCATION)), 0.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(SacabambaspisEntity entity) {
        return TEXTURE;
    }
}