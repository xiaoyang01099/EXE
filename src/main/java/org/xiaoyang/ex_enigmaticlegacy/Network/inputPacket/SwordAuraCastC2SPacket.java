package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Item.FlySwordItem;

import java.util.function.Supplier;

// SwordAuraCastC2SPacket 用于客户端左键空挥时请求服务端释放飞剑剑气。
public class SwordAuraCastC2SPacket {
    public SwordAuraCastC2SPacket() {
    }

    public SwordAuraCastC2SPacket(FriendlyByteBuf buffer) {
    }

    // 编码空包体。
    public void encode(FriendlyByteBuf buffer) {
    }

    // 处理客户端请求，服务端重新校验玩家手持飞剑。
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            FlySwordItem.trySpawnSwordAura(player);
        });
        context.setPacketHandled(true);
    }
}
