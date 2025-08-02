package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.TelekinesisTome;

import java.util.function.Supplier;

public class TelekinesisUseMessage {

    public TelekinesisUseMessage() {
    }

    public static void encode(TelekinesisUseMessage message, FriendlyByteBuf buf) {
    }

    public static TelekinesisUseMessage decode(FriendlyByteBuf buf) {
        return new TelekinesisUseMessage();
    }

    public static void handle(TelekinesisUseMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ItemStack stack = player.getMainHandItem();
                if (!stack.isEmpty() && stack.getItem() == ModItems.TELEKINESIS_TOME.get()) {
                    TelekinesisTome.onUsingTickAlt(stack, player, 0);
                }
            }
        });
        context.setPacketHandled(true);
    }
}