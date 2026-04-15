package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Tile.TileMagicTable;

import java.util.function.Supplier;

public class MagicTableGearPacket {
    private final BlockPos pos;
    private final boolean forward;

    public MagicTableGearPacket(BlockPos pos, boolean forward) {
        this.pos = pos;
        this.forward = forward;
    }

    public static void encode(MagicTableGearPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeBoolean(pkt.forward);
    }

    public static MagicTableGearPacket decode(FriendlyByteBuf buf) {
        return new MagicTableGearPacket(buf.readBlockPos(), buf.readBoolean());
    }

    public static void handle(MagicTableGearPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            BlockEntity be = player.level.getBlockEntity(pkt.pos);
            if (be instanceof TileMagicTable tile) {
                if (pkt.forward) {
                    tile.cycleGearForward();
                } else {
                    tile.cycleGearBackward();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}