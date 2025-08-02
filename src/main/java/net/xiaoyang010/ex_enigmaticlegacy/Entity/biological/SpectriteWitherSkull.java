package net.xiaoyang010.ex_enigmaticlegacy.Entity.biological;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class SpectriteWitherSkull extends WitherSkull {

    public SpectriteWitherSkull(EntityType<? extends WitherSkull> entityType, Level level) {
        super(entityType, level);
    }

    public SpectriteWitherSkull(Level level, LivingEntity shooter, double accelX, double accelY, double accelZ) {
        super(level, shooter, accelX, accelY, accelZ);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level.isClientSide) {
            // 取消爆炸伤害，改为真实伤害
            if (result.getEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) result.getEntity();
                target.hurt(DamageSource.mobAttack((LivingEntity) this.getOwner()), 50.0F);
            }
            this.discard();  // 当击中后移除骷髅头
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        // 禁用爆炸效果
        this.discard();  // 当击中方块后移除骷髅头
    }
}
