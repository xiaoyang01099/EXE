package org.xiaoyang.ex_enigmaticlegacy.Entity.biological;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public abstract class BlockEntityBase extends BotaniaBlockEntity {
    private final Set<Capability<?>> caps;

    public BlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state, Capability<?>... caps) {
        super(type, pos, state);
        this.caps = ImmutableSet.copyOf(caps);
    }

    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return this.caps.contains(cap) ? (LazyOptional<T>) LazyOptional.of(() -> this) : super.getCapability(cap, side);
    }

    public abstract void setColor(DyeColor color);
}