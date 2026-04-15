package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.tile;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModBlocks;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEffects;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.common.BotaniaStats;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class InfinityPotatoTile extends BlockEntity implements Container, WorldlyContainer {
    private static final String TAG_NAME = "name";
    private static final String TAG_JUMP_TICKS = "jumpTicks";
    private static final String TAG_NEXT_DO_IT = "nextDoIt";
    public int jumpTicks = 0;
    public int nextDoIt = 0;
    public Component name = Component.translatable("");
    private final NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);

    public InfinityPotatoTile(@NotNull BlockEntityType<InfinityPotatoTile> Type,
                              BlockPos pos, BlockState state) {
        super(Type, pos, state);
    }

    public void setCustomName(Component name) {
        this.name = name;
    }

    public void interact(Player player, InteractionHand hand) {
        if (level != null) {
            ItemStack heldItem = player.getItemInHand(hand);
            Direction facing = level.getBlockState(worldPosition)
                    .getValue(BlockStateProperties.HORIZONTAL_FACING);
            int index = facing.get3DDataValue();

            if (!level.isClientSide && !heldItem.isEmpty()
                    && heldItem.getItem() == ModBlocks.INFINITY_POTATO.get().asItem()) {
                player.sendSystemMessage(Component.translatable("Don't touch my son!"));
                return;
            }

            if (!heldItem.isEmpty()) {
                ItemStack stackInSlot = getItem(index);
                if (stackInSlot.isEmpty()) {
                    setItem(index, heldItem.split(1));
                } else if (!player.isCreative()) {
                    if (player.getInventory().add(stackInSlot)) {
                        setItem(index, ItemStack.EMPTY);
                    }
                }
            } else {
                ItemStack stackInSlot = getItem(index);
                if (!stackInSlot.isEmpty()) {
                    player.setItemInHand(hand, stackInSlot);
                    setItem(index, ItemStack.EMPTY);
                }
            }

            jump();

            String currentName = name.getString().toLowerCase(Locale.ROOT).trim();
            if (currentName.equals("shia labeouf") && !level.isClientSide && nextDoIt == 0) {
                nextDoIt = 40;
                var doitSound = ForgeRegistries.SOUND_EVENTS
                        .getValue(new ResourceLocation("botania", "doit"));
                if (doitSound != null) {
                    level.playSound(null, player.blockPosition(), doitSound,
                            SoundSource.BLOCKS, 2.5F, 0.7F);
                }
            }

            generateParticlesAndSound(level, this.getBlockPos(), player);

            if (!level.isClientSide) {
                if (player.isCrouching() && player.getItemInHand(hand).isEmpty()) {
                    InfinityPotatoTile.addEffects(level, this.getBlockPos(), player);
                }
                player.awardStat(BotaniaStats.TINY_POTATOES_PETTED);
            }

            setChanged();
        }
    }

    public void jump() {
        if (jumpTicks == 0) {
            jumpTicks = 40;
            if (level != null && !level.isClientSide) {
                level.blockEvent(worldPosition, getBlockState().getBlock(), 0, 40);
            }
        }
    }

    private void generateParticlesAndSound(Level world, BlockPos pos, Player player) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        RandomSource rand = world.getRandom();

        for (int i = 0; i < 50; i++) {
            world.addParticle(ParticleTypes.HEART,
                    x + rand.nextDouble(), y + 0.5d + rand.nextDouble(),
                    z + rand.nextDouble(), 0.0D, 0.1D + rand.nextDouble(), 0.0D);
        }

        if (player.isCrouching()) {
            world.playSound(null, pos, SoundEvents.GENERIC_EXPLODE,
                    SoundSource.BLOCKS, 1.0f, 1.0f);
            for (int i = 0; i < 20; i++) {
                world.addParticle(ParticleTypes.EXPLOSION,
                        x + rand.nextDouble(), y + rand.nextDouble(),
                        z + rand.nextDouble(), 0.1D, 0.1D + rand.nextDouble(), 0.1D);
            }
        }
    }

    public static void addEffects(Level level, BlockPos pos, Player player) {
        double radius = 10.5;
        AABB bb = new AABB(
                pos.offset((int) -radius, -2, (int) -radius),
                pos.offset((int) radius, 2, (int) radius));
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, bb);

        for (LivingEntity entity : entities) {
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

        player.getFoodData().eat(20, 30.0F);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, InfinityPotatoTile self) {
        if (self.jumpTicks > 0) {
            --self.jumpTicks;

            if (self.jumpTicks == 20 || self.jumpTicks == 0) {
                if (!level.isClientSide) {
                    level.explode(
                            null,
                            null,
                            null,
                            pos.getX() + 0.5,
                            pos.getY(),
                            pos.getZ() + 0.5,
                            0.0f,
                            false,
                            Level.ExplosionInteraction.NONE
                    );
                }
            }
        }

        if (self.nextDoIt > 0) {
            self.nextDoIt--;
        }

        if (level.getGameTime() % 100 == 0 && level.random.nextInt(2) == 0) {
            self.jumpTicks = 20;
        }
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

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(TAG_NAME, Component.Serializer.toJson(name));
        tag.putInt(TAG_JUMP_TICKS, jumpTicks);
        tag.putInt(TAG_NEXT_DO_IT, nextDoIt);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        name = Component.Serializer.fromJson(tag.getString(TAG_NAME));
        jumpTicks = tag.getInt(TAG_JUMP_TICKS);
        nextDoIt = tag.getInt(TAG_NEXT_DO_IT);
        ContainerHelper.loadAllItems(tag, items);
    }

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

    @Override
    public ItemStack removeItem(int index, int count) {
        return ContainerHelper.removeItem(items, index, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = items.get(index);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            items.set(index, ItemStack.EMPTY);
            return stack;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return player.distanceToSqr(
                    (double) this.worldPosition.getX() + 0.5D,
                    (double) this.worldPosition.getY() + 0.5D,
                    (double) this.worldPosition.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap,
                                             @Nullable Direction side) {
        return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap,
                LazyOptional.of(() -> new SidedInvWrapper(this, side)));
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
        return false;
    }
}