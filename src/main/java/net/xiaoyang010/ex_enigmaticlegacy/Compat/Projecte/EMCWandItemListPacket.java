package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EMCWandItemListPacket {
    private final List<ItemEmcData> items;
    private final boolean isAllMode;
    private final boolean isAppend;
    private final boolean isFinal;

    public EMCWandItemListPacket(List<ItemEmcData> items, boolean isAllMode, boolean isAppend, boolean isFinal) {
        this.items = items;
        this.isAllMode = isAllMode;
        this.isAppend = isAppend;
        this.isFinal = isFinal;
    }

    public static void encode(EMCWandItemListPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.isAllMode);
        buf.writeBoolean(msg.isAppend);
        buf.writeBoolean(msg.isFinal);
        buf.writeVarInt(msg.items.size());
        for (ItemEmcData data : msg.items) {
            buf.writeUtf(data.itemId, 256);
            buf.writeLong(data.emcValue);
        }
    }

    public static EMCWandItemListPacket decode(FriendlyByteBuf buf) {
        boolean isAllMode = buf.readBoolean();
        boolean isAppend = buf.readBoolean();
        boolean isFinal = buf.readBoolean();
        int size = buf.readVarInt();
        List<ItemEmcData> items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            items.add(new ItemEmcData(buf.readUtf(256), buf.readLong()));
        }
        return new EMCWandItemListPacket(items, isAllMode, isAppend, isFinal);
    }

    public static void handle(EMCWandItemListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        EMCWandScreen.handleItemListPacket(msg.items, msg.isAllMode, msg.isAppend, msg.isFinal)));
        ctx.get().setPacketHandled(true);
    }

    public record ItemEmcData(String itemId, long emcValue) {}
}