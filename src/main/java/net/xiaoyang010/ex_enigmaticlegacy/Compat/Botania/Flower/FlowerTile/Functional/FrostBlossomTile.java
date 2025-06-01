package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

import java.util.HashSet;
import java.util.Set;


public class FrostBlossomTile extends TileEntityFunctionalFlower {
    private static final int MANA_COST = 5000;
    private static final int MAX_MANA = 100000;
    private static final int COOLDOWN_TIME = 20;
    private int cooldownTicks = 0;
    private boolean hasConvertedCurrentRain = false;

    public static Set<BlockPos> SNOW_FLOWER_POSITIONS = new HashSet<>();

    public FrostBlossomTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static class FunctionalWandHud extends TileEntityFunctionalFlower.FunctionalWandHud<FrostBlossomTile> {
        public FunctionalWandHud(FrostBlossomTile flower) {
            super(flower);
        }
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (!getLevel().isClientSide) {
            if (cooldownTicks > 0) {
                cooldownTicks--;
                SNOW_FLOWER_POSITIONS.remove(getBlockPos());
                return;
            }

            boolean isRaining = getLevel().isRaining();

            if (!isRaining) {
                if (hasConvertedCurrentRain) {
                    cooldownTicks = COOLDOWN_TIME;
                }
                hasConvertedCurrentRain = false;
                SNOW_FLOWER_POSITIONS.remove(getBlockPos());
                return;
            }

            if (isRaining && !hasConvertedCurrentRain && getMana() >= MANA_COST && cooldownTicks <= 0) {
                SNOW_FLOWER_POSITIONS.add(getBlockPos());
                hasConvertedCurrentRain = true;
                addMana(-MANA_COST);
            }
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        SNOW_FLOWER_POSITIONS.remove(getBlockPos());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        SNOW_FLOWER_POSITIONS.remove(getBlockPos());
    }

    @Override
    public @Nullable RadiusDescriptor getRadius() {
        return null;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new FunctionalWandHud(this)).cast());
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        if (cooldownTicks > 0) {
            return 0xFF0000;
        }
        if (hasConvertedCurrentRain) {
            return 0xFFFFFF;
        }
        return 0xAAAAAA;
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putBoolean("hasConverted", hasConvertedCurrentRain);
        cmp.putInt("cooldownTicks", cooldownTicks);
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        hasConvertedCurrentRain = cmp.getBoolean("hasConverted");
        cooldownTicks = cmp.getInt("cooldownTicks");
    }
}