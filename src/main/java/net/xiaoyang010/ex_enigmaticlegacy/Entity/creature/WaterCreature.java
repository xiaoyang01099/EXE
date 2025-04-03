package net.xiaoyang010.ex_enigmaticlegacy.Entity.creature;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public abstract class WaterCreature extends AquaticCreature {
    private static final EntityDataAccessor<Float> RED = SynchedEntityData.defineId(WaterCreature.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> GREEN = SynchedEntityData.defineId(WaterCreature.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BLUE = SynchedEntityData.defineId(WaterCreature.class, EntityDataSerializers.FLOAT);
    public School school;
    protected double fleeDistance = 10.0D;
    protected LivingEntity fleeFromEntity;
    protected LivingEntity followEntity;
    protected Vec3 fleeLookVec;

    public WaterCreature(EntityType<? extends WaterCreature> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(RED, 1.0F);
        this.entityData.define(GREEN, 1.0F);
        this.entityData.define(BLUE, 1.0F);
    }

    public float getRed() {
        return this.entityData.get(RED);
    }

    public float getGreen() {
        return this.entityData.get(GREEN);
    }

    public float getBlue() {
        return this.entityData.get(BLUE);
    }

    protected void setColor(float red, float green, float blue) {
        this.entityData.set(RED, red);
        this.entityData.set(GREEN, green);
        this.entityData.set(BLUE, blue);
    }

    public boolean isColorful() {
        return this.getRed() != 1.0F && this.getGreen() != 1.0F && this.getBlue() != 1.0F;
    }

    public void initSchool() {
        this.school = new School(this);
    }

    @Override
    public boolean isInWater() {
        return this.level.containsAnyLiquid(this.getBoundingBox().deflate(0.0D, 0.1D, 0.0D));
    }

    public boolean shouldLeaveSchool() {
        return this.random.nextInt(1800) == 0;
    }

    public boolean canCombineWith(School otherSchool) {
        return true;
    }

    @Override
    public void tick() {
        double prevX = this.getX();
        double prevY = this.getY();
        double prevZ = this.getZ();
        super.tick();
        this.dPosX = this.getX() - prevX;
        this.dPosY = this.getY() - prevY;
        this.dPosZ = this.getZ() - prevZ;

        if (!this.level.isClientSide) {
            if (this.getTamed() && this.tickCount % 100 == 0) {
                this.heal(2.0F);
            }

            if (this.school == null) {
                this.initSchool();
            } else {
                WaterCreature leader = this.school.getLeader();
                if (leader == null || leader.isDeadOrDying()) {
                    this.school.chooseRandomLeader();
                }

                if (leader == this) {
                    this.school.updateSchool();
                }

                if (!this.school.ridinSolo() && this.shouldLeaveSchool()) {
                    this.school.removeCreature(this);
                }
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.isInWater()) {
            if (this.isOnGround()) {
                this.currentPitch *= 0.6F;
            } else {
                double d = Math.sqrt(this.dPosX * this.dPosX + this.dPosZ * this.dPosZ);
                float pitch = -((float)Math.atan2(this.dPosY, d)) * (180F / (float)Math.PI);
                this.currentPitch += (pitch - this.currentPitch) * 0.4F;
            }
        } else {
            this.currentPitch += (this.getXRot() - this.currentPitch) * 0.05F;
        }
    }

    @Override
    protected void moveCreature() {
        double targetSpeed = 0.0D;
        if (this.fleeFromEntity != null && this.getVehicle() == null) {
            if (this.fleeLookVec == null || this.random.nextInt(30) == 0) {
                this.fleeLookVec = new Vec3(this.getX() - this.fleeFromEntity.getX(),
                        2.0D + (2.0D + this.random.nextFloat() * 3.0D) * (this.random.nextBoolean() ? 1 : -1),
                        this.getZ() - this.fleeFromEntity.getZ());

                for (int i = 0; i < 4 && !this.isClearPath(this.getX() + this.fleeLookVec.x,
                        this.getY() + this.fleeLookVec.y, this.getZ() + this.fleeLookVec.z); ++i) {
                    this.fleeLookVec = new Vec3(0.0D,
                            2.0D + (2.0D + this.random.nextFloat() * 3.0D) * (this.random.nextBoolean() ? 1 : -1),
                            0.0D);
                }

                this.fleeLookVec = this.fleeLookVec.normalize();
                this.fleeLookVec = this.fleeLookVec.scale(4.0F);
                this.fleeLookVec = this.fleeLookVec.add(this.random.nextFloat(), 0.0D, this.random.nextFloat());
            }

            this.getLookControl().setLookAt(this.getX() + this.fleeLookVec.x,
                    this.getY() + this.fleeLookVec.y, this.getZ() + this.fleeLookVec.z, 10.0F, 85.0F);

            if (this.horizontalCollision && this.getDeltaMovement().y < 0.20000000298023224D) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.20000000298023224D, this.getDeltaMovement().z);
            }

            targetSpeed = this.getMovementSpeed() * 2.0D;
            if (this.distanceToSqr(this.fleeFromEntity) > this.fleeDistance * this.fleeDistance) {
                this.resetFleeDistance();
                this.fleeFromEntity = null;
                this.fleeLookVec = null;
            }
        } else if (this.followEntity != null) {
            this.getLookControl().setLookAt(this.followEntity, 6.0F, 85.0F);
            if (this.horizontalCollision && this.getDeltaMovement().y < 0.20000000298023224D) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.20000000298023224D, this.getDeltaMovement().z);
            }

            if (this.distanceToSqr(this.followEntity) > 1.0D) {
                targetSpeed = this.getMovementSpeed();
            }
        } else if (this.targetVec != null) {
            this.getLookControl().setLookAt(this.targetVec.x, this.targetVec.y, this.targetVec.z, 10.0F, 85.0F);
            if (this.horizontalCollision && this.getDeltaMovement().y < 0.20000000298023224D) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.20000000298023224D, this.getDeltaMovement().z);
            }

            targetSpeed = this.getMovementSpeed();
            if (!this.getTamed() && this.getVehicle() instanceof Player) {
                targetSpeed *= 2.200000047683716D;
            }

            double closestDist = this.getClosestPathDist();
            if (this.targetVec.distanceToSqr(this.position()) < closestDist * closestDist) {
                this.targetVec = null;
            }
        }

        if (this.noActionTime < 100 && this.findNewPath() && this.tickCount > 20 && this.school.getLeader() == this) {
            this.setRandomPath();
        }

        if (this.getFlees()) {
            this.fleeFromNearbyPlayers();
        }

        Vec3 vec = this.getViewVector(1.0F);
        this.netSpeed += (targetSpeed - this.netSpeed) * 0.1D;
        if (this.onLand()) {
            this.netSpeed = 0.0D;
            if (this.targetVec != null && this.random.nextInt(20) == 0) {
                this.setDeltaMovement(
                        vec.x * 0.17000000178813934D,
                        0.20000000298023224D,
                        vec.z * 0.17000000178813934D
                );
            }
        }

        this.setDeltaMovement(vec.x * this.netSpeed, vec.y * this.netSpeed, vec.z * this.netSpeed);
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    public final double getMovementSpeed() {
        return this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.800000011920929D;
    }

    public void resetFleeDistance() {
        this.fleeDistance = 8.0D + this.random.nextFloat() * 4.0D;
    }

    public float getClosestPathDist() {
        return 1.0F;
    }

    public boolean getFlees() {
        return !this.getTamed();
    }

    @Override
    public boolean hurt(DamageSource source, float dmg) {
        if (!super.hurt(source, dmg)) {
            return false;
        } else {
            if (source.getEntity() instanceof LivingEntity) {
                this.fleeFromEntity = (LivingEntity)source.getEntity();
            }
            return true;
        }
    }

    private void fleeFromNearbyPlayers() {
        Player player = this.level.getNearestPlayer(this, 9.0D);
        if (player != null && this.getSensing().hasLineOfSight(player)) {
            float angle = (float)Math.atan2(player.getZ() - this.getZ(),
                    player.getX() - this.getX()) * (180F / (float)Math.PI) - 90.0F;
            angle = ADGlobal.wrapAngleAround(angle, this.getYRot());
            float delta = Math.abs(angle - this.getYRot());
            if (delta < 90.0F || this.distanceToSqr(player) < 4.0D && delta < 120.0F) {
                this.fleeFromEntity = player;
                this.targetVec = null;
            }
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level.isClientSide && this.school != null) {
            if (!this.school.ridinSolo()) {
                this.school.removeCreature(this);
            }

            if (this.school.getLeader() == this) {
                this.school.chooseRandomLeader();
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ListTag colorList = this.newFloatList(this.getRed(), this.getGreen(), this.getBlue());
        tag.put("Color", colorList);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Color", 9)) {
            ListTag colorList = tag.getList("Color", 5);
            this.setColor(colorList.getFloat(0), colorList.getFloat(1), colorList.getFloat(2));
        }
    }
}