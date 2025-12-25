package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.UltimateValkyrie;

public class WitherArmorLayer<E extends AbstractClientPlayer>  extends RenderLayer<E, PlayerModel<E>> {
    private static final ResourceLocation WITHER_ARMOR_LOCATION = new ResourceLocation("textures/entity/wither/wither_armor.png");
    private final HumanoidModel<E> model;

    public WitherArmorLayer(RenderLayerParent<E, PlayerModel<E>> pRenderer, HumanoidModel model) {
        super(pRenderer);
        this.model= model;
    }

    public void render(PoseStack p_116970_, MultiBufferSource p_116971_, int p_116972_, E p_116973_, float p_116974_, float p_116975_, float p_116976_, float p_116977_, float p_116978_, float p_116979_) {
        if (UltimateValkyrie.isFullSuit(p_116973_)) {
            float f = (float) p_116973_.tickCount + p_116976_;
            float hue = (float) Util.getMillis() / 5000.0F % 1.0F;
            EntityModel<E> entitymodel = this.model();
            entitymodel.prepareMobModel(p_116973_, p_116974_, p_116975_, p_116976_);
            this.getParentModel().copyPropertiesTo(entitymodel);
            VertexConsumer vertexconsumer = p_116971_.getBuffer(RenderType.energySwirl(this.getTextureLocation(), this.xOffset(f) % 1.0F, f * 0.01F % 1.0F));
            entitymodel.setupAnim(p_116973_, p_116974_, p_116975_, p_116977_, p_116978_, p_116979_);
            float p_14170_ =(float) Util.getMillis() / 5000.0F % 1.0F,p_14171_ = 1f,p_14172_ = 1.5f;
            int i = (int)(p_14170_ * 6.0F) % 6;
            float ff = p_14170_ * 6.0F - (float)i;
            float f1 = p_14172_ * (1.0F - p_14171_);
            float f2 = p_14172_ * (1.0F - ff * p_14171_);
            float f3 = p_14172_ * (1.0F - (1.0F - ff) * p_14171_);
            float f4;
            float f5;
            float f6;
            entitymodel.renderToBuffer(p_116970_, vertexconsumer, p_116972_, OverlayTexture.NO_OVERLAY,1,1,1,200.0f);
        }
    }
    protected float xOffset(float p_117702_) {
        return Mth.cos(p_117702_ * 0.02F) * 3.0F;
    }

    protected ResourceLocation getTextureLocation() {
        return WITHER_ARMOR_LOCATION;
    }

    protected EntityModel<E> model() {
        return this.model;
    }
}

//彩色版本
//public class WitherArmorLayer<E extends AbstractClientPlayer> extends RenderLayer<E, PlayerModel<E>> {
//    private static final ResourceLocation WITHER_ARMOR_LOCATION = new ResourceLocation("textures/entity/wither/wither_armor.png");
//    private final HumanoidModel<E> model;
//
//    public WitherArmorLayer(RenderLayerParent<E, PlayerModel<E>> pRenderer, HumanoidModel<E> model) {
//        super(pRenderer);
//        this.model = model;
//    }
//
//    @Override
//    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, E player,
//                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
//                       float netHeadYaw, float headPitch) {
//        if (UltimateValkyrie.isFullSuit(player)) {
//            float f = (float) player.tickCount + partialTicks;
//
//            // 计算彩虹颜色 - 色相随时间变化
//            float hue = (float) Util.getMillis() / 3000.0F % 1.0F; // 3秒一个循环
//            float[] rgb = hsvToRgb(hue, 1.0F, 1.0F); // 饱和度和亮度都设为最大
//
//            EntityModel<E> entitymodel = this.model();
//            entitymodel.prepareMobModel(player, limbSwing, limbSwingAmount, partialTicks);
//            this.getParentModel().copyPropertiesTo(entitymodel);
//
//            VertexConsumer vertexConsumer = bufferSource.getBuffer(
//                    RenderType.energySwirl(this.getTextureLocation(), this.xOffset(f) % 1.0F, f * 0.01F % 1.0F)
//            );
//
//            entitymodel.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
//
//            // 使用彩虹颜色渲染，alpha 设置为 0.8 (透明度)
//            entitymodel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY,
//                    rgb[0], rgb[1], rgb[2], 0.8F);
//        }
//    }
//
//    /**
//     * HSV 转 RGB
//     * @param hue 色相 (0.0 - 1.0)
//     * @param saturation 饱和度 (0.0 - 1.0)
//     * @param value 明度 (0.0 - 1.0)
//     * @return float[] {r, g, b} 范围 0.0-1.0
//     */
//    private float[] hsvToRgb(float hue, float saturation, float value) {
//        int i = (int)(hue * 6.0F) % 6;
//        float f = hue * 6.0F - (float)i;
//        float p = value * (1.0F - saturation);
//        float q = value * (1.0F - f * saturation);
//        float t = value * (1.0F - (1.0F - f) * saturation);
//
//        float r, g, b;
//        switch (i) {
//            case 0:
//                r = value;
//                g = t;
//                b = p;
//                break;
//            case 1:
//                r = q;
//                g = value;
//                b = p;
//                break;
//            case 2:
//                r = p;
//                g = value;
//                b = t;
//                break;
//            case 3:
//                r = p;
//                g = q;
//                b = value;
//                break;
//            case 4:
//                r = t;
//                g = p;
//                b = value;
//                break;
//            case 5:
//                r = value;
//                g = p;
//                b = q;
//                break;
//            default:
//                throw new RuntimeException("HSV to RGB conversion error");
//        }
//
//        return new float[]{r, g, b};
//    }
//
//    protected float xOffset(float time) {
//        return Mth.cos(time * 0.02F) * 3.0F;
//    }
//
//    protected ResourceLocation getTextureLocation() {
//        return WITHER_ARMOR_LOCATION;
//    }
//
//    protected EntityModel<E> model() {
//        return this.model;
//    }
//}