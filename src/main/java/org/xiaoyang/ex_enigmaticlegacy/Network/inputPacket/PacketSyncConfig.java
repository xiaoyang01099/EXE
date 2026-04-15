package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.SpawnControlConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class PacketSyncConfig {

    private final Set<String> disabledEntities;

    public PacketSyncConfig(Set<String> disabledEntities) {
        this.disabledEntities = disabledEntities;
    }

    public static void encode(PacketSyncConfig pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.disabledEntities.size());
        for (String s : pkt.disabledEntities) {
            buf.writeUtf(s);
        }
    }

    public static PacketSyncConfig decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Set<String> set = new HashSet<>();
        for (int i = 0; i < size; i++) {
            set.add(buf.readUtf());
        }
        return new PacketSyncConfig(set);
    }

    public static void handle(PacketSyncConfig pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {

                SpawnControlConfig.syncFromServer(pkt.disabledEntities);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}