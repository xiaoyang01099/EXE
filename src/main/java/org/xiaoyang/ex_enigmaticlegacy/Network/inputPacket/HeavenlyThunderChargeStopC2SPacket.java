package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Util.ServerKeyChargeTracker;

import java.util.function.Supplier;

// HeavenlyThunderChargeStopC2SPacket 通知服务端玩家松开 V 键或取消天雷蓄力。
public class HeavenlyThunderChargeStopC2SPacket {
    public HeavenlyThunderChargeStopC2SPacket() {
    }

    public HeavenlyThunderChargeStopC2SPacket(FriendlyByteBuf buffer) {
    }

    // 空包编码，服务端按发送者清理状态。
    public void encode(FriendlyByteBuf buffer) {
    }

    // 服务端幂等清理玩家天雷蓄力状态。
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) ServerKeyChargeTracker.stopHeavenlyThunder(player);
        });
        context.setPacketHandled(true);
    }
}
