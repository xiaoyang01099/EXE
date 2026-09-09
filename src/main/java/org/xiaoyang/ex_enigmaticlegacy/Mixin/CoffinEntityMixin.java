package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.coffin.CoffinExecutions;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.yuhua.YuhuaExecutions;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.vine.VineEffects;

@Mixin(Entity.class)
public abstract class CoffinEntityMixin {
    @Inject(method = {"move", "push(DDD)V"}, at = @At("HEAD"), cancellable = true)
    private void bladeflash$movement(CallbackInfo ci) {
        if (CoffinExecutions.locked((Entity) (Object) this)
                || YuhuaExecutions.locked((Entity) (Object) this)
                || VineEffects.locked((Entity) (Object) this)) ci.cancel();
    }

    @Inject(method = "setPos(DDD)V", at = @At("HEAD"), cancellable = true)
    private void bladeflash$position(double x, double y, double z, CallbackInfo ci) {
        var entry = CoffinExecutions.entry((Entity) (Object) this);
        if (entry != null && entry.anchor.distanceToSqr(x, y, z) > 1e-12) ci.cancel();
        var jade = YuhuaExecutions.entry((Entity) (Object) this);
        if (jade != null && jade.stone && jade.anchor.distanceToSqr(x, y, z) > 1e-12) ci.cancel();
        if (VineEffects.blocksPosition((Entity) (Object) this, x, y, z)) ci.cancel();
    }
}
