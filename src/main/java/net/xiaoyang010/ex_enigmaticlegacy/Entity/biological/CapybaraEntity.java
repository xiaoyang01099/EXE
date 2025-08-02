package net.xiaoyang010.ex_enigmaticlegacy.Entity.biological;

import com.google.common.base.Suppliers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.event.ForgeEventFactory;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.ai.CapybaraAnimalAttractionGoal;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModSounds;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CapybaraEntity extends TamableAnimal implements MenuProvider {

    private static final Supplier<Set<Item>> TEMPT_ITEMS = Suppliers.memoize(() -> {
        Stream<Item> stream = Stream.of(
                Blocks.MELON.asItem(),
                Items.APPLE,
                Items.SUGAR_CANE,
                Items.MELON_SLICE
        );
        return stream.collect(Collectors.toSet());
    });

    private static final EntityDataAccessor<Integer> CHESTS = SynchedEntityData.defineId(CapybaraEntity.class, EntityDataSerializers.INT);
    public SimpleContainer inventory;

    public CapybaraEntity(EntityType<? extends CapybaraEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.25D, Ingredient.of(TEMPT_ITEMS.get().toArray(new Item[0])), false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25D));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(10, new CapybaraAnimalAttractionGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(CHESTS, 0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    @Override
    public float getWaterSlowDown() {
        return 0.65f;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Item.byBlock(Blocks.MELON));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.random.nextBoolean() ? ModSounds.CAPYBARA_AMBIENT_1 : ModSounds.CAPYBARA_AMBIENT_2;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.CAPYBARA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CAPYBARA_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(SoundEvents.COW_STEP, 0.15F, 1.0F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        Entity entity = source.getEntity();
        this.setOrderedToSit(false);
        if (entity != null && !(entity instanceof Player) && !(entity instanceof AbstractArrow)) {
            amount = (amount + 1.0F) / 2.0F;
        }
        return super.hurt(source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (stack.is(Blocks.CHEST.asItem())) {
                if (inventory == null || inventory.getContainerSize() < 27) {
                    inventory = new SimpleContainer(27);
                    entityData.set(CHESTS, 1);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    return InteractionResult.sidedSuccess(this.level.isClientSide);
                } else if (inventory.getContainerSize() < 54) {
                    SimpleContainer inv = new SimpleContainer(54);
                    for (int i = 0; i < 27; i++) {
                        inv.setItem(i, inventory.getItem(i));
                    }
                    inventory = inv;
                    entityData.set(CHESTS, 2);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    return InteractionResult.sidedSuccess(this.level.isClientSide);
                }
            } else if (!stack.isEmpty()) { //潜行右键 有物品时 切换站坐状态
                this.setOrderedToSit(!this.isOrderedToSit());
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            } else if (inventory != null) {
                player.openMenu(this);
                return InteractionResult.SUCCESS;
            }
        } else if (TEMPT_ITEMS.get().contains(stack.getItem()) && !isTame()) {
            if (this.random.nextInt(3) == 0 && !ForgeEventFactory.onAnimalTame(this, player)) {
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(true);
                this.level.broadcastEntityEvent(this, (byte) 7);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            } else {
                this.level.broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else if (!this.isVehicle() && !player.isSecondaryUseActive() && !this.isBaby() && !isInSittingPose()) {
            if (!this.level.isClientSide) {
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else if (!this.getPassengers().isEmpty()) {
            this.ejectPassengers();
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.CAPYBARA.get().create(level);
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions size) {
        return this.isBaby() ? 0.5F : 0.9F;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.CAPYBARA_SPAWN_EGG.get());
    }

    @Override
    public void tick() {
        super.tick();
        this.floatStrider();
        this.checkInsideBlocks();
        if (getPassengers().isEmpty()) {
            for (Entity e : level.getEntities(this, getBoundingBox().inflate(0.5))) {
                if (e instanceof Mob && e.getBbWidth() <= 0.75f && e.getBbHeight() <= 0.75f && !this.isBaby() && ((Mob) e).getMobType() != MobType.WATER && !isInWater()) {
                    e.startRiding(this);
                }
            }
        } else if (!getPassengers().isEmpty() && isInWater()) {
            ejectPassengers();
        }
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterPathNavigator(this, level);
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        return this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
    }

    @Override
    public boolean canBeControlledByRider() {
        return false;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        if (spawnData == null) {
            spawnData = new AgeableMobGroupData(1.0F);
        }
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    private void floatStrider() {
        if (this.isInWater()) {
            CollisionContext context = CollisionContext.of(this);
            if (context.isAbove(LiquidBlock.STABLE_SHAPE, this.blockPosition(), true) &&
                    !this.level.getFluidState(this.blockPosition().above()).is(FluidTags.WATER)) {
                this.onGround = true;
            } else {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5D).add(0.0D, 0.05D, 0.0D));
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (inventory != null) {
            ListTag list = new ListTag();
            for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                list.add(inventory.getItem(i).save(new CompoundTag()));
            }
            compound.put("Inventory", list);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Inventory")) {
            ListTag list = compound.getList("Inventory", 10);
            inventory = new SimpleContainer(list.size());
            for (int i = 0; i < list.size(); i++) {
                inventory.setItem(i, ItemStack.of(list.getCompound(i)));
            }
            entityData.set(CHESTS, list.size() > 27 ? 2 : 1);
        }
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        if (inventory == null) {
            return null;
        }
        return inventory.getContainerSize() < 54
                ? ChestMenu.threeRows(containerId, playerInventory, inventory)
                : ChestMenu.sixRows(containerId, playerInventory, inventory);
    }

    public int getChestCount() {
        return entityData.get(CHESTS);
    }

    static class WaterPathNavigator extends GroundPathNavigation {
        WaterPathNavigator(CapybaraEntity entity, Level level) {
            super(entity, level);
        }

        @Override
        protected PathFinder createPathFinder(int maxVisitedNodes) {
            this.nodeEvaluator = new WalkNodeEvaluator();
            return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
        }

        @Override
        protected boolean hasValidPathType(BlockPathTypes pathType) {
            return pathType == BlockPathTypes.WATER || super.hasValidPathType(pathType);
        }

        @Override
        public boolean isStableDestination(BlockPos pos) {
            return this.level.getBlockState(pos).is(Blocks.WATER) || super.isStableDestination(pos);
        }
    }
}