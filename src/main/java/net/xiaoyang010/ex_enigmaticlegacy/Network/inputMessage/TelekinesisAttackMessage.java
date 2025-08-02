package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.TelekinesisTome;

import java.util.function.Supplier;

public class TelekinesisAttackMessage {
    private boolean doIt;

    public TelekinesisAttackMessage() {
    }

    public TelekinesisAttackMessage(boolean doIt) {
        this.doIt = doIt;
    }

    public static void encode(TelekinesisAttackMessage message, FriendlyByteBuf buf) {
        buf.writeBoolean(message.doIt);
    }

    public static TelekinesisAttackMessage decode(FriendlyByteBuf buf) {
        return new TelekinesisAttackMessage(buf.readBoolean());
    }

    public static void handle(TelekinesisAttackMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ItemStack stack = player.getMainHandItem();
                if (!stack.isEmpty() && message.doIt) {
                    if (stack.getItem() == ModItems.TELEKINESIS_TOME.get()) {
                        TelekinesisTome.leftClick(player);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}