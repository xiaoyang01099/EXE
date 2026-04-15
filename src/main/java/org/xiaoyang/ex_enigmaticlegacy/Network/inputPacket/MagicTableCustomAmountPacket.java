package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Tile.TileMagicTable;

import java.util.function.Supplier;

public class MagicTableCustomAmountPacket {
    private final BlockPos pos;
    private final long amount;

    public MagicTableCustomAmountPacket(BlockPos pos, long amount) {
        this.pos = pos;
        this.amount = amount;
    }

    public static void encode(MagicTableCustomAmountPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeLong(pkt.amount);
    }

    public static MagicTableCustomAmountPacket decode(FriendlyByteBuf buf) {
        return new MagicTableCustomAmountPacket(buf.readBlockPos(), buf.readLong());
    }

    public static void handle(MagicTableCustomAmountPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            BlockEntity be = player.level.getBlockEntity(pkt.pos);
            if (be instanceof TileMagicTable tile) {
                tile.setCustomConvertAmount(pkt.amount);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}