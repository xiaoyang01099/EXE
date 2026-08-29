package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Util.ServerSkillCooldowns;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigFile;
import org.xiaoyang.ex_enigmaticlegacy.Item.FlySwordItem;

import java.util.function.Supplier;

// DimensionSlashCastC2SPacket 用于客户端按 V 请求服务端释放飞剑次元斩。
public class DimensionSlashCastC2SPacket {
    public DimensionSlashCastC2SPacket() {
    }

    public DimensionSlashCastC2SPacket(FriendlyByteBuf buffer) {
    }

    // 空包编码，释放条件全部交给服务端校验。
    public void encode(FriendlyByteBuf buffer) {
    }

    // 服务端处理 V 键释放请求。
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (ServerSkillCooldowns.isCoolingDown(player, ServerSkillCooldowns.DIMENSION_SLASH)) return;
            if (FlySwordItem.trySpawnDimensionSlash(player)) {
                ServerSkillCooldowns.setCooldown(
                        player,
                        ServerSkillCooldowns.DIMENSION_SLASH,
                        ServerSkillCooldowns.serverCooldownTicks(ConfigFile.flySwordDimensionSlashCooldown())
                );
            }
        });
        context.setPacketHandled(true);
    }
}
