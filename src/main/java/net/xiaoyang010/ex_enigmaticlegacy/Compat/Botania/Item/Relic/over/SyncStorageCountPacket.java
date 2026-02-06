package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncStorageCountPacket {
    private int count;

    public SyncStorageCountPacket(int count) {
        this.count = count;
    }

    public static void encode(SyncStorageCountPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.count);
    }

    public static SyncStorageCountPacket decode(FriendlyByteBuf buf) {
        return new SyncStorageCountPacket(buf.readInt());
    }

    public static void handle(SyncStorageCountPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                var info = PlayerUnlockData.CLIENT_CACHE.computeIfAbsent(
                        player.getUUID(), k -> new PlayerUnlockData.UnlockInfo());
                info.storageCount = msg.count;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}