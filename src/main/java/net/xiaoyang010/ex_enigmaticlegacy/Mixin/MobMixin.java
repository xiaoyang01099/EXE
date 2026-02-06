package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EntityRidingData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void preventTargetingWhenPlayerOnTop(LivingEntity target, CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;

        if (EntityRidingData.hasPlayerOnTop(mob.getUUID())) {
            ci.cancel();
        }
    }
}