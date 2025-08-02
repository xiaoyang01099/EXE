package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.ISmartNBT;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public final class SmartNBTMessage {
    private int windowId;
    private CompoundTag nbtTagCompound;

    public SmartNBTMessage() {}

    public SmartNBTMessage(final int windowId, final CompoundTag nbtTagCompound) {
        this.windowId = windowId;
        this.nbtTagCompound = nbtTagCompound;
    }

    public static SmartNBTMessage decode(FriendlyByteBuf buf) {
        SmartNBTMessage message = new SmartNBTMessage();
        message.windowId = buf.readVarInt();
        message.nbtTagCompound = buf.readNbt();
        return message;
    }

    public static void encode(SmartNBTMessage message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.windowId);
        buf.writeNbt(message.nbtTagCompound);
    }

    public int getWindowId() {
        return windowId;
    }

    public CompoundTag getCompoundTag() {
        return nbtTagCompound;
    }

    public static void handle(final SmartNBTMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer entityPlayer = context.getSender();
            if (entityPlayer != null && entityPlayer.containerMenu.containerId == message.windowId && entityPlayer.containerMenu instanceof ISmartNBT) {
                ((ISmartNBT) entityPlayer.containerMenu).readNBT(message.nbtTagCompound);
            }
        });
        context.setPacketHandled(true);
    }
}