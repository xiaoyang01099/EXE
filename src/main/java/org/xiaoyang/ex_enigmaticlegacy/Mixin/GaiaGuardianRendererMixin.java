package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.GaiaGuardianLateRenderQueue;
import vazkii.botania.client.render.entity.GaiaGuardianRenderer;
import vazkii.botania.common.entity.GaiaGuardianEntity;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = GaiaGuardianRenderer.class, remap = false)
public abstract class GaiaGuardianRendererMixin implements GaiaGuardianRendererAccessor<GaiaGuardianEntity, HumanoidModel<GaiaGuardianEntity>> {

    @Shadow
    public abstract ResourceLocation getTextureLocation(GaiaGuardianEntity entity);

    @Inject(
            method = "render(Lvazkii/botania/common/entity/GaiaGuardianEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void exe$deferDopplegangerRender(
            GaiaGuardianEntity entity,
            float yaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            CallbackInfo ci
    ) {
        if (!GaiaGuardianLateRenderQueue.shouldDefer()) {
            return;
        }

        int invulTime = entity.getInvulTime();
        float grainIntensity;
        float disfiguration;

        if (invulTime > 0) {
            grainIntensity = invulTime > 20 ? 1.0F : (float) invulTime * 0.05F;
            disfiguration = grainIntensity * 0.3F;
        } else {
            disfiguration = (0.025F + (float) entity.hurtTime * 0.0425F) / 2.0F;
            grainIntensity = 0.05F + (float) entity.hurtTime * 0.085F;
        }

        ResourceLocation texture = getTextureLocation(entity);

        HumanoidModel<GaiaGuardianEntity> model = exe$getModel();
        List<RenderLayer<GaiaGuardianEntity, HumanoidModel<GaiaGuardianEntity>>> layers = exe$getLayers();

        List<RenderLayer<GaiaGuardianEntity, HumanoidModel<GaiaGuardianEntity>>> layersCopy = new ArrayList<>(layers);

        GaiaGuardianLateRenderQueue.enqueue(
                entity,
                model,
                layersCopy,
                texture,
                poseStack,
                yaw,
                partialTicks,
                packedLight,
                grainIntensity,
                disfiguration
        );

        ci.cancel();
    }
}