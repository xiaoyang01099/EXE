package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SwapInvoPacket {

    private int inventoryGroup;

    public SwapInvoPacket() {
        this.inventoryGroup = 1;
    }

    public SwapInvoPacket(int group) {
        this.inventoryGroup = group;
    }

    public static void encode(SwapInvoPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.inventoryGroup);
    }

    public static SwapInvoPacket decode(FriendlyByteBuf buf) {
        return new SwapInvoPacket(buf.readInt());
    }

    public static void handle(SwapInvoPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) {
                UtilInventory.swapInventoryGroup(p, msg.inventoryGroup);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}