package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerMotionUpdateMessage {
    private double x;
    private double y;
    private double z;

    public PlayerMotionUpdateMessage() {
    }

    public PlayerMotionUpdateMessage(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void encode(PlayerMotionUpdateMessage message, FriendlyByteBuf buf) {
        buf.writeDouble(message.x);
        buf.writeDouble(message.y);
        buf.writeDouble(message.z);
    }

    public static PlayerMotionUpdateMessage decode(FriendlyByteBuf buf) {
        return new PlayerMotionUpdateMessage(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()
        );
    }

    public static void handle(PlayerMotionUpdateMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            handleClientSide(message);
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClientSide(PlayerMotionUpdateMessage message) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.setDeltaMovement(message.x, message.y, message.z);
            player.hurtMarked = true;
        }
    }
}