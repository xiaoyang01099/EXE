package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.TelekinesisTomeLevel;

import java.util.function.Supplier;

public class TelekinesisTomeLevelAttackMessage {

    private final boolean attack;

    public TelekinesisTomeLevelAttackMessage(boolean attack) {
        this.attack = attack;
    }

    public static void encode(TelekinesisTomeLevelAttackMessage msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.attack);
    }

    public static TelekinesisTomeLevelAttackMessage decode(FriendlyByteBuf buf) {
        return new TelekinesisTomeLevelAttackMessage(buf.readBoolean());
    }

    public static void handle(TelekinesisTomeLevelAttackMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack held = player.getMainHandItem();
            if (held.getItem() instanceof TelekinesisTomeLevel tome && msg.attack) {
                tome.performAttack(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
