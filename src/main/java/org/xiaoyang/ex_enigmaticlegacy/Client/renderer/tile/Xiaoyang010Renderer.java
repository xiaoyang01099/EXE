
package org.xiaoyang.ex_enigmaticlegacy.Client.renderer.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import org.xiaoyang.ex_enigmaticlegacy.Client.model.PlayerModelN;
import org.xiaoyang.ex_enigmaticlegacy.Entity.biological.Xiaoyang010Entity;

public class Xiaoyang010Renderer extends MobRenderer<Xiaoyang010Entity, PlayerModelN<Xiaoyang010Entity>> {
	public Xiaoyang010Renderer(EntityRendererProvider.Context context) {
		super(context, new PlayerModelN<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
		this.addLayer(new HumanoidArmorLayer(this, new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), Minecraft.getInstance().getModelManager()));
	}

	@Override
	public ResourceLocation getTextureLocation(Xiaoyang010Entity entity) {
		return new ResourceLocation("ex_enigmaticlegacy:textures/entity/xiao_yang_.png");
	}
}
