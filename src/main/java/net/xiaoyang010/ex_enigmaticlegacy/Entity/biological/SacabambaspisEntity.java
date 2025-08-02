package net.xiaoyang010.ex_enigmaticlegacy.Entity.biological;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.ai.EatFishFoodAIFish;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.ai.EntityMateAIFishBase;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;

public class SacabambaspisEntity extends EntityFishBase {

    private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(SacabambaspisEntity.class, EntityDataSerializers.BOOLEAN);

    public SacabambaspisEntity(EntityType<? extends EntityFishBase> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean dropsEggs() {
        return true;
    }

    @Override
    public String getTexture() {
        return "ex_enigmaticlegacy:textures/entity/sacabambaspis.png";
    }

    @Override
    protected float getAISpeedFish() {
        return 0.25F;
    }

    @Override
    protected boolean isBase() {
        return true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return WaterAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.06D);
    }

    @Override
    public boolean isInWaterOrBubble() {
        return this.isInWater() ||
                this.level.getBlockState(this.blockPosition()).is(Blocks.KELP) ||
                this.level.getBlockState(this.blockPosition()).is(Blocks.KELP_PLANT);
    }

    private boolean isEffectiveInWater() {
        BlockPos pos = this.blockPosition();
        return this.level.getFluidState(pos).isSource() ||
                this.level.getBlockState(pos).is(Blocks.KELP) ||
                this.level.getBlockState(pos).is(Blocks.KELP_PLANT) ||
                this.isInWater();
    }

    @Override
    public void tick() {
        super.tick();

        BlockState state = this.level.getBlockState(this.blockPosition());
        if (state.is(Blocks.KELP) || state.is(Blocks.KELP_PLANT)) {
            this.setAirSupply(this.getMaxAirSupply());
        }
    }

    @Override
    public boolean canBreatheUnderwater() {
        return super.canBreatheUnderwater();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FROM_BUCKET, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("FromBucket", this.fromBucket());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setFromBucket(compound.getBoolean("FromBucket"));
    }

    @Override
    protected void registerGoals() {
        // 最高优先级：繁殖和恐慌
        this.goalSelector.addGoal(0, new EntityMateAIFishBase(this, 0.08D));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25D));

        // 次高优先级：逃离威胁
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.6D, 1.4D));

        // 中等优先级：觅食
        this.goalSelector.addGoal(3, new EatFishFoodAIFish(this));

        // 较低优先级：日常活动
        this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, 1.0D, 40));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    public MobType getMobType() {
        return MobType.WATER;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.fromBucket() && !this.hasCustomName();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }

    @Override
    public float getWaterSlowDown() {
        return 0.8F;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {

        if (source == DamageSource.DROWN && isEffectiveInWater()) {
            return false;
        }

        if (isEffectiveInWater()) {
            return super.hurt(source, amount * 0.5F);
        }

        return super.hurt(source, amount);
    }

    public boolean fromBucket() {
        return this.entityData.get(FROM_BUCKET);
    }

    public void setFromBucket(boolean fromBucket) {
        this.entityData.set(FROM_BUCKET, fromBucket);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (itemstack.is(Items.WATER_BUCKET) && this.isAlive()) {
            this.playSound(SoundEvents.BUCKET_FILL_FISH, 1.0F, 1.0F);
            itemstack.shrink(1);
            ItemStack bucketStack = new ItemStack(ModItems.SACABAMBASPIS_BUCKET.get());
            this.saveDefaultDataToBucketTag(bucketStack);

            if (!this.level.isClientSide) {
                this.gameEvent(GameEvent.FLUID_PICKUP);
            }

            if (itemstack.isEmpty()) {
                player.setItemInHand(hand, bucketStack);
            } else if (!player.getInventory().add(bucketStack)) {
                player.drop(bucketStack, false);
            }

            this.discard();
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    private void saveDefaultDataToBucketTag(ItemStack bucket) {
        if (this.hasCustomName()) {
            bucket.setHoverName(this.getCustomName());
        }
        CompoundTag tag = bucket.getOrCreateTag();
        tag.putFloat("Health", this.getHealth());
    }
}