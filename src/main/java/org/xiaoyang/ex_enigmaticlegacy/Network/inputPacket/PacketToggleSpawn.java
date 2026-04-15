package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;
import org.xiaoyang.ex_enigmaticlegacy.SpawnControlConfig;

import java.util.function.Supplier;

public class PacketToggleSpawn {

    private final ResourceLocation entityId;
    private final boolean wantDisabled;

    public PacketToggleSpawn(ResourceLocation entityId, boolean wantDisabled) {
        this.entityId = entityId;
        this.wantDisabled = wantDisabled;
    }

    public static void encode(PacketToggleSpawn pkt, FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.entityId);
        buf.writeBoolean(pkt.wantDisabled);
    }

    public static PacketToggleSpawn decode(FriendlyByteBuf buf) {
        return new PacketToggleSpawn(buf.readResourceLocation(), buf.readBoolean());
    }

    public static void handle(PacketToggleSpawn pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) return;

            if (pkt.wantDisabled) {
                SpawnControlConfig.disableEntity(pkt.entityId);
            } else {
                SpawnControlConfig.enableEntity(pkt.entityId);
            }

            PacketSyncConfig syncPkt = new PacketSyncConfig(SpawnControlConfig.getDisabledEntities());
            NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), syncPkt);
        });
        ctx.get().setPacketHandled(true);
    }
}