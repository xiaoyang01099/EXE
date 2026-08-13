package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.functional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.SyncSnowOverridePacket;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import java.util.HashSet;
import java.util.Set;

public class FrostBlossomTile extends FunctionalFlowerBlockEntity {
    private static final int MANA_COST = 5000;
    private static final int MAX_MANA = 100000;
    private static final int COOLDOWN_TIME = 20;
    private int cooldownTicks = 0;
    private boolean hasConvertedCurrentRain = false;
    public static final Set<BlockPos> SNOW_FLOWER_POSITIONS = new HashSet<>();

    public static boolean IS_SNOWING_OVERRIDE = false;

    public FrostBlossomTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static class FunctionalWandHud extends BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<FrostBlossomTile> {
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
                removeFromActive();
                return;
            }

            boolean isRaining = getLevel().isRaining();

            if (!isRaining) {
                if (hasConvertedCurrentRain) {
                    cooldownTicks = COOLDOWN_TIME;
                }
                hasConvertedCurrentRain = false;
                removeFromActive();
                return;
            }

            if (hasConvertedCurrentRain && !SNOW_FLOWER_POSITIONS.contains(getBlockPos())) {
                addToActive();
            }

            if (!hasConvertedCurrentRain && getMana() >= MANA_COST) {
                addToActive();
                hasConvertedCurrentRain = true;
                addMana(-MANA_COST);
            }

            updateGlobalOverride();
        }
    }

    private void addToActive() {
        SNOW_FLOWER_POSITIONS.add(getBlockPos());
        updateGlobalOverride();
    }

    private void removeFromActive() {
        SNOW_FLOWER_POSITIONS.remove(getBlockPos());
        updateGlobalOverride();
    }

    public static void updateGlobalOverride() {
        boolean newValue = !SNOW_FLOWER_POSITIONS.isEmpty();
        if (newValue != IS_SNOWING_OVERRIDE) {
            IS_SNOWING_OVERRIDE = newValue;
            NetworkHandler.CHANNEL.send(
                    PacketDistributor.ALL.noArg(),
                    new SyncSnowOverridePacket(IS_SNOWING_OVERRIDE)
            );
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        removeFromActive();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        removeFromActive();
    }

    @Override
    public @Nullable RadiusDescriptor getRadius() {
        return null;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap,
                LazyOptional.of(() -> new FunctionalWandHud(this)).cast());
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        if (cooldownTicks > 0) return 0xFF0000;
        if (hasConvertedCurrentRain) return 0xFFFFFF;
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