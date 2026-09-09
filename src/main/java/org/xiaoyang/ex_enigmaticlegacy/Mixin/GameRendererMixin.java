package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.bladeflash.HeldItemTrails;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void bladeflash$afterHands(float partialTick, long finishTime, PoseStack pose, CallbackInfo ci) {
        HeldItemTrails.finishFrame();
    }
}
