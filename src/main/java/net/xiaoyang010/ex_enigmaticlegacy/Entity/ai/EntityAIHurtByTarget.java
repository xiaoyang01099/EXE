package net.xiaoyang010.ex_enigmaticlegacy.Entity.ai;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;


public class EntityAIHurtByTarget extends Goal {
    private final Mob mob;
    private int revengeTimerOld;

    public EntityAIHurtByTarget(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return this.mob.getLastHurtByMobTimestamp() != this.revengeTimerOld &&
                canAttack(this.mob, this.mob.getLastHurtByMob(), false, true);
    }

    @Override
    public boolean canContinueToUse() {
        return canAttack(this.mob, this.mob.getTarget(), false,
                this.mob.getLastHurtByMob() != this.mob.getTarget());
    }

    @Override
    public void start() {
        this.mob.setTarget(this.mob.getLastHurtByMob());
        this.revengeTimerOld = this.mob.getLastHurtByMobTimestamp();
    }

    @Override
    public void stop() {
        this.mob.setTarget(null);
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