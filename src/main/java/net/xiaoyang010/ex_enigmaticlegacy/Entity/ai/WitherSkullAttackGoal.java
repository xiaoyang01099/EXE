package net.xiaoyang010.ex_enigmaticlegacy.Entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.MiaoMiaoEntity;

import java.util.EnumSet;

public class WitherSkullAttackGoal extends Goal {
    private final MiaoMiaoEntity mob;
    private LivingEntity target;
    protected int attackTime = -1;
    private final double attackRadius;
    private final float attackInterval;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public WitherSkullAttackGoal(MiaoMiaoEntity mob, double attackRadius, float attackInterval) {
        this.mob = mob;
        this.attackRadius = attackRadius;
        this.attackInterval = attackInterval;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity != null && livingentity.isAlive()) {
            this.target = livingentity;
            double distance = this.mob.distanceToSqr(livingentity);
            return distance >= this.attackRadius * this.attackRadius; // 当距离大于设定值时启用远程攻击
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() || !this.mob.getNavigation().isDone();
    }

    @Override
    public void stop() {
        this.target = null;
        this.seeTime = 0;
        this.attackTime = -1;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        double distanceToTargetSqr = this.mob.distanceToSqr(this.target);
        boolean canSeeTarget = this.mob.getSensing().hasLineOfSight(this.target);

        if (canSeeTarget) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }

        // 更新移动策略
        if (distanceToTargetSqr <= this.attackRadius * this.attackRadius && this.seeTime >= 5) {
            this.mob.getNavigation().stop();
            ++this.strafingTime;
        } else {
            this.mob.getNavigation().moveTo(this.target, 1.0D);
            this.strafingTime = -1;
        }

        if (this.strafingTime >= 20) {
            if ((double)this.mob.getRandom().nextFloat() < 0.3D) {
                this.strafingClockwise = !this.strafingClockwise;
            }
            if ((double)this.mob.getRandom().nextFloat() < 0.3D) {
                this.strafingBackwards = !this.strafingBackwards;
            }
            this.strafingTime = 0;
        }

        if (this.strafingTime > -1) {
            if (distanceToTargetSqr > (this.attackRadius * 0.75) * (this.attackRadius * 0.75)) {
                this.strafingBackwards = false;
            } else if (distanceToTargetSqr < (this.attackRadius * 0.25) * (this.attackRadius * 0.25)) {
                this.strafingBackwards = true;
            }

            this.mob.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
            this.mob.lookAt(this.target, 30.0F, 30.0F);
        }

        // 处理攻击
        if (this.attackTime == 0) {
            if (canSeeTarget) {
                this.mob.shootWitherSkull(this.target);
                this.attackTime = (int)(this.attackInterval * 20); // 转换为tick
            }
        } else if (this.attackTime > 0) {
            --this.attackTime;
        }
    }
}
