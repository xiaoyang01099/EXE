package org.xiaoyang.ex_enigmaticlegacy.Entity.others;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModItems;

public class RideablePearlEntity extends ThrowableItemProjectile {
    public RideablePearlEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public RideablePearlEntity(LivingEntity shooter, Level level) {
        super(ModEntities.RIDEABLE_PEARL_ENTITY.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.RIDEABLE_PEARL.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level.isClientSide) {
            if (!this.getPassengers().isEmpty()) {
                Entity rider = this.getPassengers().get(0);
                rider.stopRiding();
            }
            this.discard();
        }
    }
}
