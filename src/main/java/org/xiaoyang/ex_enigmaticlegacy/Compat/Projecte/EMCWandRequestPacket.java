package org.xiaoyang.ex_enigmaticlegacy.Compat.Projecte;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EMCWandRequestPacket {
    private final boolean showAll;

    public EMCWandRequestPacket(boolean showAll) {
        this.showAll = showAll;
    }

    public static void encode(EMCWandRequestPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.showAll);
    }

    public static EMCWandRequestPacket decode(FriendlyByteBuf buf) {
        return new EMCWandRequestPacket(buf.readBoolean());
    }

    public static void handle(EMCWandRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            boolean canShowAll = msg.showAll && (player.hasPermissions(2) || EMCWandHelper.isAuthorized(player.getUUID()));

            var allItems = EMCWandHelper.getAllItems();
            List<EMCWandItemListPacket.ItemEmcData> dataList = new ArrayList<>();

            for (var entry : allItems) {
                if (!canShowAll && entry.hasEmc()) continue;
                var key = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(entry.item());
                if (key == null) continue;
                dataList.add(new EMCWandItemListPacket.ItemEmcData(key.toString(), entry.emcValue()));
            }

            int CHUNK_SIZE = 200;
            if (dataList.size() <= CHUNK_SIZE) {
                NetworkHandler.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new EMCWandItemListPacket(dataList, canShowAll, false, true)
                );
            } else {
                int totalChunks = (int) Math.ceil((double) dataList.size() / CHUNK_SIZE);
                for (int i = 0; i < totalChunks; i++) {
                    int start = i * CHUNK_SIZE;
                    int end = Math.min(start + CHUNK_SIZE, dataList.size());
                    List<EMCWandItemListPacket.ItemEmcData> chunk = dataList.subList(start, end);
                    boolean isFirst = (i == 0);
                    boolean isLast = (i == totalChunks - 1);
                    NetworkHandler.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new EMCWandItemListPacket(chunk, canShowAll, !isFirst, isLast)
                    );
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}