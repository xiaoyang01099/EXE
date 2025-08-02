package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public interface IStateProvider<C extends IControl<C>, S extends IState<S>> extends IControl<C>
{
    S getState();

    void setState(@Nonnull S state);

    void writeToNBT(@Nonnull CompoundTag compoundTag, @Nonnull S state);
}