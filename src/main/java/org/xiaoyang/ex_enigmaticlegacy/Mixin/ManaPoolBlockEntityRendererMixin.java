package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.xiaoyang.ex_enigmaticlegacy.Client.help.EXERenderHelper;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.SpecialLateRenderQueue;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.block_entity.ManaPoolBlockEntityRenderer;

@Mixin(value = ManaPoolBlockEntityRenderer.class, remap = false)
public abstract class ManaPoolBlockEntityRendererMixin {
    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
                    remap = true
            )
    )
    private VertexConsumer exEnigmaticLegacy$redirectManaPoolBuffer(
            MultiBufferSource buffers, RenderType type) {
        if (type == RenderHelper.MANA_POOL_WATER && SpecialLateRenderQueue.shouldDefer()) {
            return SpecialLateRenderQueue.getBuffer(
                    EXERenderHelper.MANA_POOL_WATER_AFTER_LEVEL
            );
        }
        return buffers.getBuffer(type);
    }
}