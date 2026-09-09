package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.coffin.CoffinExecutions;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.yuhua.YuhuaExecutions;

@Mixin(ServerLevel.class)
public abstract class CoffinServerLevelMixin {
    @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
    private void bladeflash$tick(Entity entity, CallbackInfo ci) {
        if (CoffinExecutions.locked(entity)) { CoffinExecutions.hold(entity); ci.cancel(); }
        if (YuhuaExecutions.locked(entity)) { YuhuaExecutions.hold(entity); ci.cancel(); }
    }

    @Inject(method = "tickPassenger", at = @At("HEAD"), cancellable = true)
    private void bladeflash$passenger(Entity vehicle, Entity passenger, CallbackInfo ci) {
        if (CoffinExecutions.locked(passenger)) { CoffinExecutions.hold(passenger); ci.cancel(); }
        if (YuhuaExecutions.locked(passenger)) { YuhuaExecutions.hold(passenger); ci.cancel(); }
    }
}
