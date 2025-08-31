package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IGhostAcceptorContainer;


import java.util.function.Supplier;

public class ExtremeAutoCrafterGhostTransferMessage {
    private final int windowId;
    private final int slot;
    private final ItemStack itemStack;

    public ExtremeAutoCrafterGhostTransferMessage(int windowId, int slot, ItemStack itemStack) {
        this.windowId = windowId;
        this.slot = slot;
        this.itemStack = itemStack;
    }

    public static ExtremeAutoCrafterGhostTransferMessage decode(FriendlyByteBuf buf) {
        int windowId = buf.readVarInt();
        int slot = buf.readVarInt();
        ItemStack itemStack = buf.readItem();
        return new ExtremeAutoCrafterGhostTransferMessage(windowId, slot, itemStack);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(windowId);
        buf.writeVarInt(slot);
        buf.writeItem(itemStack);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null &&
                    player.containerMenu instanceof IGhostAcceptorContainer &&
                    player.containerMenu.containerId == windowId) {
                ((IGhostAcceptorContainer) player.containerMenu).acceptGhostStack(slot, itemStack);
            }
        });
        return true;
    }
}