package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.coffin.CoffinExecutions;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.yuhua.YuhuaExecutions;

@Mixin(Creeper.class)
public abstract class CoffinCreeperMixin {
    @Inject(method = "explodeCreeper", at = @At("HEAD"), cancellable = true)
    private void bladeflash$explode(CallbackInfo ci) {
        if (CoffinExecutions.locked((Creeper) (Object) this)) ci.cancel();
        if (YuhuaExecutions.locked((Creeper) (Object) this)) ci.cancel();
    }
}
