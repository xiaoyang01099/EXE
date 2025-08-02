package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.tile;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.PlayerModelN;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.CloneEntity;

public class CloneEntityRenderer extends MobRenderer<CloneEntity, PlayerModelN<CloneEntity>> {
    public CloneEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModelN<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        this.addLayer(new HumanoidArmorLayer(this, new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR))));
    }

    @Override
    public ResourceLocation getTextureLocation(CloneEntity entity) {
        return new ResourceLocation("ex_enigmaticlegacy:textures/entity/xiao_yang_.png");
    }
}

