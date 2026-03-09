package net.xiaoyang010.ex_enigmaticlegacy.Entity.biological;

import java.util.function.Predicate;


import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.ai.EntityAIFindEntityNearest;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.ai.EntityAIHurtByTarget;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.creature.BodyPart;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.creature.Bone;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.creature.ComplexCreature;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.creature.Euler;

public class SeaSerpent extends ComplexCreature implements Enemy {
    private static final Predicate<LivingEntity> CAN_TARGET = target -> {
        if (target.isInvertedHealAndHarm() || target instanceof Enemy || target instanceof AbstractGolem) {
            return false;
        }

        return target.isPassenger() && target.getVehicle().isInWater() || target.isInWater() || !target.isOnGround();
    };

    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(SeaSerpent.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> HUNGER = SynchedEntityData.defineId(SeaSerpent.class, EntityDataSerializers.BYTE);
    public static final int[] PART_HEIGHT = new int[] {8, 14, 12, 10, 8, 6, 5, 4};
    public static final int[] PART_LENGTH = new int[] {18, 24, 24, 20, 14, 16, 18, 22};
    private final BodyPart[] partList = new BodyPart[8];
    private final Bone baseBone = new Bone();
    private final Bone[] boneList = new Bone[8];
    private final Euler[] targetAngles = new Euler[8];
    private int strikeTick;
    private boolean strikeStopped;
    private Vec3 oldVec;

    public SeaSerpent(EntityType<? extends SeaSerpent> type, Level level) {
        super(type, level);
        this.baseBone.setLength(0.0F);
        this.boneList[0] = new Bone(this.baseBone);
        this.boneList[0].setLength((float)PART_LENGTH[0] / 16.0F);

        for (int i = 1; i < this.boneList.length; ++i) {
            this.boneList[i] = new Bone(i == 1 ? this.baseBone : this.boneList[i - 1]);
            this.boneList[i].setLength((float)(-PART_LENGTH[i]) / 16.0F);
        }

        for (int i = 0; i < this.partList.length; ++i) {
            this.targetAngles[i] = new Euler();
            this.partList[i] = new BodyPart(this, (float)PART_LENGTH[i] / 16.0F);
        }

        this.updateParts();
        this.refreshDimensions();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 90.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.06D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    public boolean isClearPath(double x, double y, double z) {
        Vec3 start = this.position();
        Vec3 end = new Vec3(x, y, z);
        Vec3 movement = end.subtract(start).normalize();

        HitResult hitResult = this.level.clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.SOURCE_ONLY,
                this
        ));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return false;
        }

        BlockPos targetPos = new BlockPos(x, y, z);
        return this.level.getFluidState(targetPos).is(FluidTags.WATER);
    }

    @Override
    public boolean findNewPath() {
        if (!this.isInWater()) {
            return this.random.nextInt(20) == 0;
        }
        return this.random.nextInt(70) == 0;
    }

    @Override
    public boolean setRandomPath() {
        if (this.isInWater()) {
            double x = this.getX() + (10.0D + this.random.nextFloat() * 12.0D) * (this.random.nextBoolean() ? 1 : -1);
            double y = this.getY() + ((this.random.nextFloat() - 0.5D) * 12.0D);
            double z = this.getZ() + (10.0D + this.random.nextFloat() * 12.0D) * (this.random.nextBoolean() ? 1 : -1);

            if (this.isClearPath(x, y, z)) {
                this.targetVec = new Vec3(x, y, z);
                return true;
            }
        } else {
            BlockPos waterPos = findNearestWater(16);
            if (waterPos != null) {
                this.targetVec = new Vec3(waterPos.getX(), waterPos.getY(), waterPos.getZ());
                return true;
            }
        }
        return false;
    }

    private BlockPos findNearestWater(int range) {
        BlockPos pos = this.blockPosition();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int y = -range; y <= range; y++) {
            for (int x = -range; x <= range; x++) {
                for (int z = -range; z <= range; z++) {
                    mutablePos.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    if (this.level.getFluidState(mutablePos).is(FluidTags.WATER)) {
                        return mutablePos.immutable();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.isInWater()) {
            if (this.onGround) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.8D, 1.0D, 0.8D));
            } else {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.08D, 0.0D));
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
        this.entityData.define(HUNGER, (byte)this.random.nextInt(16));
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(0, new EntityAIHurtByTarget(this));
        this.targetSelector.addGoal(1, new EntityAIFindEntityNearest<>(this, Player.class, CAN_TARGET));
        this.targetSelector.addGoal(2, new AIHuntPrey(this));
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    private void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    public int getHunger() {
        return this.entityData.get(HUNGER);
    }

    private void setHunger(int hunger) {
        this.entityData.set(HUNGER, (byte)hunger);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 2;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(1.1F, 1.1F);
    }

    @Override
    public int getExperienceValue() {
        return 25;
    }

    @Override
    public int getMaxAirSupply() {
        return 300;
    }

    @Override
    public BodyPart[] getParts() {
        return this.partList;
    }

    @Override
    public Bone getBaseBone() {
        return this.baseBone;
    }

    @Override
    public Bone[] getBoneList() {
        return this.boneList;
    }

    @Override
    public void updatePitchRotations(float partialTick) {
        for (int i = this.boneList.length - 1; i > 1; --i) {
            if (partialTick == 1.0F) {
                this.boneList[i].getRotation().x += this.targetAngles[i].x = this.targetAngles[i - 1].x;
            } else {
                this.boneList[i].getRotation().x += this.targetAngles[i].x + (this.targetAngles[i - 1].x - this.targetAngles[i].x) * partialTick;
            }
        }

        this.targetAngles[1].x = -(this.currentPitch - this.prevCurrentPitch) * 2.4F;
        float moveScale = this.animationSpeedOld + (this.animationSpeed - this.animationSpeedOld) * partialTick;
        float moveTick = this.animationPosition - this.animationSpeed * (1.0F - partialTick);
        if (moveScale > 1.0F) {
            moveScale = 1.0F;
        }

        for (int i = 0; i < this.boneList.length; ++i) {
            float breatheAnim = Mth.sin(0.1F * ((float)this.tickCount + partialTick - (float)i * 6.0F));
            float moveAnim = Mth.sin(0.2F * (moveTick - (float)i * 2.0F)) * moveScale;
            this.boneList[i].getRotation().x += breatheAnim * 1.1F;
            this.boneList[i].getRotation().x += moveAnim * 6.0F;
            if (i == 0 && partialTick == 1.0F) {
                Euler angle = new Euler(this.currentPitch + 90.0F, this.getYRot(), 0.0F);
                Vec3 vec = angle.rotateVector(moveAnim * 0.03F);
                this.setDeltaMovement(this.getDeltaMovement().add(vec.x, vec.y, vec.z));
            }
        }
    }

    @Override
    public void updateYawRotations(float partialTick) {
        for (int i = this.boneList.length - 1; i > 1; --i) {
            if (partialTick == 1.0F) {
                this.boneList[i].getRotation().y += this.targetAngles[i].y = this.targetAngles[i - 1].y;
            } else {
                this.boneList[i].getRotation().y += this.targetAngles[i].y + (this.targetAngles[i - 1].y - this.targetAngles[i].y) * partialTick;
            }
        }

        this.targetAngles[1].y = -(this.currentYaw - this.prevCurrentYaw) * 2.5F;
        this.targetAngles[0].y = (this.currentYaw - this.prevCurrentYaw) * 1.5F;
    }

    private double getStrikeRange() {
        return 6.0D;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            if (this.getHunger() <= 0) {
                if (this.tickCount % 40 == 0) {
                    this.heal(1.0F);
                }

                if (this.random.nextInt(3600) == 0) {
                    this.setHunger(14 + this.random.nextInt(8));
                }
            }

            this.strikeTick = Math.max(0, this.strikeTick - this.hurtTime - 1);
        }
    }

    @Override
    public double moveByTarget(LivingEntity target) {
        double targetSpeed = this.getMovementSpeed() * 1.06D;
        double dSq = this.distanceToSqr(target);
        double range = 32.0D;
        double strikeRange = this.getStrikeRange();
        if (this.isAttacking()) {
            if (dSq < strikeRange * strikeRange) {
                if (this.targetVec == null) {
                    this.targetVec = new Vec3(target.getX() - this.getX(), target.getY() - this.getY(), target.getZ() - this.getZ());
                    this.targetVec = this.targetVec.normalize();
                }

                if (this.oldVec == null) {
                    this.oldVec = this.position();
                }

                if (this.isInWater()) {
                    this.getLookControl().setLookAt(target, 15.0F, 85.0F);
                    float scale = 9.0F;
                    targetSpeed *= 0.30000001192092896D;
                    this.setDeltaMovement(
                            this.getDeltaMovement().x + (this.targetVec.x * targetSpeed * (double)scale - this.getDeltaMovement().x) * 0.6D,
                            this.getDeltaMovement().y + (this.targetVec.y * targetSpeed * (double)scale - this.getDeltaMovement().y) * 0.6D,
                            this.getDeltaMovement().z + (this.targetVec.z * targetSpeed * (double)scale - this.getDeltaMovement().z) * 0.6D
                    );
                }
            } else if (!this.strikeStopped) {
                this.getLookControl().setLookAt(target, 8.0F, 85.0F);
                this.targetVec = new Vec3(target.getX() - this.getX(), target.getY() - this.getY(), target.getZ() - this.getZ());
                this.targetVec = this.targetVec.normalize();
                this.oldVec = this.position();
            }

            double dSq1 = this.distanceToSqr(this.oldVec.x, this.oldVec.y, this.oldVec.z);
            if (dSq1 > strikeRange * strikeRange || !this.isInWater() && !this.isOnGround() || this.strikeStopped) {
                this.setAttacking(false);
                this.strikeStopped = false;
                this.strikeTick = 60 + this.random.nextInt(20);
                this.targetVec = null;
                this.oldVec = null;
            }
        } else if (this.targetVec != null) {
            this.getLookControl().setLookAt(this.targetVec.x, this.targetVec.y, this.targetVec.z, 3.0F, 85.0F);
            if (this.horizontalCollision && this.getDeltaMovement().y < 0.20000000298023224D) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.20000000298023224D, this.getDeltaMovement().z);
            }

            if (dSq > range * range) {
                this.moveTick = 0;
                this.targetVec = null;
            } else if (this.strikeTick <= 0 && dSq > strikeRange * strikeRange && this.random.nextInt(70) == 0) {
                this.setAttacking(true);
                this.moveTick = 0;
                this.targetVec = null;
            }
        } else if (dSq > range * range) {
            this.getLookControl().setLookAt(target, 10.0F, 85.0F);
        } else {
            targetSpeed = 0.0D;
            this.setRandomPathAround(target);
        }

        if (this.targetVec != null && this.targetVec.distanceTo(this.position()) < 3.0D || this.moveTick > 120) {
            this.moveTick = 0;
            this.setRandomPathAround(target);
        }

        return targetSpeed;
    }

//    @Override
//    public boolean findNewPath() {
//        return this.random.nextInt(40) == 0 || this.onLand() && this.random.nextInt(4) == 0;
//    }
//
//    @Override
//    public boolean setRandomPath() {
//        double x = this.getX() + (10.0D + (double)this.random.nextFloat() * 12.0D) * (double)(this.random.nextBoolean() ? 1 : -1);
//        double y = this.getY() + ((double)this.random.nextFloat() - 0.5D) * 12.0D;
//        double z = this.getZ() + (10.0D + (double)this.random.nextFloat() * 12.0D) * (double)(this.random.nextBoolean() ? 1 : -1);
//        if (this.onLand()) {
//            x = this.getX() + (4.0D + (double)this.random.nextFloat() * 16.0D) * (double)(this.random.nextBoolean() ? 1 : -1);
//            z = this.getZ() + (4.0D + (double)this.random.nextFloat() * 16.0D) * (double)(this.random.nextBoolean() ? 1 : -1);
//        }
//
//        if (this.isClearPath(x, y, z)) {
//            this.targetVec = new Vec3(x, y, z);
//            return true;
//        } else {
//            return false;
//        }
//    }

    private boolean setRandomPathAround(LivingEntity living) {
        double x = living.getX() + (4.0D + (double)this.random.nextFloat() * 5.0D) * (double)(this.random.nextBoolean() ? 1 : -1);
        double y = living.getBoundingBox().minY - 5.0D + ((double)this.random.nextFloat() - 0.5D) * 20.0D;
        double z = living.getZ() + (4.0D + (double)this.random.nextFloat() * 5.0D) * (double)(this.random.nextBoolean() ? 1 : -1);
        if (this.onLand()) {
            x = this.getX() + (4.0D + (double)this.random.nextFloat() * 16.0D) * (double)(this.random.nextBoolean() ? 1 : -1);
            z = this.getZ() + (4.0D + (double)this.random.nextFloat() * 16.0D) * (double)(this.random.nextBoolean() ? 1 : -1);
        }

        if (this.isClearPathWaterBelow(x, y, z)) {
            this.targetVec = new Vec3(x, y, z);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void collideWithEntity(BodyPart part, Entity entity) {
        LivingEntity target = this.getTarget();
        float dmg = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        BodyPart[] partList = this.getParts();

        if (partList != null && part == partList[partList.length - 1] && this.dPosX * this.dPosX + this.dPosY * this.dPosY + this.dPosZ * this.dPosZ > 0.04000000000000001D) {
            entity.hurt(DamageSource.mobAttack(this), dmg * 0.6F);
            double x = entity.getX() - this.getX();
            double y = entity.getY() - this.getY();
            double z = entity.getZ() - this.getZ();
            double d0 = Math.sqrt(x * x + y * y + z * z);
            float scale = 0.12F * dmg;
            entity.setDeltaMovement(
                    x / d0 * (double)scale,
                    y / d0 * (double)scale,
                    z / d0 * (double)scale
            );
            entity.hasImpulse = true;
        }

        if (this.isAttacking() && target != null && this.distanceToSqr(target) < this.getStrikeRange() * this.getStrikeRange()) {
            if (!this.level.isClientSide && entity == target) {
                this.strikeStopped = true;
            }

            this.eatOrDamageEntity(part, entity, dmg);
            double x = entity.getX() - this.getX();
            double y = entity.getY() - this.getY();
            double z = entity.getZ() - this.getZ();
            double d0 = Math.sqrt(x * x + y * y + z * z);
            Vec3 vec = new Vec3(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
            vec = vec.normalize();
            float scale = 0.8F;
            entity.setDeltaMovement(
                    (x / d0 + vec.x) * (double)scale,
                    (y / d0 + vec.y) * (double)scale,
                    (z / d0 + vec.z) * (double)scale
            );
            entity.hasImpulse = true;
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            // 水中移动逻辑
            this.moveRelative(0.01F, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));

            if (this.getTarget() == null) {
                this.setDeltaMovement(
                        this.getDeltaMovement().add(0.0D, -0.005D, 0.0D)
                );
            }
        } else {
            // 陆地移动逻辑
            super.travel(travelVector);
        }
    }

    @Override
    protected void handleAirSupply(int airSupply) {
        if (!this.isInWater() && !this.hasEffect(MobEffects.WATER_BREATHING)) {
            this.setAirSupply(airSupply - 1);
            if (this.getAirSupply() == -20) {
                this.setAirSupply(0);
                this.hurt(DamageSource.DROWN, 2.0F);
            }
        } else {
            this.setAirSupply(this.getMaxAirSupply());
        }
    }

    @Override
    public void killed(ServerLevel level, LivingEntity living) {
        super.killed(level,living);
        int hunger = this.getHunger();
        boolean edible = false;
        if (living instanceof Player || living instanceof Animal || living instanceof Npc) {
            edible = true;
        }

        if (!this.level.isClientSide && hunger > 0 && edible) {
            this.setHunger(hunger - 1);
        }
    }

    @Override
    public void knockback(double strength, double x, double z) {
        if (!this.isInWater()) {
            super.knockback(strength, x, z);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("Hunger", (byte)this.getHunger());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Hunger")) {
            this.setHunger(tag.getByte("Hunger"));
        }
    }

    @Override
    public boolean checkSpawnObstruction(Level level) {
        return this.getY() < 48.0D;
    }

    @Override
    protected float getSoundVolume() {
        return 0.8F;
    }

    static class AIHuntPrey extends EntityAIFindEntityNearest<Mob> {
        public AIHuntPrey(SeaSerpent creature) {
            super(creature, Mob.class, CAN_TARGET);
        }

        @Override
        public boolean canUse() {
            return ((SeaSerpent)this.mob).getHunger() > 0 && super.canUse();
        }
    }
}