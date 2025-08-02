package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.NBTMessage;

import javax.annotation.Nonnull;

public interface INBTMessage
{
    static void sendNBT(final int windowId, @Nonnull final CompoundTag compoundTag)
    {
        NetworkHandler.sendToServer(new NBTMessage(windowId, compoundTag));
    }

    static void sendNBT(final int windowId, @Nonnull final CompoundTag compoundTag, @Nonnull final ServerPlayer serverPlayer)
    {
        NetworkHandler.sendToPlayer(new NBTMessage(windowId, compoundTag), serverPlayer);
    }

    void receiveNBT(@Nonnull final CompoundTag compoundTag);
}