package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Util.ServerSkillCooldowns;
import org.xiaoyang.ex_enigmaticlegacy.Item.FlySwordPlusItem;

import java.util.function.Supplier;

// BattoSlashCastC2SPacket 用于客户端右键蓄力完成后请求服务端释放拔刀斩。
public class BattoSlashCastC2SPacket {
    public BattoSlashCastC2SPacket() {
    }

    public BattoSlashCastC2SPacket(FriendlyByteBuf buffer) {
    }

    // 空包编码，释放条件全部交给服务端校验。
    public void encode(FriendlyByteBuf buffer) {
    }

    // 服务端处理真·飞剑右键拔刀斩请求。
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (ServerSkillCooldowns.isCoolingDown(player, ServerSkillCooldowns.BATTO_SLASH)) return;
            if (FlySwordPlusItem.trySpawnBattoSlash(player)) {
                ServerSkillCooldowns.setCooldown(
                        player,
                        ServerSkillCooldowns.BATTO_SLASH,
                        ServerSkillCooldowns.serverCooldownTicks(ServerSkillCooldowns.BATTO_SLASH_CLIENT_COOLDOWN_TICKS)
                );
            }
        });
        context.setPacketHandled(true);
    }
}
