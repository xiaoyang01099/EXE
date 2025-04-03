package net.xiaoyang010.ex_enigmaticlegacy.Entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

/**
 * 野猎护甲感知的AI目标基类，重写基础检查方法，防止攻击穿戴野猎护甲的玩家
 */
public abstract class WildHuntAwareGoal extends Goal {
    protected final PathfinderMob mob;
    protected final Predicate<Player> shouldIgnorePlayer;

    public WildHuntAwareGoal(PathfinderMob mob, Predicate<Player> shouldIgnorePlayer) {
        this.mob = mob;
        this.shouldIgnorePlayer = shouldIgnorePlayer;
    }

    /**
     * 检查目标是否为受保护的玩家
     */
    protected boolean isPlayerToIgnore(LivingEntity target) {
        if (target instanceof Player) {
            Player player = (Player) target;
            return shouldIgnorePlayer.test(player);
        }
        return false;
    }

    /**
     * 检查并重置目标
     */
    protected boolean checkAndResetTarget() {
        LivingEntity target = this.mob.getTarget();
        if (isPlayerToIgnore(target)) {
            this.mob.setTarget(null);
            return true;
        }
        return false;
    }

    /**
     * 野猎护甲感知的弓箭攻击目标
     */
    public static class Ranged<T extends AbstractSkeleton> extends RangedBowAttackGoal<T> {
        private final T skeleton;
        private final Predicate<Player> shouldIgnorePlayer;

        public Ranged(T skeleton, double speedModifier, int attackIntervalMin, float attackRadius, Predicate<Player> shouldIgnorePlayer) {
            super(skeleton, speedModifier, attackIntervalMin, attackRadius);
            this.skeleton = skeleton;
            this.shouldIgnorePlayer = shouldIgnorePlayer;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.skeleton.getTarget();
            if (target instanceof Player && shouldIgnorePlayer.test((Player) target)) {
                this.skeleton.setTarget(null);
                return false;
            }
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.skeleton.getTarget();
            if (target instanceof Player && shouldIgnorePlayer.test((Player) target)) {
                this.skeleton.setTarget(null);
                return false;
            }
            return super.canContinueToUse();
        }

        @Override
        public void tick() {
            LivingEntity target = this.skeleton.getTarget();
            if (target instanceof Player && shouldIgnorePlayer.test((Player) target)) {
                this.skeleton.setTarget(null);
                this.stop();
                this.skeleton.stopUsingItem();
                this.skeleton.getNavigation().stop();
                return;
            }
            super.tick();
        }
    }

    /**
     * 野猎护甲感知的近战攻击目标
     */
    public static class Melee extends MeleeAttackGoal {
        private final PathfinderMob mob;
        private final Predicate<Player> shouldIgnorePlayer;

        public Melee(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen, Predicate<Player> shouldIgnorePlayer) {
            super(mob, speedModifier, followingTargetEvenIfNotSeen);
            this.mob = mob;
            this.shouldIgnorePlayer = shouldIgnorePlayer;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            if (target instanceof Player && shouldIgnorePlayer.test((Player) target)) {
                this.mob.setTarget(null);
                return false;
            }
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.mob.getTarget();
            if (target instanceof Player && shouldIgnorePlayer.test((Player) target)) {
                this.mob.setTarget(null);
                return false;
            }
            return super.canContinueToUse();
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (target instanceof Player && shouldIgnorePlayer.test((Player) target)) {
                this.mob.setTarget(null);
                this.stop();
                return;
            }
            super.tick();
        }
    }
}