package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public interface ISmartNBT
{
    @Nonnull
    default CompoundTag writeNBT()
    {
        return new CompoundTag();
    }

    default void afterWriteNBT(@Nonnull final CompoundTag smartNBT) {}

    void readNBT(@Nonnull CompoundTag smartNBT);
}