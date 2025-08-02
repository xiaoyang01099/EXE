package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.SpectriteWither;

public class SpectriteWitherRenderer extends LivingEntityRenderer<SpectriteWither, WitherBossModel<SpectriteWither>> {

    private static final ResourceLocation[] WITHER_NORMAL_FRAMES = new ResourceLocation[]{
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/0.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/1.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/2.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/3.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/4.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/5.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/6.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/7.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/8.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/9.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/10.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/11.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/12.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/13.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/14.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/15.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/16.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/17.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/18.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/19.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/20.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/21.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/22.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/23.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/24.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/25.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/26.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/27.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/28.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/29.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/30.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/31.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/32.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/33.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/34.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/normal/35.png")
    };

    private static final ResourceLocation[] WITHER_INVULNERABLE_FRAMES = new ResourceLocation[]{
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/0.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/1.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/2.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/3.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/4.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/5.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/6.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/7.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/8.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/9.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/10.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/11.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/12.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/13.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/14.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/15.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/16.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/17.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/18.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/19.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/20.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/21.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/22.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/23.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/24.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/25.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/26.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/27.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/28.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/29.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/30.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/31.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/32.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/33.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/34.png"),
            new ResourceLocation("ex_enigmaticlegacy", "textures/entity/spectrite_wither/invulnerable/35.png")
    };

    private static final int ANIMATION_FRAME_COUNT = WITHER_NORMAL_FRAMES.length;
    private static final int FRAME_TIME = 2;



    //目前凋零无敌模式无法实现，但是模型纹理应该是没有问题的，然后就是发射的头颅，我不会做，所以纹理没有做。到时候你看看
    public SpectriteWitherRenderer(EntityRendererProvider.Context context) {
        super(context, new WitherBossModel<>(context.bakeLayer(ModelLayers.WITHER)), 1.0F);
        //this.addLayer(new WitherArmorLayer(this, context.getModelSet())); // 确保传递正确的渲染器和模型集
    }

    @Override
    public ResourceLocation getTextureLocation(SpectriteWither entity) {
        int invulnerableTicks = entity.getInvulnerableTicks();

        // 计算当前帧
        long time = entity.tickCount;
        int frameIndex = (int)((time / FRAME_TIME) % ANIMATION_FRAME_COUNT);

        if (invulnerableTicks > 0 && (invulnerableTicks > 80 || invulnerableTicks / 5 % 2 != 1)) {
            // 渲染无敌状态下的帧
            return WITHER_INVULNERABLE_FRAMES[frameIndex];
        } else {
            // 渲染正常状态下的帧
            return WITHER_NORMAL_FRAMES[frameIndex];
        }
    }

    @Override
    protected int getBlockLightLevel(SpectriteWither entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    protected void scale(SpectriteWither entity, PoseStack poseStack, float partialTicks) {
        float scaleValue = 2.0F;
        int invulnerableTicks = entity.getInvulnerableTicks();
        if (invulnerableTicks > 0) {
            scaleValue -= ((float) invulnerableTicks - partialTicks) / 220.0F * 0.5F;
        }
        poseStack.scale(scaleValue, scaleValue, scaleValue);
    }
}
