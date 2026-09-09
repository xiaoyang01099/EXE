package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.coffin.CoffinExecutions;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.yuhua.YuhuaExecutions;

@Mixin(LivingEntity.class)
public abstract class CoffinLivingMixin {
    @ModifyVariable(method = "setHealth", at = @At("HEAD"), argsOnly = true)
    private float bladeflash$health(float health) {
        return org.xiaoyang.ex_enigmaticlegacy.api.shader.frost.FrostEffects.health((LivingEntity) (Object) this,
                YuhuaExecutions.health((LivingEntity) (Object) this,
                CoffinExecutions.constrainedHealth((LivingEntity) (Object) this, health)));
    }
}
