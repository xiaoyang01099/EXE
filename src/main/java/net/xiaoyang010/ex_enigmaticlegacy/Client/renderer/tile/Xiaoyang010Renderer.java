
package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.tile;

import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.PlayerModelN;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.Xiaoyang010Entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.HumanoidModel;

public class Xiaoyang010Renderer extends MobRenderer<Xiaoyang010Entity, PlayerModelN<Xiaoyang010Entity>> {
	public Xiaoyang010Renderer(EntityRendererProvider.Context context) {
		super(context, new PlayerModelN<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
		this.addLayer(new HumanoidArmorLayer(this, new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR))));
	}

	@Override
	public ResourceLocation getTextureLocation(Xiaoyang010Entity entity) {
		return new ResourceLocation("ex_enigmaticlegacy:textures/entity/xiao_yang_.png");
	}
}
