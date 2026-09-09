package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.coffin.CoffinExecutions;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.yuhua.YuhuaExecutions;

@Mixin(ServerPlayer.class)
public abstract class CoffinServerPlayerMixin {
    @Inject(method = "doTick", at = @At("HEAD"), cancellable = true)
    private void bladeflash$tick(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (CoffinExecutions.locked(player)) { CoffinExecutions.hold(player); ci.cancel(); }
        if (YuhuaExecutions.locked(player)) {
            YuhuaExecutions.hold(player); ci.cancel();
        }
    }
}
