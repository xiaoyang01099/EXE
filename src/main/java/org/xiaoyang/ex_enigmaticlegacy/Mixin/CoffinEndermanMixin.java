package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.coffin.CoffinExecutions;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.yuhua.YuhuaExecutions;

@Mixin(EnderMan.class)
public abstract class CoffinEndermanMixin {
    @Inject(method = "teleport(DDD)Z", at = @At("HEAD"), cancellable = true)
    private void bladeflash$teleport(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (CoffinExecutions.locked((EnderMan) (Object) this)) cir.setReturnValue(false);
        if (YuhuaExecutions.locked((EnderMan) (Object) this)) cir.setReturnValue(false);
    }
}
