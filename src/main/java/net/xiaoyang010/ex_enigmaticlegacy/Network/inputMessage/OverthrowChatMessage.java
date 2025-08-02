package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OverthrowChatMessage {
    private String overthrownPlayer;
    private String overthrower;
    private int type;

    public OverthrowChatMessage() {
    }

    public OverthrowChatMessage(String overthrownPlayer, String overthrower, int type) {
        this.overthrownPlayer = overthrownPlayer;
        this.overthrower = overthrower;
        this.type = type;
    }

    public static void encode(OverthrowChatMessage message, FriendlyByteBuf buf) {
        buf.writeUtf(message.overthrownPlayer);
        buf.writeUtf(message.overthrower);
        buf.writeInt(message.type);
    }

    public static OverthrowChatMessage decode(FriendlyByteBuf buf) {
        String overthrownPlayer = buf.readUtf();
        String overthrower = buf.readUtf();
        int type = buf.readInt();
        return new OverthrowChatMessage(overthrownPlayer, overthrower, type);
    }

    public static void handle(OverthrowChatMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                message.handleClientSide();
            }
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClientSide() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            Component message;

            if (this.type == 0) {
                message = new TextComponent(this.overthrower + " ")
                        .append(new TranslatableComponent("message.overthrown1"))
                        .append(new TextComponent(" " + this.overthrownPlayer + " "))
                        .append(new TranslatableComponent("message.overthrown2"));
            } else if (this.type == 1) {
                message = new TextComponent(this.overthrower + " ")
                        .append(new TranslatableComponent("message.overthrown1"))
                        .append(new TextComponent(" " + this.overthrownPlayer + " "))
                        .append(new TranslatableComponent("message.overthrown3"));
            } else {
                message = new TextComponent("Unknown overthrow message type: " + this.type);
            }

            player.sendMessage(message, player.getUUID());
        }
    }
}