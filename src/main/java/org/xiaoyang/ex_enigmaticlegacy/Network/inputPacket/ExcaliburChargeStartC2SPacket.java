package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Util.ServerExcaliburChargeTracker;

import java.util.function.Supplier;

// ExcaliburChargeStartC2SPacket 通知服务端玩家按下 C 键并开始咖喱棒蓄力。
public class ExcaliburChargeStartC2SPacket {
    public ExcaliburChargeStartC2SPacket() {
    }

    public ExcaliburChargeStartC2SPacket(FriendlyByteBuf buffer) {
    }

    // 空包编码，玩家和蓄力参数全部由服务端读取。
    public void encode(FriendlyByteBuf buffer) {
    }

    // 服务端验证开始条件并登记可信蓄力时间。
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) ServerExcaliburChargeTracker.start(player);
        });
        context.setPacketHandled(true);
    }
}
