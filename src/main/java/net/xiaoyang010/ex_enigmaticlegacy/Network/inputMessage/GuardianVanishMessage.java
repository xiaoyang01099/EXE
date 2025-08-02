package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GuardianVanishMessage {

    public GuardianVanishMessage() {
    }

    public static void encode(GuardianVanishMessage msg, FriendlyByteBuf buf) {
    }

    public static GuardianVanishMessage decode(FriendlyByteBuf buf) {
        return new GuardianVanishMessage();
    }

    public static void handle(GuardianVanishMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                handleClientSide(message);
            }
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClientSide(GuardianVanishMessage message) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            TranslatableComponent chatMessage = new TranslatableComponent("message.guardian_vanish");
            chatMessage.withStyle(ChatFormatting.DARK_PURPLE);

            player.sendMessage(chatMessage, player.getUUID());
        }
    }
}