package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HotbarSwapPacket {

    public HotbarSwapPacket() {}

    public static void encode(HotbarSwapPacket msg, FriendlyByteBuf buf) {}

    public static HotbarSwapPacket decode(FriendlyByteBuf buf) {
        return new HotbarSwapPacket();
    }

    public static void handle(HotbarSwapPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) {
                UtilInventory.swapHotbars(p);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}