package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.coffin.CoffinVisuals;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.yuhua.YuhuaClient;

@Mixin(ClientLevel.class)
public abstract class CoffinClientLevelMixin {
    @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
    private void bladeflash$tick(Entity entity, CallbackInfo ci) {
        if (CoffinVisuals.locked(entity)) { CoffinVisuals.hold(entity); ci.cancel(); }
        if (YuhuaClient.locked(entity)) {
            YuhuaClient.hold(entity); ci.cancel();
        }
    }
}
