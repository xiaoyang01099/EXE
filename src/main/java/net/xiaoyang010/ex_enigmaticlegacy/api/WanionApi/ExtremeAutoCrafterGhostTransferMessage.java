package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IGhostAcceptorContainer;


import java.util.function.Supplier;

public class ExtremeAutoCrafterGhostTransferMessage {
    private int windowId;
    private int slot;
    private ItemStack itemStack;

    public ExtremeAutoCrafterGhostTransferMessage() {}

    public ExtremeAutoCrafterGhostTransferMessage(final int windowId, final int slot, final ItemStack itemStack) {
        this.windowId = windowId;
        this.slot = slot;
        this.itemStack = itemStack;
    }

    public static void encode(ExtremeAutoCrafterGhostTransferMessage message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.windowId);
        buf.writeVarInt(message.slot);
        buf.writeItem(message.itemStack);
    }

    public static ExtremeAutoCrafterGhostTransferMessage decode(FriendlyByteBuf buf) {
        ExtremeAutoCrafterGhostTransferMessage message = new ExtremeAutoCrafterGhostTransferMessage();
        message.windowId = buf.readVarInt();
        message.slot = buf.readVarInt();
        message.itemStack = buf.readItem();
        return message;
    }

    public static void handle(ExtremeAutoCrafterGhostTransferMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer != null &&
                    serverPlayer.containerMenu instanceof IGhostAcceptorContainer &&
                    serverPlayer.containerMenu.containerId == message.windowId) {

                ((IGhostAcceptorContainer) serverPlayer.containerMenu).acceptGhostStack(message.slot, message.itemStack);
            }
        });

        context.setPacketHandled(true);
    }
}