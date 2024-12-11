
package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.PlayerModelN;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.Xingyun2825Entity;

public class Xingyun2825Renderer extends MobRenderer<Xingyun2825Entity, PlayerModelN<Xingyun2825Entity>> {
	public Xingyun2825Renderer(EntityRendererProvider.Context context) {
		super(context, new PlayerModelN<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
		this.addLayer(new HumanoidArmorLayer(this, new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR))));
		this.addLayer(new WitLayer(this,context.getModelSet()));
	}
	@Override
	public ResourceLocation getTextureLocation(Xingyun2825Entity entity) {
		return new ResourceLocation("ex_enigmaticlegacy:textures/entity/xingyun2825.png");
	}
	@OnlyIn(Dist.CLIENT)
	public class WitLayer extends EnergySwirlLayer<Xingyun2825Entity, PlayerModelN<Xingyun2825Entity>> {
		private static final ResourceLocation WITHER_ARMOR_LOCATION = new ResourceLocation("textures/entity/wither/wither_armor.png");
		private final PlayerModelN<Xingyun2825Entity> model;

		public WitLayer(RenderLayerParent<Xingyun2825Entity, PlayerModelN<Xingyun2825Entity>> p_174554_, EntityModelSet p_174555_) {
			super(p_174554_);
			model = new PlayerModelN<>(p_174555_.bakeLayer(ModelLayers.PLAYER));
		}

		protected float xOffset(float p_117702_) {
			return Mth.cos(p_117702_ * 0.02F) * 3.0F;
		}

		protected ResourceLocation getTextureLocation() {
			return WITHER_ARMOR_LOCATION;
		}

		protected EntityModel<Xingyun2825Entity> model() {
			return model;
		}
	}
}
