package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccess {
    @Accessor("DATA_HEALTH_ID")
    static EntityDataAccessor<Float> bladeflash$healthKey() { throw new AssertionError(); }
}
