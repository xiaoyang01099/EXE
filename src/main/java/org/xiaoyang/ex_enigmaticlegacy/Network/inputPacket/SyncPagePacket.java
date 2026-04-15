package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Client.screen.SpectriteChestScreen;
import org.xiaoyang.ex_enigmaticlegacy.Tile.SpectriteChestTile;

import java.util.function.Supplier;

public class SyncPagePacket {

    private final BlockPos pos;
    private final int page;

    public SyncPagePacket(BlockPos pos, int page) {
        this.pos = pos;
        this.page = page;
    }

    public static void encode(SyncPagePacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeInt(msg.page);
    }

    public static SyncPagePacket decode(FriendlyByteBuf buf) {
        return new SyncPagePacket(buf.readBlockPos(), buf.readInt());
    }

    public static void handle(SyncPagePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            BlockEntity be = mc.level.getBlockEntity(msg.pos);
            if (be instanceof SpectriteChestTile tile) {
                tile.setCurrentPageClient(msg.page);
            }

            if (mc.screen instanceof SpectriteChestScreen screen) {
                screen.syncPage(msg.page);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}