package net.xiaoyang010.ex_enigmaticlegacy.Mixin;


import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Effect.Drowning;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;

@Mixin(WitherBoss.class)
public abstract class WitherBossMixin extends LivingEntity {

    protected WitherBossMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * @author xiaoyang001
     * @reason 修改凋零被赋予该效果
     */
    @Overwrite
    public boolean addEffect(MobEffectInstance pEffectInstance, @Nullable Entity pEntity) {
        if (pEffectInstance.getEffect() instanceof Drowning) {
            return super.addEffect(pEffectInstance, pEntity);
        }
        return false;
    }
}