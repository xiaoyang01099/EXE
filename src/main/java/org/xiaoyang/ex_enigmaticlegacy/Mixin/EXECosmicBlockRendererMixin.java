package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yuo.endless.client.render.CosmicBlockRender;
import com.yuo.endless.tiles.CosmicTile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXECosmicBlockLateRenderQueue;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXERenderFrameState;

@Mixin(value = CosmicBlockRender.class, remap = false)
public class EXECosmicBlockRendererMixin {

    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    private void exe_deferCosmicBlock(
            CosmicTile cosmicTile,
            float v,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int packedLight,
            int packedOverlay,
            CallbackInfo ci
    ) {
        if (!EXERenderFrameState.shouldDeferWorldEffect()) {
            return;
        }

        BlockState blockState = cosmicTile.getBlockState();

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.scale(1.0011123F, 1.0011123F, 1.0011123F);
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        EXECosmicBlockLateRenderQueue.enqueue(
                blockState,
                poseStack,
                packedLight,
                packedOverlay
        );

        poseStack.popPose();
        ci.cancel();
    }
}