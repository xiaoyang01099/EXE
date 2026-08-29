package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.Event.AutoTrackingClientHandler;
import org.xiaoyang.ex_enigmaticlegacy.Event.SparklingClientHandler;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline.MiaoOutlineCaptureOnlyBufferSource;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Shadow
    public abstract <E extends Entity> EntityRenderer<? super E> getRenderer(E entity);

    @Inject(
            method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    )
    public void ex_enigmaticlegacy$captureOutlineVertices(
            Entity entity,
            double x,
            double y,
            double z,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            CallbackInfo callbackInfo
    ) {
        if (entity.getId() != AutoTrackingClientHandler.getLockedTargetId()
                && !SparklingClientHandler.shouldCaptureSparklingOutline(entity)) return;

        MiaoOutlineCaptureOnlyBufferSource captureBufferSource = new MiaoOutlineCaptureOnlyBufferSource(entity.getId());
        EntityRenderer<? super Entity> renderer = getRenderer(entity);
        renderer.render(entity, entityYaw, partialTick, poseStack, captureBufferSource, packedLight);
    }
}
