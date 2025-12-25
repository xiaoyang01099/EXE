package net.xiaoyang010.ex_enigmaticlegacy.Entity.biological;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.ClipContext;

import javax.annotation.Nullable;

public abstract class EntityFishBase extends TamableAnimal {

    private static final EntityDataAccessor<Integer> TICKS = SynchedEntityData.defineId(EntityFishBase.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MATEABLE = SynchedEntityData.defineId(EntityFishBase.class, EntityDataSerializers.INT);
    private int inPFLove;
    public BlockPos currentTarget;

    protected EntityFishBase(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.moveControl = this.isBase() ? new SwimmingMoveHelperBase() : new SwimmingMoveHelper();
        this.navigation = new WaterBoundPathNavigation(this, level);
    }

    public abstract boolean dropsEggs();
    public abstract String getTexture();
    protected abstract float getAISpeedFish();
    protected abstract boolean isBase();

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(3, new RandomSwimmingGoal(this, 1.0D, 40));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TICKS, random.nextInt(24000));
        this.entityData.define(MATEABLE, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Ticks", this.getTicks());
        compound.putInt("InPFLove", this.inPFLove);
        compound.putInt("mateable", this.getMateable());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setTicks(compound.getInt("Ticks"));
        this.inPFLove = compound.getInt("InPFLove");
        this.setMateable(compound.getInt("mateable"));
    }

    public ItemStack getPropagule() {
        ItemStack stack = new ItemStack(Items.EGG);
        CompoundTag nbt = new CompoundTag();
        nbt.putString("creature", Registry.ENTITY_TYPE.getKey(this.getType()).toString());
        stack.setTag(nbt);
        return stack;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.TROPICAL_FISH);
    }

    public boolean canMateWith(EntityFishBase otherAnimal) {
        if (otherAnimal == this) return false;
        if (otherAnimal.getClass() != this.getClass()) return false;
        return this.isInLove() && otherAnimal.isInLove();
    }

    public void setNotMateable() {
        this.setMateable(-6000);
    }

    public boolean isInLove() {
        return this.inPFLove > 0;
    }

    public void resetLove() {
        this.inPFLove = 0;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!itemstack.isEmpty() && this.isFood(itemstack) && this.inPFLove <= 0 && this.getMateable() == 0) {
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
            this.inPFLove = 600;
            this.level.broadcastEntityEvent(this, (byte)18);
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(0.1F, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            Vec3 motion = this.getDeltaMovement();
            motion = motion.scale(0.9D);

            if (this.horizontalCollision && this.isCollidingRim()) {
                motion = motion.add(0, 0.05D, 0);
            }

            this.setDeltaMovement(motion);
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.inPFLove > 0) {
            --this.inPFLove;
        }

        if (this.getMateable() < 0) {
            this.setMateable(this.getMateable() + 1);
        }

        if (this.isAlive() && !isInWater()) {
            int air = this.getAirSupply();
            this.setAirSupply(air - 1);
            if (this.getAirSupply() == -20) {
                this.setAirSupply(0);
                this.hurt(DamageSource.DROWN, 2.0F);
            }
        } else {
            this.setAirSupply(300);
        }

        if (!level.isClientSide && this.getCanBreed() && this.dropsEggs()) {
            if (random.nextDouble() > 0.5) {
                ItemStack egg = this.getPropagule();
                ItemEntity eggEntity = new ItemEntity(level, this.getX(), this.getY(), this.getZ(), egg);
                eggEntity.setPickUpDelay(10);
                this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                level.addFreshEntity(eggEntity);

                for (int i = 0; i < 7; ++i) {
                    double d0 = random.nextGaussian() * 0.02D;
                    double d1 = random.nextGaussian() * 0.02D;
                    double d2 = random.nextGaussian() * 0.02D;
                    level.addParticle(
                            ParticleTypes.HEART,
                            this.getRandomX(1.0D),
                            this.getRandomY() + 0.5D,
                            this.getRandomZ(1.0D),
                            d0, d1, d2
                    );
                }
            }
            this.setTicks(0);
        }
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader level) {
        return level.getBlockState(this.blockPosition()).getMaterial() == Material.WATER;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        this.setAirSupply(300);
        this.setTicks(0);
        this.setMateable(0);
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    public int getTicks() {
        return this.entityData.get(TICKS);
    }

    public void setTicks(int ticks) {
        this.entityData.set(TICKS, ticks);
    }

    public int getMateable() {
        return this.entityData.get(MATEABLE);
    }

    public void setMateable(int ticks) {
        this.entityData.set(MATEABLE, ticks);
    }

    public boolean getCanBreed() {
        return this.getTicks() > 24000;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    public boolean isInWater() {
        return level.getBlockState(blockPosition()).getMaterial() == Material.WATER
                || level.getBlockState(blockPosition().above()).getMaterial() == Material.WATER;
    }

    public boolean isAtBottom() {
        if (this.getY() - 1 > 1) {
            BlockPos pos = new BlockPos(this.getX(), this.getY() - 1, this.getZ());
            return (isInWater() &&
                    ((level.getBlockState(pos)).getMaterial() != Material.WATER) &&
                    ((double)pos.getY() + 0.334D) > this.getY());
        }
        return true;
    }

    public boolean isCollidingRim() {
        if (this.isInWater()) {
            Vec3 start = this.getEyePosition();
            Vec3 viewVector = this.getViewVector(0);
            Vec3 end = start.add(viewVector.x, viewVector.y, viewVector.z);

            ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this);
            HitResult hitResult = level.clip(context);

            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos hitPos = new BlockPos(hitResult.getLocation());
                return level.getBlockState(hitPos).getMaterial() == Material.WATER;
            }
        }
        return false;
    }


    public void eatItem(ItemStack stack) {
        if (!stack.isEmpty() && stack.is(Items.KELP)) {
            this.heal(2.0F);
            this.playSound(SoundEvents.GENERIC_EAT, 0.5F + 0.5F * (float)this.random.nextInt(2),
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);

            for(int i = 0; i < 6; ++i) {
                Vec3 vec3 = new Vec3(((double)this.random.nextFloat() - 0.5D) * 0.1D,
                        Math.random() * 0.1D + 0.1D,
                        ((double)this.random.nextFloat() - 0.5D) * 0.1D);
                this.level.addParticle(ParticleTypes.ITEM_SLIME,
                        this.getX() + this.getLookAngle().x / 2.0D,
                        this.getY(),
                        this.getZ() + this.getLookAngle().z / 2.0D,
                        vec3.x, vec3.y, vec3.z);
            }
        }
    }

    class SwimmingMoveHelperBase extends MoveControl {
        private final EntityFishBase fish = EntityFishBase.this;

        public SwimmingMoveHelperBase() {
            super(EntityFishBase.this);
        }

        public void tick() {
            if (this.operation == Operation.MOVE_TO && !this.fish.getNavigation().isDone()) {
                Vec3 vec3 = new Vec3(this.wantedX - fish.getX(), this.wantedY - fish.getY(), this.wantedZ - fish.getZ());
                double d0 = vec3.length();
                double d1 = vec3.y / d0;
                float f = (float)(Mth.atan2(vec3.z, vec3.x) * (double)(180F / (float)Math.PI)) - 90F;

                fish.setYRot(this.rotlerp(fish.getYRot(), f, 10.0F));
                fish.yBodyRot = fish.getYRot();
                fish.yHeadRot = fish.getYRot();

                float speed = getAISpeedFish();
                if (fish.isAtBottom()) {
                    speed *= 0.25F;
                }

                fish.setSpeed(speed);
                fish.setDeltaMovement(fish.getDeltaMovement().add(0, fish.getSpeed() * d1 * 0.1D, 0));
            } else {
                fish.setSpeed(0);
            }
        }
    }

    class SwimmingMoveHelper extends MoveControl {
        private final EntityFishBase fish = EntityFishBase.this;

        public SwimmingMoveHelper() {
            super(EntityFishBase.this);
        }

        public void tick() {
            if (this.operation == Operation.MOVE_TO && !this.fish.getNavigation().isDone()) {
            Vec3 vec3 = new Vec3(this.wantedX - fish.getX(), this.wantedY - fish.getY(), this.wantedZ - fish.getZ());
            double d0 = vec3.length();
            double d1 = vec3.y / d0;
            float f = (float)(Mth.atan2(vec3.z, vec3.x) * (double)(180F / (float)Math.PI)) - 90F;

            fish.setYRot(this.rotlerp(fish.getYRot(), f, 20.0F));
            fish.yBodyRot = fish.getYRot();
            fish.yHeadRot = fish.getYRot();

            float speed = getAISpeedFish();
            fish.setSpeed(speed);
            fish.setDeltaMovement(fish.getDeltaMovement().add(0, fish.getSpeed() * d1 * 0.1D, 0));
        } else {
            fish.setSpeed(0);
        }
        }
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null; // Override in specific entity classes
    }

    // XP and loot
    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        if (level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            this.level.addFreshEntity(new ExperienceOrb(
                    this.level,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    this.getRandom().nextInt(7) + 1
            ));
        }

        // Reset breeding timers
        this.resetLove();
        if (mate instanceof EntityFishBase) {
            ((EntityFishBase) mate).resetLove();
        }
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return Math.max(dimensions.height * 0.85F, 0.2F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source == DamageSource.IN_WALL) {
            return false;
        }
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        this.inPFLove = 0;
        return super.hurt(source, amount);
    }

    @Override
    protected int calculateFallDamage(float fallDistance, float damageMultiplier) {
        return 0;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
        // Fish don't take fall damage
    }

    @Override
    public int getMaxAirSupply() {
        return 300;
    }

    @Override
    public boolean shouldDropExperience() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.hasCustomName();
    }
}