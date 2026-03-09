package net.xiaoyang010.ex_enigmaticlegacy.Entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.CatMewEntity;

import java.util.EnumSet;

public class WitherSkullAttackGoal extends Goal {
    private final CatMewEntity mob;
    private LivingEntity target;
    protected int attackTime = -1;
    private final double attackRadius;
    private final float attackInterval;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public WitherSkullAttackGoal(CatMewEntity mob, double attackRadius, float attackInterval) {
        this.mob = mob;
        this.attackRadius = attackRadius;
        this.attackInterval = attackInterval;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity != null && livingentity.isAlive()) {
            this.target = livingentity;
            double sqrt = Math.sqrt(this.mob.getOnPos().distToCenterSqr(target.getX(), target.getY(), target.getZ()));
            return sqrt > attackRadius;
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
        if (this.target == null) return;

        double distanceToTargetSqr = this.mob.distanceToSqr(this.target);
        boolean canSeeTarget = this.mob.getSensing().hasLineOfSight(this.target);

        if (canSeeTarget) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }

        // 计算视线方向
        double deltaX = this.target.getX() - this.mob.getX();
        double deltaY = this.target.getY() + this.target.getEyeHeight() - (this.mob.getY() + this.mob.getEyeHeight());
        double deltaZ = this.target.getZ() - this.mob.getZ();

        // 计算yRot和xRot
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yRot = (float) ((Math.atan2(deltaZ, deltaX) * 180.0D / Math.PI) - 90.0F);
        float xRot = (float) (-(Math.atan2(deltaY, horizontalDistance) * 180.0D / Math.PI));

        // 更新实体的朝向
        this.mob.setYRot(this.rotlerp(this.mob.getYRot(), yRot, 30.0F));
        this.mob.setXRot(this.rotlerp(this.mob.getXRot(), xRot, 30.0F));

        // 更新移动策略
        if (distanceToTargetSqr <= this.attackRadius * this.attackRadius && this.seeTime >= 5) {
            this.mob.getNavigation().stop();
            ++this.strafingTime;
        } else {
            this.mob.getNavigation().moveTo(this.target, 1.0D);
            this.strafingTime = -1;
        }

        if (this.strafingTime >= 20) {
            if (this.mob.getRandom().nextFloat() < 0.3D) {
                this.strafingClockwise = !this.strafingClockwise;
            }
            if (this.mob.getRandom().nextFloat() < 0.3D) {
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
        }

        // 处理攻击
        if (this.attackTime == 0) {
            if (canSeeTarget && distanceToTargetSqr <= this.attackRadius * this.attackRadius) {
                this.mob.shootWitherSkull(this.target);
                this.attackTime = (int)(this.attackInterval * 20);
            }
        } else if (this.attackTime > 0) {
            --this.attackTime;
        }

        // 确保头部朝向目标
        this.mob.getLookControl().setLookAt(
                this.target.getX(),
                this.target.getY() + this.target.getEyeHeight(),
                this.target.getZ(),
                30.0F,
                30.0F
        );
    }

    private float rotlerp(float current, float target, float maxDelta) {
        float delta = (target - current) % 360.0F;
        if (delta > 180.0F) {
            delta -= 360.0F;
        }
        if (delta < -180.0F) {
            delta += 360.0F;
        }

        if (delta > maxDelta) {
            delta = maxDelta;
        }
        if (delta < -maxDelta) {
            delta = -maxDelta;
        }

        return current + delta;
    }
}