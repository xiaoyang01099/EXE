package net.xiaoyang010.ex_enigmaticlegacy.Entity.creature;

import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

public abstract class AquaticCreature extends WaterAnimal {
    public int randNumTick;
    protected float currentYaw;
    protected float currentPitch;
    protected float prevCurrentYaw;
    protected float prevCurrentPitch;
    protected double netSpeed;
    protected double dPosX;
    protected double dPosY;
    protected double dPosZ;
    protected Vec3 targetVec;

    public AquaticCreature(EntityType<? extends AquaticCreature> type, Level level) {
        super(type, level);
        this.randNumTick = this.random.nextInt(100);
    }

    @Override
    public boolean isInWaterRainOrBubble() {
        // 改进水体检测逻辑
        AABB box = this.getBoundingBox();
        return this.level.containsAnyLiquid(box) ||
                this.level.containsAnyLiquid(box.deflate(0.0D, 0.3D, 0.0D));
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    public float getCurrentPitch(float partialTicks) {
        return this.prevCurrentPitch + (this.currentPitch - this.prevCurrentPitch) * partialTicks;
    }

    public boolean onLand() {
        return !this.isInWater() && this.isOnGround();
    }

    public boolean getTamed() {
        return false;
    }

    public boolean getRotatePitch() {
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.yRot = this.yBodyRot = this.yHeadRot = Mth.wrapDegrees(this.yHeadRot);
        if (this.dPosX * this.dPosX + this.dPosY * this.dPosY + this.dPosZ * this.dPosZ < 1.6000001778593287E-5D) {
            this.setXRot(0.0F);
        }

        this.currentYaw = ADGlobal.wrapAngleAround(this.currentYaw, this.getYRot());
        this.currentPitch = ADGlobal.wrapAngleAround(this.currentPitch, this.getXRot());
        this.prevCurrentYaw = this.currentYaw;
        this.prevCurrentPitch = this.currentPitch;
        this.prevCurrentYaw = ADGlobal.wrapAngleAround(this.prevCurrentYaw, this.currentYaw);
        this.prevCurrentPitch = ADGlobal.wrapAngleAround(this.prevCurrentPitch, this.currentPitch);
        this.currentYaw += (this.getYRot() - this.currentYaw) * 0.6F;
    }

    @Override
    protected void customServerAiStep() {
        this.moveCreature();
    }

    /** Server side only */
    protected abstract void moveCreature();

    public boolean findNewPath() {
        return this.random.nextInt(70) == 0 || this.onLand() && this.random.nextInt(10) == 0;
    }

    protected double addPathY() {
        return this.getEyeHeight();
    }

    public boolean setRandomPath() {
        double x = this.getX() + (3.0D + this.random.nextFloat() * 3.0D) * (this.random.nextBoolean() ? 1 : -1);
        double y = this.getY() + (this.random.nextFloat() - 0.5D) * 6.0D + this.addPathY();
        double z = this.getZ() + (3.0D + this.random.nextFloat() * 3.0D) * (this.random.nextBoolean() ? 1 : -1);
        if (this.onLand()) {
            x = this.getX() + (2.0D + this.random.nextFloat() * 8.0D) * (this.random.nextBoolean() ? 1 : -1);
            z = this.getZ() + (2.0D + this.random.nextFloat() * 8.0D) * (this.random.nextBoolean() ? 1 : -1);
        }

        if (this.isClearPath(x, y, z)) {
            this.targetVec = new Vec3(x, y, z);
            return true;
        } else {
            return false;
        }
    }

    public boolean isClearPath(double x, double y, double z) {
        BlockPos blockPos = new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        boolean water = this.level.getBlockState(blockPos).getMaterial() == this.getPathingMaterial();
        ClipContext context = new ClipContext(this.getEyePosition(1.0F), new Vec3(x, y, z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this);
        BlockHitResult hitResult = this.level.clip(context);
        return water && hitResult.getType() == HitResult.Type.MISS;
    }

    public boolean isClearPathWaterBelow(double x, double y, double z) {
        BlockPos blockPos = new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        BlockPos blockPosBelow = blockPos.below();
        boolean water = this.level.getBlockState(blockPos).getMaterial() == this.getPathingMaterial();
        boolean waterBelow = this.level.getBlockState(blockPosBelow).getMaterial() == this.getPathingMaterial();
        ClipContext context = new ClipContext(this.getEyePosition(1.0F), new Vec3(x, y, z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this);
        BlockHitResult hitResult = this.level.clip(context);
        return (water || waterBelow) && hitResult.getType() == HitResult.Type.MISS;
    }

    public Material getPathingMaterial() {
        return Material.WATER;
    }

    @Override
    public void travel(Vec3 travelVector) {
        double posY;
        if (this.isInWater()) {
            posY = this.getY();
            this.moveRelative(0.02F, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.84D, 0.84D, 0.84D));

            if (this.horizontalCollision && this.isUnderWater()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.30000001192092896D, 0));
            }
        } else if (this.isInLava()) {
            posY = this.getY();
            this.moveRelative(0.02F, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.5D, 0.5D, 0.5D));

            if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, -0.02D, 0));
            }

            if (this.horizontalCollision && this.isUnderWater()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.30000001192092896D, 0));
            }
        } else {
            float friction = 0.91F;
            if (this.isOnGround()) {
                BlockPos pos = new BlockPos(this.getX(), this.getBoundingBox().minY - 1.0D, this.getZ());
                friction = this.level.getBlockState(pos).getFriction(this.level, pos, this) * 0.91F;
            }

            float f3 = 0.16277136F / (friction * friction * friction);
            friction = 0.91F;

            if (this.isOnGround()) {
                float movement = this.getSpeed() * f3;
                this.moveRelative(movement, travelVector);
            } else {
                this.moveRelative(0.02F, travelVector);
            }

            this.move(MoverType.SELF, this.getDeltaMovement());

            if (this.horizontalCollision && this.onClimbable()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.2D, 0));
            }

            if (this.hasEffect(MobEffects.LEVITATION)) {
                this.setDeltaMovement(this.getDeltaMovement().add(
                        0,
                        (0.05D * (double)(this.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - this.getDeltaMovement().y) * 0.2D,
                        0
                ));
            } else if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, -0.08D, 0));
            }

            this.setDeltaMovement(this.getDeltaMovement().multiply(friction, 0.9800000190734863D, friction));
        }

        this.handleLimbSwing();
    }

    protected void handleLimbSwing() {
        this.animationSpeedOld = this.animationSpeed;
        double x = this.getX() - this.xo;
        double y = this.getY() - this.yo;
        double z = this.getZ() - this.zo;
        if (!this.level.isClientSide) {
            x = this.dPosX;
            y = this.dPosY;
            z = this.dPosZ;
        }

        float f6 = Mth.sqrt((float)(x * x + y * y + z * z)) * 4.0F;
        if (f6 > 1.0F) {
            f6 = 1.0F;
        }

        if (!this.isInWater() && !this.isOnGround()) {
            this.animationSpeed *= 0.4F;
        } else {
            float delta = f6 - this.animationSpeed;
            if (delta >= 0.0F) {
                this.animationSpeed += delta * 0.4F;
            } else {
                this.animationSpeed += delta * 0.1F;
            }
        }

        if (this.animationSpeed < 0.0F) {
            this.animationSpeed = 0.0F;
        }
    }

    @Override
    public void knockback(double strength, double x, double z) {
        Vec3 oldMotion = this.getDeltaMovement();
        super.knockback(strength, x, z);
        if (this.isInWater()) {
            Vec3 newMotion = this.getDeltaMovement();
            this.setDeltaMovement(
                    oldMotion.x + (newMotion.x - oldMotion.x) * 0.5D,
                    oldMotion.y + (newMotion.y - oldMotion.y) * 0.800000011920929D,
                    oldMotion.z + (newMotion.z - oldMotion.z) * 0.5D
            );
        } else {
            Vec3 newMotion = this.getDeltaMovement();
            this.setDeltaMovement(
                    newMotion.x,
                    oldMotion.y + (newMotion.y - oldMotion.y) * 0.4000000059604645D,
                    newMotion.z
            );
        }
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        // 覆盖以防止蜘蛛网效果
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(this.randNumTick);
    }

    public void readSpawnData(FriendlyByteBuf buffer) {
        this.randNumTick = buffer.readInt();
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return new ResourceLocation(ExEnigmaticlegacyMod.MODID, "entities/" + this.getType().getRegistryName().getPath());
    }
}