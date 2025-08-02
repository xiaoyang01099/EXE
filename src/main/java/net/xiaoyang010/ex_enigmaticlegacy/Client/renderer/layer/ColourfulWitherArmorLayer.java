package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PowerableMob;

public class ColourfulWitherArmorLayer<E extends LivingEntity & PowerableMob>  extends EnergySwirlLayer<E, HumanoidModel<E>> {
    private static final ResourceLocation WITHER_ARMOR_LOCATION = new ResourceLocation("textures/entity/wither/wither_armor.png");
    private final HumanoidModel<E> model;

    public ColourfulWitherArmorLayer(RenderLayerParent<E, HumanoidModel<E>> p_174554_, EntityModelSet p_174555_) {
        super(p_174554_);
        this.model = new HumanoidModel<>(p_174555_.bakeLayer(ModelLayers.PLAYER));
    }

    public void render(PoseStack p_116970_, MultiBufferSource p_116971_, int p_116972_, E p_116973_, float p_116974_, float p_116975_, float p_116976_, float p_116977_, float p_116978_, float p_116979_) {
        if (true) {
            float f = (float) p_116973_.tickCount + p_116976_;
            float hue = (float) Util.getMillis() / 5000.0F % 1.0F;
            EntityModel<E> entitymodel = this.model();
            entitymodel.prepareMobModel(p_116973_, p_116974_, p_116975_, p_116976_);
            this.getParentModel().copyPropertiesTo(entitymodel);
            VertexConsumer vertexconsumer = p_116971_.getBuffer(RenderType.energySwirl(this.getTextureLocation(), this.xOffset(f) % 1.0F, f * 0.01F % 1.0F));
            entitymodel.setupAnim(p_116973_, p_116974_, p_116975_, p_116977_, p_116978_, p_116979_);
            //LayerColor
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

            }//Mth.clamp((int)(f4 * 255.0F) 0, 255f)
            //FinishValue
            entitymodel.renderToBuffer(p_116970_, vertexconsumer, p_116972_, OverlayTexture.NO_OVERLAY,f4,f5,f6,200.0f);
        }
    }
    //A: p_13661_ << 24 |R: p_13662_ << 16 |G: p_13663_ << 8 |B: p_13664_
    /*public int getColor(){
        float hue = (float) Util.getMillis() / 5000.0F % 1.0F;
        int c =0x00ff0000 & Mth.hsvToRgb(hue, 2.0F, 1.5F);
        return c;
    }
    public int getColorR(){
        float hue = (float) Util.getMillis() / 5000.0F % 1.0F;
        int c =0x00ff0000 & Mth.hsvToRgb(hue, 2.0F, 1.5F);
        return c>>16;
    }
    public int getColorG(){
        float hue = (float) Util.getMillis() / 5000.0F % 1.0F;
        int c =0x0000ff00 & Mth.hsvToRgb(hue, 2.0F, 1.5F);
        return c>>8;
    }public int getColorB(){
        float hue = (float) Util.getMillis() / 5000.0F % 1.0F;
        int c =0x000000ff & Mth.hsvToRgb(hue, 2.0F, 1.5F);
        return c;
    }*/
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