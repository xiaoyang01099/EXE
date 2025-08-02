package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ICanSwingMySwordMessage {
    private boolean swingTheSword;

    public ICanSwingMySwordMessage() {
    }

    public ICanSwingMySwordMessage(boolean swingTheSword) {
        this.swingTheSword = swingTheSword;
    }

    public static void encode(ICanSwingMySwordMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.swingTheSword);
    }

    public static ICanSwingMySwordMessage decode(FriendlyByteBuf buffer) {
        return new ICanSwingMySwordMessage(buffer.readBoolean());
    }

    public static void handle(ICanSwingMySwordMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            handleMessage(message, context);
        });

        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleMessage(ICanSwingMySwordMessage message, NetworkEvent.Context context) {
        Player player = Minecraft.getInstance().player;

        if (player != null && message.swingTheSword) {
             player.swing(InteractionHand.MAIN_HAND, true);
        }
    }
}