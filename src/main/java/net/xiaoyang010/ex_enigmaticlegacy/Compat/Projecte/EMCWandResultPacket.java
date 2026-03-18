package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EMCWandResultPacket {
    private final String itemId;
    private final long emcValue;
    private final boolean success;

    public EMCWandResultPacket(String itemId, long emcValue, boolean success) {
        this.itemId = itemId;
        this.emcValue = emcValue;
        this.success = success;
    }

    public static void encode(EMCWandResultPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.itemId);
        buf.writeLong(msg.emcValue);
        buf.writeBoolean(msg.success);
    }

    public static EMCWandResultPacket decode(FriendlyByteBuf buf) {
        return new EMCWandResultPacket(buf.readUtf(256), buf.readLong(), buf.readBoolean());
    }

    public static void handle(EMCWandResultPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        EMCWandScreen.handleResultPacket(msg.itemId, msg.emcValue, msg.success)));
        ctx.get().setPacketHandled(true);
    }
}