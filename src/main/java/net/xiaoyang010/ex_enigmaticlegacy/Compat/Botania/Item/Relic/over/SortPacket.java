package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SortPacket {

    public SortPacket() {}

    public static void encode(SortPacket msg, FriendlyByteBuf buf) {}

    public static SortPacket decode(FriendlyByteBuf buf) {
        return new SortPacket();
    }

    public static void handle(SortPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) {
                UtilInventory.doSort(p);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}