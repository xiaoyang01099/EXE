package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;
import vazkii.botania.common.item.block.ItemBlockTinyPotato;

import javax.annotation.Nullable;

public class BelieverTile extends TileEntityGeneratingFlower {
    private static final String TAG_COOLDOWN = "cooldown";
    private static final int MANA_PER_POTATO = 1452;
    private int cooldown = 0;

    public BelieverTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        if (level == null || level.isClientSide) {
            return;
        }

        if(cooldown > 0) {
            cooldown--;
        }

        for(ItemEntity item : level.getEntitiesOfClass(ItemEntity.class,
                new AABB(getEffectivePos(), getEffectivePos().offset(1, 1, 1)),
                entity -> entity.getItem().getItem() instanceof ItemBlockTinyPotato
                        && entity.getAge() >= getSlowdownFactor())) {

            ItemStack stack = item.getItem();
            stack.shrink(1);
            addMana(MANA_PER_POTATO);

            // 播放声音
            level.playSound(null, getEffectivePos(),
                    SoundEvents.GENERIC_EAT,
                    SoundSource.BLOCKS, 0.2F, 0.6F);

            // 生成物品粒子
            if(level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.WITCH,
                        item.getX(), item.getY(), item.getZ(),
                        20,
                        0.1D, 0.1D, 0.1D,
                        0.05D
                );
            }
            break;
        }
    }

    private int getSlowdownFactor() {
        return 5;
    }

    @Override
    public void writeToPacketNBT(CompoundTag tag) {
        super.writeToPacketNBT(tag);
        tag.putInt(TAG_COOLDOWN, cooldown);
    }

    @Override
    public void readFromPacketNBT(CompoundTag tag) {
        super.readFromPacketNBT(tag);
        cooldown = tag.getInt(TAG_COOLDOWN);
    }

    @Override
    public int getColor() {
        return 0xD3D604;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(() -> new GeneratingWandHud(this)).cast());
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), 1);
    }

    @Override
    public int getMaxMana() {
        return 10000;
    }
}








//这边是右键抚摸获得mana但是有bug，没有办法获得

/*public class BelieverTile extends TileEntityGeneratingFlower {
    private static final int MAX_MANA = 20000;
    private static final int TINY_POTATO_MANA = 50;
    private static final int INFINITY_POTATO_MANA = 10000;
    private static final int RANGE = 5;
    private static final int PAT_COOLDOWN = 20;

    private int cooldown = 0;
    private int storedMana = 0;

    public BelieverTile(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.BELIEVERTILE.get(), blockPos, blockState);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (!level.isClientSide) {
            if (cooldown > 0) {
                cooldown--;
            }

            if (storedMana > 0) {
                int manaToAdd = Math.min(storedMana, MAX_MANA - getMana());
                addMana(manaToAdd);
                storedMana = Math.max(0, storedMana - manaToAdd);
            }
        }
    }

    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && cooldown <= 0) {
            BlockPos flowerPos = getEffectivePos();
            boolean interacted = false;

            for (BlockPos checkPos : BlockPos.betweenClosed(
                    flowerPos.offset(-RANGE, -RANGE, -RANGE),
                    flowerPos.offset(RANGE, RANGE, RANGE))) {
                BlockState targetState = level.getBlockState(checkPos);

                if (targetState.getBlock() instanceof BlockTinyPotato) {
                    storedMana += TINY_POTATO_MANA;
                    // 添加粒子效果
                    level.addParticle(ParticleTypes.HEART,
                            checkPos.getX() + 0.5, checkPos.getY() + 0.5, checkPos.getZ() + 0.5,
                            0, 0.1, 0);
                    interacted = true;
                } else if (targetState.getBlock() instanceof InfinityPotato) {
                    if (player.isCrouching()) {
                        storedMana += INFINITY_POTATO_MANA * 2;
                    } else {
                        storedMana += INFINITY_POTATO_MANA;
                    }
                    // 爆炸粒子和音效
                    level.addParticle(ParticleTypes.EXPLOSION,
                            checkPos.getX() + 0.5, checkPos.getY() + 0.5, checkPos.getZ() + 0.5,
                            0, 0.1, 0);
                    level.playSound(null, checkPos, SoundEvents.GENERIC_EXPLODE,
                            SoundSource.BLOCKS, 0.5f, 1.0f);
                    interacted = true;
                }
            }

            if (interacted) {
                cooldown = PAT_COOLDOWN;
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        return 0xC2B280;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(() -> new GeneratingWandHud(this)).cast());
    }
}*/