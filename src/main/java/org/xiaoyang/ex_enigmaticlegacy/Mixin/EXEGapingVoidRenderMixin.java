package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yuo.endless.client.lib.CCModel;
import com.yuo.endless.entity.GapingVoidEntity;
import com.yuo.endless.client.render.GapingVoidRender;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXEGapingVoidLateRenderQueue;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXERenderFrameState;

@Mixin(value = GapingVoidRender.class, remap = false)
public abstract class EXEGapingVoidRenderMixin extends EntityRenderer<GapingVoidEntity> {
    @Shadow
    @Final
    private CCModel hemisphere;

    protected EXEGapingVoidRenderMixin() {
        super((EntityRendererProvider.Context) null);
    }

    @Inject(
            method = "render(" + "Lcom/yuo/endless/entity/GapingVoidEntity;" + "FF" + "Lcom/mojang/blaze3d/vertex/PoseStack;" + "Lnet/minecraft/client/renderer/MultiBufferSource;" + "I" + ")V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void exe$deferGapingVoid(
            GapingVoidEntity entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        if (EXERenderFrameState.isShadowPass()) {
            ci.cancel();
            return;
        }

        if (!EXERenderFrameState.shouldDeferWorldEffect()) {
            return;
        }

        EXEGapingVoidLateRenderQueue.enqueue(
                entity,
                this.hemisphere,
                poseStack,
                partialTicks,
                packedLight
        );

        ci.cancel();
    }
}