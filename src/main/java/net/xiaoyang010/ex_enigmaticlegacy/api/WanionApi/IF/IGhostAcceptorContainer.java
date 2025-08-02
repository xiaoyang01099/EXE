package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public interface IGhostAcceptorContainer
{
    void acceptGhostStack(int slot, @Nonnull ItemStack itemStack);
}