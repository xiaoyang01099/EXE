package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IControlNameable;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IState;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IStateNameable;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IStateProvider;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class RedstoneControl implements IStateProvider<RedstoneControl, RedstoneControl.RedstoneState>, IControlNameable {
    private final BlockEntity blockEntity;
    private RedstoneState state = RedstoneState.IGNORED;
    public static final ResourceLocation GUI_TEXTURES = new ResourceLocation("ex_enigmaticlegacy:textures/gui/gui_textures.png");

    public RedstoneControl(@Nonnull final BlockEntity blockEntity)
    {
        this.blockEntity = blockEntity;
    }

    public RedstoneControl(@Nonnull final BlockEntity blockEntity, @Nonnull final RedstoneState state) {
        this.blockEntity = blockEntity;
        this.state = state;
    }

    @Override
    public boolean canOperate() {
        if (state == RedstoneState.IGNORED)
            return true;

        final Level level = blockEntity.getLevel();
        final BlockPos pos = blockEntity.getBlockPos();

        if (level == null)
            return false;

        final boolean powered = level.hasNeighborSignal(pos);
        return state == RedstoneState.OFF && !powered || state == RedstoneState.ON && powered;
    }

    @Nonnull
    @Override
    public CompoundTag writeNBT() {
        final CompoundTag redstoneControlNBT = new CompoundTag();
        redstoneControlNBT.putInt("RedstoneControl", state.ordinal());
        return redstoneControlNBT;
    }

    @Override
    public void readNBT(@Nonnull final CompoundTag compoundTag) {
        if (compoundTag.contains("RedstoneControl"))
            state = RedstoneState.values()[Mth.clamp(compoundTag.getInt("RedstoneControl"), 0, RedstoneState.values().length - 1)];
    }

    @Nonnull
    @Override
    public RedstoneControl copy()
    {
        return new RedstoneControl(blockEntity, state);
    }

    @Nonnull
    @Override
    public String getControlName()
    {
        return "ex_enigmaticlegacy.redstone.control";
    }

    @Override
    public RedstoneState getState()
    {
        return state;
    }

    @Override
    public void setState(@Nonnull final RedstoneState state)
    {
        this.state = state;
    }

    @Override
    public void writeToNBT(@Nonnull final CompoundTag compoundTag, @Nonnull final RedstoneState state) {
        compoundTag.putInt("RedstoneControl", state.ordinal());
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || (obj instanceof RedstoneControl && this.state == ((RedstoneControl) obj).state);
    }

    public enum RedstoneState implements IState<RedstoneState>, IStateNameable {
        IGNORED,
        OFF,
        ON;

        @Nonnull
        @Override
        public RedstoneState getNextState() {
            final int nextState = ordinal() + 1;
            return nextState > values().length - 1 ? values()[0] : values()[nextState];
        }

        @Nonnull
        @Override
        public RedstoneState getPreviousState() {
            final int previousState = ordinal() - 1;
            return previousState >= 0 ? values()[previousState] : values()[values().length - 1];
        }

        @Override
        @Nullable
        public ResourceLocation getTextureResourceLocation()
        {
            return GUI_TEXTURES;
        }

        @Override
        @Nullable
        public Pair<Integer, Integer> getTexturePos(final boolean hovered) {
            return new ImmutablePair<>(!hovered ? 0 : 18, 54 + (18 * ordinal()));
        }

        @Nonnull
        @Override
        public String getStateName()
        {
            return "ex_enigmaticlegacy.redstone.control.state." + name().toLowerCase();
        }
    }
}