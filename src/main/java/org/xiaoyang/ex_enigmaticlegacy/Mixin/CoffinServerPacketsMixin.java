package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.coffin.CoffinExecutions;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.yuhua.YuhuaExecutions;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.vine.VineEffects;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class CoffinServerPacketsMixin {
    @Shadow public ServerPlayer player;
    @Shadow private boolean clientIsFloating;
    @Shadow private boolean clientVehicleIsFloating;

    @Inject(method = {"handleMovePlayer", "handleMoveVehicle", "handlePlayerInput", "handleInteract",
        "handlePlayerAction", "handleUseItem", "handleUseItemOn", "handlePlayerCommand", "handleAnimate"},
        at = @At("HEAD"), cancellable = true)
    private void bladeflash$input(CallbackInfo ci) {
        if (player.server.isSameThread() && (CoffinExecutions.locked(player) || YuhuaExecutions.locked(player))) ci.cancel();
    }

    @Inject(method = {"handleMovePlayer", "handleMoveVehicle", "handlePlayerInput"},
        at = @At("HEAD"), cancellable = true)
    private void vine$movement(CallbackInfo ci) {
        if (player.server.isSameThread() && VineEffects.locked(player)) ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void bladeflash$floating(CallbackInfo ci) {
        if (CoffinExecutions.locked(player) || YuhuaExecutions.locked(player) || VineEffects.locked(player)) {
            clientIsFloating = false; clientVehicleIsFloating = false;
        }
    }
}
