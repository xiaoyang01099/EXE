package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Util.ServerExcaliburChargeTracker;

import java.util.function.Supplier;

// ExcaliburCastC2SPacket 用于客户端松开 C 键后请求服务端释放咖喱棒首版视觉。
public class ExcaliburCastC2SPacket {
    public ExcaliburCastC2SPacket() {
    }

    public ExcaliburCastC2SPacket(FriendlyByteBuf buffer) {
    }

    // 空包编码，释放条件全部交给服务端校验。
    public void encode(FriendlyByteBuf buffer) {
    }

    // 服务端处理咖喱棒释放请求，首版只触发同步实体释放视觉。
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (!ServerExcaliburChargeTracker.release(player)) {
                ServerExcaliburChargeTracker.stop(player);
            }
        });
        context.setPacketHandled(true);
    }
}
