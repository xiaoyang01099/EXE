package net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Event.SpectatorModeHandler;

import java.util.function.Supplier;

public class SpectatorModePacket {

    public SpectatorModePacket() {
    }

    public static void encode(SpectatorModePacket packet, FriendlyByteBuf buffer) {
    }

    public static SpectatorModePacket decode(FriendlyByteBuf buffer) {
        return new SpectatorModePacket();
    }

    public static void handle(SpectatorModePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                SpectatorModeHandler.toggleSpectatorMode(player);
            }
        });
        context.setPacketHandled(true);
    }
}