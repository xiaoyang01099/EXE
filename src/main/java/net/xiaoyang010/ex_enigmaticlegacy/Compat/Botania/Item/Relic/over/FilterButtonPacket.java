package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;

import java.util.ArrayList;
import java.util.function.Supplier;

public class FilterButtonPacket {

    public FilterButtonPacket() {}

    public static void encode(FilterButtonPacket msg, FriendlyByteBuf buf) {}

    public static FilterButtonPacket decode(FriendlyByteBuf buf) {
        return new FilterButtonPacket();
    }

    public static void handle(FilterButtonPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer p = ctx.get().getSender();
            if (p == null) return;

            ArrayList<Container> locations = UtilInventory.findTileEntityInventories(
                    p, ConfigHandler.FILTER_RANGE.get());

            for (Container inventory : locations) {
                UtilInventory.sortFromPlayerToInventory(p.level, inventory, p);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}