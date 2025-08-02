package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.tile;


import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import net.xiaoyang010.ex_enigmaticlegacy.Client.model.ModelSeaSerpent;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.SeaSerpent;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

public class SeaSerpentRender extends RenderAquaticCreature<SeaSerpent> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/sea_serpent.png");

    public SeaSerpentRender(EntityRendererProvider.Context context) {
        super(context, new ModelSeaSerpent(context.bakeLayer(ModelSeaSerpent.LAYER_LOCATION)), 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(SeaSerpent entity) {
        return TEXTURE;
    }
}