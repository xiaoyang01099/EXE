package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Tile.TileMagicTable;

import java.math.BigInteger;
import java.util.function.Supplier;

public class MagicTableConvertPacket {
    private final BlockPos pos;

    public MagicTableConvertPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(MagicTableConvertPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
    }

    public static MagicTableConvertPacket decode(FriendlyByteBuf buf) {
        return new MagicTableConvertPacket(buf.readBlockPos());
    }

    public static void handle(MagicTableConvertPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            BlockEntity be = player.level.getBlockEntity(pkt.pos);
            if (be instanceof TileMagicTable tile) {
                BigInteger earned = tile.convertOutputToEmc(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}