package net.xiaoyang010.ex_enigmaticlegacy.Entity.ai;

import java.util.EnumSet;
import java.util.function.Predicate;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

public class EntityAIFindEntityNearest<T extends LivingEntity> extends Goal {
    protected final Mob mob;
    private final Predicate<LivingEntity> predicate;
    private final TargetingConditions targetConditions;
    private LivingEntity target;
    private final Class<T> classToCheck;

    public EntityAIFindEntityNearest(Mob mobIn, Class<T> classToCheck, Predicate<LivingEntity> predicate) {
        this.mob = mobIn;
        this.classToCheck = classToCheck;
        this.predicate = predicate;
        this.targetConditions = TargetingConditions.forNonCombat().range(this.getFollowRange()).selector(predicate);
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isInWater() || this.mob.getRandom().nextInt(10) != 0) {
            return false;
        }

        if (this.classToCheck == Player.class) {
            this.target = this.mob.level.getNearestPlayer(this.targetConditions, this.mob,
                    this.mob.getX(), this.mob.getY(), this.mob.getZ());
        } else {
            this.target = this.mob.level.getNearestEntity(
                    this.mob.level.getEntitiesOfClass(this.classToCheck,
                            this.mob.getBoundingBox().inflate(this.getFollowRange()),
                            entity -> true),
                    this.targetConditions,
                    this.mob,
                    this.mob.getX(),
                    this.mob.getEyeY(),
                    this.mob.getZ()
            );
        }

        return this.target != null && this.canAttack(this.mob, this.target, false, true);
    }

    @Override
    public boolean canContinueToUse() {
        if (!canAttack(this.mob, this.target, false, false)) {
            return false;
        }

        double followRange = this.getFollowRange();
        if (this.mob.distanceToSqr(this.target) > followRange * followRange) {
            return false;
        }

        return this.predicate.test(this.target);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.target);
    }

    @Override
    public void stop() {
        this.mob.setTarget(null);
        this.target = null;
    }

    private double getFollowRange() {
        return this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    private static boolean canAttack(Mob attacker, LivingEntity target, boolean requiresSightline, boolean requiresLineOfSight) {
        if (target == null) {
            return false;
        } else if (!target.isAlive()) {
            return false;
        } else if (!attacker.canAttack(target)) {
            return false;
        } else {
            if (requiresSightline && !attacker.getSensing().hasLineOfSight(target)) {
                return false;
            }

            if (requiresLineOfSight) {
                if (attacker.isPassenger()) {
                    return false;
                }

                if (attacker.distanceToSqr(target) > 16.0D) {
                    return false;
                }
            }

            return true;
        }
    }
}