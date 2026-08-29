package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.Event.KeyChargeHandler;
import org.xiaoyang.ex_enigmaticlegacy.Item.MagicBowItem;

@Mixin(LocalPlayer.class)
public class MagicBowMovementMixin {
    private static final float USING_ITEM_SLOWDOWN_RECOVER_MULTIPLIER = 5.0F;

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;aiStep()V", shift = At.Shift.BEFORE))
    public void ex_enigmaticlegacy$restoreMagicBowMovementInput(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (KeyChargeHandler.shouldBlockMovement() && !player.isPassenger()) {
            player.input.leftImpulse = 0.0F;
            player.input.forwardImpulse = 0.0F;
            player.input.jumping = false;
            return;
        }
        if (KeyChargeHandler.shouldSlowMovement() && !player.isPassenger()) {
            player.input.leftImpulse *= 0.2F;
            player.input.forwardImpulse *= 0.2F;
            return;
        }
        if (!player.isUsingItem()) return;
        if (player.isPassenger()) return;
        if (!(player.getUseItem().getItem() instanceof MagicBowItem)) return;

        player.input.leftImpulse *= USING_ITEM_SLOWDOWN_RECOVER_MULTIPLIER;
        player.input.forwardImpulse *= USING_ITEM_SLOWDOWN_RECOVER_MULTIPLIER;
    }
}
