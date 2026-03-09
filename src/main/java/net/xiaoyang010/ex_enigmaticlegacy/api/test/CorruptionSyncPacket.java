package net.xiaoyang010.ex_enigmaticlegacy.api.test;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CorruptionSyncPacket {
    private final boolean isBatch;
    private final BlockPos pos;
    private final int corruption;
    private final Map<BlockPos, Integer> batchData;

    public CorruptionSyncPacket(BlockPos pos, int corruption) {
        this.isBatch = false;
        this.pos = pos;
        this.corruption = corruption;
        this.batchData = null;
    }

    public CorruptionSyncPacket(Map<BlockPos, Integer> batchData) {
        this.isBatch = true;
        this.pos = null;
        this.corruption = 0;
        this.batchData = batchData;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(isBatch);
        if (isBatch) {
            buf.writeInt(batchData.size());
            batchData.forEach((p, c) -> {
                buf.writeBlockPos(p);
                buf.writeVarInt(c);
            });
        } else {
            buf.writeBlockPos(pos);
            buf.writeVarInt(corruption);
        }
    }

    public static CorruptionSyncPacket decode(FriendlyByteBuf buf) {
        boolean isBatch = buf.readBoolean();
        if (isBatch) {
            int size = buf.readInt();
            Map<BlockPos, Integer> data = new HashMap<>();
            for (int i = 0; i < size; i++) {
                BlockPos p = buf.readBlockPos();
                int c = buf.readVarInt();
                data.put(p, c);
            }
            return new CorruptionSyncPacket(data);
        } else {
            BlockPos p = buf.readBlockPos();
            int c = buf.readVarInt();
            return new CorruptionSyncPacket(p, c);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (isBatch) {
                ClientCorruptionCache.clearAll();
                ClientCorruptionCache.updateBatch(batchData);
            } else {
                ClientCorruptionCache.updateCorruption(pos, corruption);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}