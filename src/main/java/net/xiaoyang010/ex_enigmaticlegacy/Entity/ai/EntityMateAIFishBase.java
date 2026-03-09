package net.xiaoyang010.ex_enigmaticlegacy.Entity.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.core.Registry;
import net.minecraft.core.BlockPos;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.EntityFishBase;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class EntityMateAIFishBase extends Goal {
    private final EntityFishBase animal;
    private final Class<? extends EntityFishBase> mateClass;
    Level level;
    private EntityFishBase targetMate;
    int spawnBabyDelay;
    double moveSpeed;

    public EntityMateAIFishBase(EntityFishBase animal, double speedIn) {
        this(animal, speedIn, animal.getClass());
    }

    public EntityMateAIFishBase(EntityFishBase animal, double speedIn, Class<? extends EntityFishBase> mateClass) {
        this.animal = animal;
        this.level = animal.level;
        this.mateClass = mateClass;
        this.moveSpeed = speedIn;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.animal.isInLove()) {
            return false;
        } else {
            this.targetMate = this.getNearbyMate();
            return this.targetMate != null;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetMate.isAlive() && this.targetMate.isInLove() && this.spawnBabyDelay < 60;
    }

    @Override
    public void stop() {
        this.targetMate = null;
        this.spawnBabyDelay = 0;
    }

    @Override
    public void tick() {
        this.animal.getLookControl().setLookAt(this.targetMate, 10.0F, (float)this.animal.getMaxHeadXRot());
        this.animal.getNavigation().moveTo(this.targetMate, this.moveSpeed);
        ++this.spawnBabyDelay;

        if (this.spawnBabyDelay >= 60 && this.animal.distanceToSqr(this.targetMate) < 9.0D) {
            this.spawnBaby();
        }
    }

    private EntityFishBase getNearbyMate() {
        List<? extends EntityFishBase> list = this.level.getEntitiesOfClass(
                this.mateClass,
                this.animal.getBoundingBox().inflate(8.0D)
        );

        double d0 = Double.MAX_VALUE;
        EntityFishBase nearestMate = null;

        for (EntityFishBase potentialMate : list) {
            if (this.animal.canMateWith(potentialMate) && this.animal.distanceToSqr(potentialMate) < d0) {
                nearestMate = potentialMate;
                d0 = this.animal.distanceToSqr(potentialMate);
            }
        }

        return nearestMate;
    }

    private void spawnBaby() {
        ItemStack itemstack = this.animal.getPropagule();
        if (!itemstack.hasTag()) {
            itemstack.setTag(new CompoundTag());
        }
        Random random = this.animal.getRandom();

        // 获取实体的注册表名称
        String stringEgg = Registry.ENTITY_TYPE.getKey(this.animal.getType()).toString();
        itemstack.getTag().putString("creature", stringEgg);

        BlockPos pos = this.animal.blockPosition();
        ItemEntity entityToSpawn = new ItemEntity(
                level,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                itemstack
        );

        entityToSpawn.setPickUpDelay(10);
        this.animal.playSound(SoundEvents.CHICKEN_EGG, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
        level.addFreshEntity(entityToSpawn);

        this.animal.resetLove();
        this.targetMate.resetLove();
        this.animal.setTicks(0);
        this.animal.setNotMateable();
        this.targetMate.setNotMateable();

        // 生成心形粒子
        for (int i = 0; i < 7; ++i) {
            double d0 = random.nextGaussian() * 0.02D;
            double d1 = random.nextGaussian() * 0.02D;
            double d2 = random.nextGaussian() * 0.02D;
            double d3 = random.nextDouble() * (double)this.animal.getBbWidth() * 2.0D - (double)this.animal.getBbWidth();
            double d4 = 0.5D + random.nextDouble() * (double)this.animal.getBbHeight();
            double d5 = random.nextDouble() * (double)this.animal.getBbWidth() * 2.0D - (double)this.animal.getBbWidth();

            this.level.addParticle(
                    ParticleTypes.HEART,
                    this.animal.getX() + d3,
                    this.animal.getY() + d4,
                    this.animal.getZ() + d5,
                    d0, d1, d2
            );
        }

        if (this.level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT)) {
            this.level.addFreshEntity(new ExperienceOrb(
                    this.level,
                    this.animal.getX(),
                    this.animal.getY(),
                    this.animal.getZ(),
                    random.nextInt(7) + 1
            ));
        }
    }
}