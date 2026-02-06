package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncChestUnlockPacket {
    private boolean unlocked;

    public SyncChestUnlockPacket(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public static void encode(SyncChestUnlockPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.unlocked);
    }

    public static SyncChestUnlockPacket decode(FriendlyByteBuf buf) {
        return new SyncChestUnlockPacket(buf.readBoolean());
    }

    public static void handle(SyncChestUnlockPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                var info = PlayerUnlockData.CLIENT_CACHE.computeIfAbsent(
                        player.getUUID(), k -> new PlayerUnlockData.UnlockInfo());
                info.eChestUnlocked = msg.unlocked;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}