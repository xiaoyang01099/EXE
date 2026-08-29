package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Util.ServerExcaliburChargeTracker;

import java.util.function.Supplier;

// ExcaliburChargeStopC2SPacket 通知服务端玩家松开 C 键未满蓄力或取消咖喱棒蓄力。
public class ExcaliburChargeStopC2SPacket {
    public ExcaliburChargeStopC2SPacket() {
    }

    public ExcaliburChargeStopC2SPacket(FriendlyByteBuf buffer) {
    }

    // 空包编码，服务端按发送者清理状态。
    public void encode(FriendlyByteBuf buffer) {
    }

    // 服务端幂等清理玩家咖喱棒蓄力状态。
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) ServerExcaliburChargeTracker.stop(player);
        });
        context.setPacketHandled(true);
    }
}
