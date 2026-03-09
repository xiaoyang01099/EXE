package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.common.ModStats;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class InfinityPotatoTile extends BlockEntity implements Container, WorldlyContainer {
    public static final int JUMP_DURATION = 0;

    private static final String TAG_NAME = "name";
    private static final String TAG_ITEMS = "items";
    private static final String TAG_JUMP_TICKS = "jumpTicks";

    public int jumpTicks = 0; // 用于控制跳跃的tick数
    public Component name = new TextComponent(""); // 方块的自定义名称
    private final NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY); // 存储物品

    public InfinityPotatoTile(@NotNull BlockEntityType<InfinityPotatoTile> Type, BlockPos pos, BlockState state) {
        super(Type, pos, state);
    }

    // 设置自定义名称
    public void setCustomName(Component name) {
        this.name = name;
    }

    // 处理玩家与方块的交互
    public void interact(Player player, InteractionHand hand) {
        if (level != null){
            ItemStack heldItem = player.getItemInHand(hand);
            Direction facing = level.getBlockState(worldPosition).getValue(BlockStateProperties.HORIZONTAL_FACING);
            int index = facing.get3DDataValue();

            // 检查是否拿着无限土豆物品
            if (!level.isClientSide && !heldItem.isEmpty() && heldItem.getItem() == ModBlockss.INFINITY_POTATO.get().asItem()) {
                player.sendMessage(new TextComponent("Don't touch my son!"), Util.NIL_UUID);
                return;
            }

            if (!heldItem.isEmpty()) {
                // 往土豆上放物品
                ItemStack stackInSlot = getItem(index);
                if (stackInSlot.isEmpty()) {
                    setItem(index, heldItem.split(1));
                } else if (!player.isCreative()) {
                    // 如果已有物品，返回给玩家
                    if (player.getInventory().add(stackInSlot)) {
                        setItem(index, ItemStack.EMPTY);
                    }
                }
            } else {
                // 空手拿走物品
                ItemStack stackInSlot = getItem(index);
                if (!stackInSlot.isEmpty()) {
                    player.setItemInHand(hand, stackInSlot);
                    setItem(index, ItemStack.EMPTY);
                }
            }

            // 生成粒子和音效效果
            generateParticlesAndSound(level, this.getBlockPos(), player);

            if (!level.isClientSide) {
                // 如果玩家蹲下并且空手，给予增益效果
                if (player.isCrouching() && player.getItemInHand(hand).isEmpty()) {
                    InfinityPotatoTile.addEffects(level, this.getBlockPos(), player);
                }
                // 奖励统计
                player.awardStat(ModStats.TINY_POTATOES_PETTED);
            }

            setChanged();
        }
    }

    // 生成粒子和音效的功能
    private void generateParticlesAndSound(Level world, BlockPos pos, Player player) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        Random rand = world.getRandom();

        // 生成心形粒子
        for (int i = 0; i < 50; i++) {
            world.addParticle(ParticleTypes.HEART, x + rand.nextDouble(), y +0.5d+ rand.nextDouble(), z + rand.nextDouble(), 0.0D, 0.1D + rand.nextDouble(), 0.0D);
        }

        // 生成爆炸粒子和音效（当玩家蹲下时）
        if (player.isCrouching()) {
            world.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0f, 1.0f);
            for (int i = 0; i < 20; i++) {
                world.addParticle(ParticleTypes.EXPLOSION, x + rand.nextDouble(), y + rand.nextDouble(), z + rand.nextDouble(), 0.1D, 0.1D + rand.nextDouble(), 0.1D);
            }
        }
    }

    // 增益效果
    public static void addEffects(Level level, BlockPos pos, Player player) {
        double radius = 10.5;
        AABB bb = new AABB(pos.offset(-radius, -2, -radius), pos.offset(radius, 2, radius));
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, bb);

        for (LivingEntity entity : entities) {
            // 给予增益效果
            entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 24000, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 24000, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 24000, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.LUCK, 24000, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 24000, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 24000, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 24000, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 24000, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 24000, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.SATURATION, 24000, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 24000, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.HEAL, 24000, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 24000, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 24000, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 24000, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.JUMP, 24000, 2));
            entity.addEffect(new MobEffectInstance(ModEffects.DAMAGE_REDUCTION.get(), 24000, 2));
            entity.addEffect(new MobEffectInstance(ModEffects.CREEPER_FRIENDLY.get(), 24000, 2));

        }

        // 增加玩家的饱食度
        player.getFoodData().eat(20, 30.0F);
    }

    // 每tick调用，用于更新跳跃逻辑
    public static void tick(Level level, BlockPos pos, BlockState state, InfinityPotatoTile self) {
        if (self.jumpTicks > 0) {
            --self.jumpTicks;
        }

        //随机触发跳跃
        if (level.getGameTime() % 100 == 0 && level.random.nextInt(2) == 0)
            self.jumpTicks = 20;
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 0) {
            this.jumpTicks = param;
            return true;
        } else {
            return super.triggerEvent(id, param);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
        }

    }

    // 保存到NBT
    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(TAG_NAME, Component.Serializer.toJson(name));
        tag.putInt(TAG_JUMP_TICKS, jumpTicks);
        ContainerHelper.saveAllItems(tag, items); // 保存物品
    }

    // 从NBT加载
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        name = Component.Serializer.fromJson(tag.getString(TAG_NAME));
        jumpTicks = tag.getInt(TAG_JUMP_TICKS);
        ContainerHelper.loadAllItems(tag, items); // 加载物品
    }
    // 容器逻辑（物品管理）

    @Nonnull
    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        items.set(index, stack);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    // 从容器中移除指定数量的物品并返回这些物品
    @Override
    public ItemStack removeItem(int index, int count) {
        return ContainerHelper.removeItem(items, index, count);
    }

    // 从指定槽位移除物品并返回该物品，不更新容器
    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = items.get(index);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            items.set(index, ItemStack.EMPTY);  // 清除该槽位的物品
            return stack;
        }
    }

    // 检查玩家是否可以和容器进行交互
    @Override
    public boolean stillValid(Player player) {
        // 这里的检查通常是根据方块的位置和玩家的距离来判断
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            // 通常会检查玩家与方块的距离，通常为 64 平方
            return player.distanceToSqr((double) this.worldPosition.getX() + 0.5D,
                    (double) this.worldPosition.getY() + 0.5D,
                    (double) this.worldPosition.getZ() + 0.5D) <= 64.0D;
        }
    }

    // 获取能力
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> new SidedInvWrapper(this, side)));
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @org.jetbrains.annotations.Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
        return false;
    }
}
