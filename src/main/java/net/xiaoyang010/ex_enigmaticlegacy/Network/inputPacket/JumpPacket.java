package net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.WildHuntArmor;

import java.util.function.Supplier;

public class JumpPacket {

    public JumpPacket() {
    }

    public JumpPacket(FriendlyByteBuf buf) {
    }

    // 编码到网络缓冲区
    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                WildHuntArmor.performDoubleJump(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}