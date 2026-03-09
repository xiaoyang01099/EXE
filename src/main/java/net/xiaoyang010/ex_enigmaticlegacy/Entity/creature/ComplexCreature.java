package net.xiaoyang010.ex_enigmaticlegacy.Entity.creature;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.api.IComplexMob;

public abstract class ComplexCreature extends AquaticCreature implements IComplexMob {
    private static final EntityDataAccessor<Integer> TICKS_EXISTED = SynchedEntityData.defineId(ComplexCreature.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> LIMB_SWING = SynchedEntityData.defineId(ComplexCreature.class, EntityDataSerializers.FLOAT);
    protected int moveTick;

    public ComplexCreature(EntityType<? extends ComplexCreature> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TICKS_EXISTED, 0);
        this.entityData.define(LIMB_SWING, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return this.getTarget() == null;
    }

    @Override
    public void tick() {
        double prevX = this.getX();
        double prevY = this.getY();
        double prevZ = this.getZ();
        super.tick();

        if (!this.level.isClientSide) {
            this.entityData.set(TICKS_EXISTED, this.tickCount);
            this.entityData.set(LIMB_SWING, this.animationSpeed);
        } else {
            this.tickCount = this.entityData.get(TICKS_EXISTED);
            if (this.dPosX * this.dPosX + this.dPosY * this.dPosY + this.dPosZ * this.dPosZ < 1.6000001778593287E-5D || this.tickCount % 100 == 0) {
                this.animationSpeed = this.entityData.get(LIMB_SWING);
            }
        }

        this.dPosX = this.getX() - prevX;
        this.dPosY = this.getY() - prevY;
        this.dPosZ = this.getZ() - prevZ;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.updateSwingTime();
        if (!this.isInWater()) {
            if (this.isOnGround()) {
                this.currentPitch *= 0.6F;
            } else {
                double d = Math.sqrt(this.dPosX * this.dPosX + this.dPosZ * this.dPosZ);
                float pitch = -((float)Math.atan2(this.dPosY, d)) * (180F / (float)Math.PI);
                this.currentPitch += (pitch - this.currentPitch) * 0.25F;
            }
        } else {
            this.currentPitch += (this.getXRot() - this.currentPitch) * 0.1F;
        }

        this.updateParts();
    }

//    @Override
//    protected void moveCreature() {
//        double targetSpeed = 0.0D;
//        LivingEntity target = this.getTarget();
//        if (target != null) {
//            ++this.moveTick;
//            targetSpeed = this.moveByTarget(target);
//        } else if (this.targetVec != null) {
//            ++this.moveTick;
//            targetSpeed = this.moveByPathing();
//        } else if (this.noActionTime < 100 && this.findNewPath() && this.tickCount > 20) {
//            this.setRandomPath();
//        }
//
//        Vec3 vec = this.getViewVector(1.0F);
//        this.netSpeed += (targetSpeed - this.netSpeed) * 0.1D;
//        float dYaw = Math.abs(this.currentYaw - this.prevCurrentYaw);
//        if (dYaw > 0.02F && this.netSpeed > targetSpeed * 0.6D) {
//            this.netSpeed += (targetSpeed * 0.6D - this.netSpeed) * 0.4D;
//        }
//
//        if (this.onLand()) {
//            this.netSpeed = 0.0D;
//            if (this.targetVec != null && this.random.nextInt(12) == 0) {
//                this.setDeltaMovement(
//                        vec.x * 0.25D,
//                        0.20000000298023224D,
//                        vec.z * 0.25D
//                );
//            }
//        }
//
//        this.setDeltaMovement(vec.x * this.netSpeed, vec.y * this.netSpeed, vec.z * this.netSpeed);
//        this.move(MoverType.SELF, this.getDeltaMovement());
//    }

    protected void moveCreature() {
        double targetSpeed = 0.0D;
        LivingEntity target = this.getTarget();

        if (target != null) {
            ++this.moveTick;
            // 判断是否在水中
            if (this.isInWater()) {
                targetSpeed = this.moveByTarget(target);
            } else {
                // 陆地上的移动速度应该降低
                targetSpeed = this.moveByTarget(target) * 0.3D;
            }
        } else if (this.targetVec != null) {
            ++this.moveTick;
            targetSpeed = this.moveByPathing();
        } else if (this.noActionTime < 100 && this.findNewPath() && this.tickCount > 20) {
            this.setRandomPath();
        }

        // 应用视角向量移动
        Vec3 viewVec = this.getViewVector(1.0F);
        this.netSpeed += (targetSpeed - this.netSpeed) * 0.1D;

        // 检查转向角度，如果转向太大则降低速度
        float dYaw = Math.abs(this.currentYaw - this.prevCurrentYaw);
        if (dYaw > 0.02F && this.netSpeed > targetSpeed * 0.6D) {
            this.netSpeed += (targetSpeed * 0.6D - this.netSpeed) * 0.4D;
        }

        // 在陆地上的处理
        if (this.onLand()) {
            this.netSpeed = Math.min(this.netSpeed, 0.1D); // 限制陆地速度
            if (this.targetVec != null && this.random.nextInt(12) == 0) {
                // 尝试回到水中
                this.setDeltaMovement(
                        viewVec.x * 0.25D,
                        0.20000000298023224D,
                        viewVec.z * 0.25D
                );
            }
        }

        // 在水中的移动
        if (this.isInWater()) {
            this.setDeltaMovement(
                    viewVec.x * this.netSpeed,
                    viewVec.y * this.netSpeed,
                    viewVec.z * this.netSpeed
            );
        } else {
            // 陆地上的移动，考虑重力
            this.setDeltaMovement(
                    viewVec.x * this.netSpeed * 0.3D,
                    this.getDeltaMovement().y - 0.08D, // 添加重力
                    viewVec.z * this.netSpeed * 0.3D
            );
        }

        // 进行实际的移动，同时考虑碰撞
        this.move(MoverType.SELF, this.getDeltaMovement());

        // 处理碰撞后的反弹
        if (this.horizontalCollision && !this.isInWater()) {
            this.setDeltaMovement(
                    this.getDeltaMovement().x,
                    0.2D,
                    this.getDeltaMovement().z
            );
        }
    }

    public double moveByTarget(LivingEntity target) {
        return 0.0D;
    }

    public double moveByPathing() {
        this.getLookControl().setLookAt(this.targetVec.x, this.targetVec.y, this.targetVec.z, 3.0F, 85.0F);
        if (this.horizontalCollision && this.getDeltaMovement().y < 0.20000000298023224D) {
            this.setDeltaMovement(this.getDeltaMovement().x, 0.20000000298023224D, this.getDeltaMovement().z);
        }

        if (this.targetVec.distanceTo(this.position()) < 3.0D || this.moveTick > 120) {
            this.moveTick = 0;
            if (this.random.nextInt(5) < 3) {
                this.setRandomPath();
            } else {
                this.targetVec = null;
            }
        }

        return this.getMovementSpeed();
    }

    public final double getMovementSpeed() {
        return this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.8D;
    }

    @Override
    public boolean isInWaterRainOrBubble() {
        AABB box = this.getBoundingBox();
        return this.level.containsAnyLiquid(box) ||
                this.level.containsAnyLiquid(box.deflate(0.0D, 0.3D, 0.0D));
    }

    @Override
    public boolean isInWater() {
        AABB box = this.getBoundingBox();
        return this.level.containsAnyLiquid(box) ||
                (this.wasEyeInWater && this.level.containsAnyLiquid(box.deflate(0.0D, 0.3D, 0.0D)));
    }

    public abstract int getExperienceValue();

    @Override
    public abstract BodyPart[] getParts();

    public abstract Bone getBaseBone();

    public abstract Bone[] getBoneList();

    protected void updateParts() {
        this.getBaseBone().setRotation(this.currentPitch, this.getYRot(), 0.0F);
        this.resetBoneAngles();
        this.updatePitchRotations(1.0F);
        this.updateYawRotations(1.0F);
        this.setBodyPartPositions();
    }

    public void updatePitchRotations(float partialTick) {
    }

    public void updateYawRotations(float partialTick) {
    }

    private void setBodyPartPositions() {
        BodyPart[] partList = this.getParts();
        Bone baseBone = this.getBaseBone();
        Bone[] boneList = this.getBoneList();
        Vec3 vec = boneList[0].getRotation().getRotated(baseBone.getRotation()).rotateVector(boneList[0].getLength() / 2.0F);
        this.level.broadcastEntityEvent(partList[0], (byte)3);
        partList[0].setPos(this.getX() + vec.x, this.getY() + vec.y, this.getZ() + vec.z);
        partList[0].setRot(this.getYRot(), this.getXRot());
        vec = baseBone.getRotatedVector();
        Euler angle = baseBone.getRotation();

        for (int i = 1; i < partList.length; ++i) {
            angle = angle.getRotated(boneList[i].getRotation());
            float length = boneList[i].getLength();
            Vec3 midVec = angle.rotateVector(length / 2.0F);
            this.level.broadcastEntityEvent(partList[i], (byte)3);
            partList[i].setPos(this.getX() + vec.x + midVec.x, this.getY() + vec.y + midVec.y, this.getZ() + vec.z + midVec.z);
            partList[i].setRot(this.getYRot(), this.getXRot());
            Vec3 target = angle.rotateVector(length);
            vec = vec.add(target.x, target.y, target.z);
        }
    }

    public void resetBoneAngles() {
        for (Bone bone : this.getBoneList()) {
            bone.setRotation(0.0F, 0.0F, 0.0F);
        }
    }

    public void collideWithEntity(BodyPart part, Entity entity) {
    }

    public void eatOrDamageEntity(Entity target, float dmg) {
        if (target instanceof LivingEntity && target instanceof Fish) {
            LivingEntity living = (LivingEntity)target;
            if (living.getHealth() - dmg < 0.0F) {
                this.swing(InteractionHand.MAIN_HAND);
                living.kill();
                this.heal(1.0F);
            }
        }

        if (target.isAlive()) {
            target.hurt(DamageSource.mobAttack(this), dmg);
        }
    }

    public void eatOrDamageEntity(BodyPart part, Entity target, float dmg) {
        if (target instanceof LivingEntity && target.getBbWidth() < this.getBbWidth() + 0.5F && target.getBbHeight() < this.getBbHeight() + 0.5F) {
            LivingEntity living = (LivingEntity)target;
            if (living.getHealth() - dmg < 0.0F && (part == this.getParts()[0] || part == this.getParts()[1])) {
                this.swing(InteractionHand.MAIN_HAND);
                living.hurt(DamageSource.mobAttack(this), 0.0F);
                living.kill();
            }
        }

        if (target.isAlive()) {
            if (target.hurt(DamageSource.mobAttack(this), dmg)) {
                this.swing(InteractionHand.MAIN_HAND);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if ("arrow".equals(source.getMsgId())) {
            amount *= 0.25F;
        }

        return super.hurt(source, amount);
    }

    @Override
    public boolean hurt(BodyPart part, DamageSource source, float damage) {
        return this.hurt(source, damage * 0.85F);
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        for (BodyPart bodyPart : this.getParts()) {
            this.level.broadcastEntityEvent(bodyPart, (byte)3);
            bodyPart.remove(reason);
        }
    }

    public abstract boolean checkSpawnObstruction(Level level);
}