package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;
import org.xiaoyang.ex_enigmaticlegacy.Tile.SpectriteChestTile;

import java.util.function.Supplier;

public class ScrollPagePacket {

    private final BlockPos pos;
    private final int delta;

    public ScrollPagePacket(BlockPos pos, int delta) {
        this.pos = pos;
        this.delta = delta;
    }

    public static void encode(ScrollPagePacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeInt(msg.delta);
    }

    public static ScrollPagePacket decode(FriendlyByteBuf buf) {
        return new ScrollPagePacket(buf.readBlockPos(), buf.readInt());
    }

    public static void handle(ScrollPagePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (!(be instanceof SpectriteChestTile tile)) return;
            if (!tile.stillValid(player)) return;
            tile.scrollPage(msg.delta);

            NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncPagePacket(msg.pos, tile.getCurrentPage())
            );

            player.containerMenu.broadcastChanges();
        });
        ctx.get().setPacketHandled(true);
    }
}