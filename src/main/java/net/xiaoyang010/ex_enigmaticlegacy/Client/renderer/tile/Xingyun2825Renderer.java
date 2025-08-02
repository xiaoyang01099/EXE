
package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.PlayerModelN;
import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.layer.ColourfulWitherArmorLayer;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.Xingyun2825Entity;

import java.util.Random;

public class Xingyun2825Renderer extends MobRenderer<Xingyun2825Entity, PlayerModelN<Xingyun2825Entity>> {
	public Xingyun2825Renderer(EntityRendererProvider.Context context) {
		super(context, new PlayerModelN<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
		this.addLayer(new HumanoidArmorLayer(this, new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR))));
		this.addLayer(new ItemInHandLayer<>(this));
		this.addLayer(new ColourfulWitherArmorLayer(this,context.getModelSet()));
	}
	@Override
	public ResourceLocation getTextureLocation(Xingyun2825Entity entity) {
		return new ResourceLocation("ex_enigmaticlegacy:textures/entity/xingyun2825.png");
	}

	@Override
	public void render(Xingyun2825Entity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
		super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
		float $$14;
		float $$15;
		if (pEntity.deathTick-650 > 0) {
			$$14 = ((float)(pEntity.deathTick-650)/2 + pPartialTicks) / 200.0F;
			$$15 = Math.min($$14 > 0.8F ? ($$14 - 0.8F) / 0.2F : 0.0F, 1.0F);
			Random $$16 = new Random(432L);
			VertexConsumer $$17 = pBuffer.getBuffer(RenderType.lightning());
			pMatrixStack.pushPose();
			pMatrixStack.translate(0.0, -0, -0);
			for(int $$18 = 0; (float)$$18 < ($$14 + $$14 * $$14) / 2.0F * 60.0F; ++$$18) {
				pMatrixStack.mulPose(Vector3f.XP.rotationDegrees($$16.nextFloat() * 360.0F));
				pMatrixStack.mulPose(Vector3f.YP.rotationDegrees($$16.nextFloat() * 360.0F));
				pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees($$16.nextFloat() * 360.0F));
				pMatrixStack.mulPose(Vector3f.XP.rotationDegrees($$16.nextFloat() * 360.0F));
				pMatrixStack.mulPose(Vector3f.YP.rotationDegrees($$16.nextFloat() * 360.0F));
				pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees($$16.nextFloat() * 360.0F + $$14 * 90.0F));
				float $$19 = $$16.nextFloat() * 20.0F + 5.0F + $$15 * 10.0F;
				float $$20 = $$16.nextFloat() * 2.0F + 1.0F + $$15 * 2.0F;
				Matrix4f $$21 = pMatrixStack.last().pose();
				int $$22 = (int)(255.0F * (1.0F - $$15));
				vertex01($$17, $$21, $$22);
				vertex2($$17, $$21, $$19, $$20);
				vertex3($$17, $$21, $$19, $$20);
				vertex01($$17, $$21, $$22);
				vertex3($$17, $$21, $$19, $$20);
				vertex4($$17, $$21, $$19, $$20);
				vertex01($$17, $$21, $$22);
				vertex4($$17, $$21, $$19, $$20);
				vertex2($$17, $$21, $$19, $$20);
			}
			pMatrixStack.popPose();
		}
	}
	private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0) / 2.0);;
	private static void vertex01(VertexConsumer p_114220_, Matrix4f p_114221_, int p_114222_) {
		p_114220_.vertex(p_114221_, 0.0F, 0.0F, 0.0F).color(255, 255, 255, p_114222_).endVertex();
	}

	private static void vertex2(VertexConsumer p_114215_, Matrix4f p_114216_, float p_114217_, float p_114218_) {
		Vector3f vec3 = getRGB();
		p_114215_.vertex(p_114216_, -HALF_SQRT_3 * p_114218_, p_114217_, -0.5F * p_114218_).color(vec3.x(), vec3.y(), vec3.z(), 0).endVertex();
	}

	private static void vertex3(VertexConsumer p_114224_, Matrix4f p_114225_, float p_114226_, float p_114227_) {
		Vector3f vec3 = getRGB();
		p_114224_.vertex(p_114225_, HALF_SQRT_3 * p_114227_, p_114226_, -0.5F * p_114227_).color(vec3.x(), vec3.y(), vec3.z(), 0).endVertex();
	}

	private static void vertex4(VertexConsumer p_114229_, Matrix4f p_114230_, float p_114231_, float p_114232_) {
		Vector3f vec3 = getRGB();
		p_114229_.vertex(p_114230_, 0.0F, p_114231_, 1.0F * p_114232_).color(vec3.x(), vec3.y(), vec3.z(), 0).endVertex();
	}
public static Vector3f getRGB(){
	float p_14170_ =(float) Util.getMillis() / 5000.0F % 1.0F,p_14171_ = 1f,p_14172_ = 1.5f;
	int i = (int)(p_14170_ * 6.0F) % 6;
	float ff = p_14170_ * 6.0F - (float)i;
	float f1 = p_14172_ * (1.0F - p_14171_);
	float f2 = p_14172_ * (1.0F - ff * p_14171_);
	float f3 = p_14172_ * (1.0F - (1.0F - ff) * p_14171_);
	float f4;
	float f5;
	float f6;
	switch (i) {
		case 0:
			f4 = p_14172_;
			f5 = f3;
			f6 = f1;
			break;
		case 1:
			f4 = f2;
			f5 = p_14172_;
			f6 = f1;
			break;
		case 2:
			f4 = f1;
			f5 = p_14172_;
			f6 = f3;
			break;
		case 3:
			f4 = f1;
			f5 = f2;
			f6 = p_14172_;
			break;
		case 4:
			f4 = f3;
			f5 = f1;
			f6 = p_14172_;
			break;
		case 5:
			f4 = p_14172_;
			f5 = f1;
			f6 = f2;
			break;
		default:
			throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + p_14170_ + ", " + p_14171_ + ", " + p_14172_);

	}
	return new Vector3f(f4,f5,f6);
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
