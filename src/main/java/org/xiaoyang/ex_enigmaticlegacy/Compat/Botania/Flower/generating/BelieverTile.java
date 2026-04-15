package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.generating;

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
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

public class BelieverTile extends GeneratingFlowerBlockEntity {
    private static final String TAG_COOLDOWN = "cooldown";
    private static final int MANA_PER_TINY_POTATO = 1452;    // 普通小土豆产能
    private static final int MANA_PER_INFINITY_POTATO = 10000;  // 无尽小土豆产能
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
    }

    public void addRightMana(boolean isInfinityPotato) {
        if (cooldown != 0) return;

        int manaToAdd = isInfinityPotato ? MANA_PER_INFINITY_POTATO : MANA_PER_TINY_POTATO;
        addMana(manaToAdd);
        cooldown = 20;
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
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<>(this)).cast());
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