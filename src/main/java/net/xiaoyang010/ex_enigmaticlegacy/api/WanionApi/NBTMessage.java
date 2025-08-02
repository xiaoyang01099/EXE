package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.INBTMessage;

import java.util.function.Supplier;

public class NBTMessage
{
    private int windowId;
    private CompoundTag nbtMessage;

    public NBTMessage() {}

    public NBTMessage(final int windowId, final CompoundTag nbtMessage)
    {
        this.windowId = windowId;
        this.nbtMessage = nbtMessage;
    }

    public NBTMessage(FriendlyByteBuf buf)
    {
        this.windowId = buf.readVarInt();
        this.nbtMessage = buf.readNbt();
    }

    public static void encode(NBTMessage message, FriendlyByteBuf buf)
    {
        message.toBytes(buf);
    }

    public static NBTMessage decode(FriendlyByteBuf buf)
    {
        return new NBTMessage(buf);
    }

    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeVarInt(windowId);
        buf.writeNbt(nbtMessage);
    }

    public int getWindowId()
    {
        return windowId;
    }

    public CompoundTag getNbtMessage()
    {
        return nbtMessage;
    }

    public static void handle(NBTMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Player player = null;

            if (context.getDirection().getReceptionSide().isServer()) {
                player = context.getSender();
            } else {
                player = Minecraft.getInstance().player;
            }

            if (player != null &&
                    player.containerMenu.containerId == message.getWindowId() &&
                    player.containerMenu instanceof INBTMessage) {
                ((INBTMessage) player.containerMenu).receiveNBT(message.getNbtMessage());
            }
        });

        context.setPacketHandled(true);
    }

    public static class Handler
    {
        public static void onMessage(final NBTMessage message, Supplier<NetworkEvent.Context> contextSupplier)
        {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                Player player = null;

                if (context.getDirection().getReceptionSide().isServer()) {
                    player = context.getSender();
                } else {
                    player = Minecraft.getInstance().player;
                }

                if (player != null &&
                        player.containerMenu.containerId == message.getWindowId() &&
                        player.containerMenu instanceof INBTMessage) {
                    ((INBTMessage) player.containerMenu).receiveNBT(message.getNbtMessage());
                }
            });

            context.setPacketHandled(true);
        }
    }
}