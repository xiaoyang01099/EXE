package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Model.SpecialRenderHelper;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.SpecialLateRenderQueue;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.block_entity.PylonBlockEntityRenderer;

@Mixin(value = PylonBlockEntityRenderer.class, remap = false)
public abstract class PylonBlockEntityRendererMixin {
    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
                    remap = true
            )
    )
    private VertexConsumer exEnigmaticLegacy$redirectPylonBuffer(
            MultiBufferSource buffers, RenderType type) {
        if (!SpecialLateRenderQueue.shouldDefer()) {
            return buffers.getBuffer(type);
        }

        if (type == RenderHelper.MANA_PYLON_GLOW) {
            return SpecialLateRenderQueue.getBuffer(
                    SpecialRenderHelper.MANA_PYLON_GLOW_AFTER_LEVEL
            );
        }
        if (type == RenderHelper.NATURA_PYLON_GLOW) {
            return SpecialLateRenderQueue.getBuffer(
                    SpecialRenderHelper.NATURA_PYLON_GLOW_AFTER_LEVEL
            );
        }
        if (type == RenderHelper.GAIA_PYLON_GLOW) {
            return SpecialLateRenderQueue.getBuffer(
                    SpecialRenderHelper.GAIA_PYLON_GLOW_AFTER_LEVEL
            );
        }

        return buffers.getBuffer(type);
    }
}