package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.coffin.CoffinExecutions;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.yuhua.YuhuaExecutions;

@Mixin(value = ForgeHooks.class, remap = false)
public abstract class ForgeHooksMixin {
    @Inject(method = "onLivingDamage", at = @At("RETURN"), cancellable = true)
    private static void bladeflash$damage(LivingEntity entity, DamageSource source, float amount,
                                         CallbackInfoReturnable<Float> cir) {
        float result = YuhuaExecutions.damage(entity, source, cir.getReturnValue());
        if (!YuhuaExecutions.locked(entity)
                && !(source.getDirectEntity() instanceof LivingEntity owner && YuhuaExecutions.weapon(owner)))
            result = CoffinExecutions.damage(entity, source, result);
        cir.setReturnValue(result);
    }

    @Inject(method = "onLivingDeath", at = @At("RETURN"), cancellable = true)
    private static void bladeflash$death(LivingEntity entity, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (CoffinExecutions.executing(entity) || YuhuaExecutions.executing(entity)) cir.setReturnValue(false);
        else if (CoffinExecutions.locked(entity) || YuhuaExecutions.locked(entity)) cir.setReturnValue(true);
    }
}
