package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import vazkii.botania.api.BotaniaAPI;

import java.util.function.Supplier;

public class LunarFlaresParticleMessage {
    private final double x;
    private final double y;
    private final double z;
    private final int quantity;

    public LunarFlaresParticleMessage(double x, double y, double z, int quantity) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.quantity = quantity;
    }

    public static void encode(LunarFlaresParticleMessage message, FriendlyByteBuf buf) {
        buf.writeDouble(message.x);
        buf.writeDouble(message.y);
        buf.writeDouble(message.z);
        buf.writeInt(message.quantity);
    }

    public static LunarFlaresParticleMessage decode(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        int quantity = buf.readInt();
        return new LunarFlaresParticleMessage(x, y, z, quantity);
    }

    public static void handle(LunarFlaresParticleMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> {
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    for (int i = 0; i <= message.quantity; i++) {
                        float r = 0.0F;
                        float g = 0.8F + (float) Math.random() * 0.2F;
                        float b = 0.4F + (float) Math.random() * 0.6F;
                        float size = 0.3F + (float) Math.random() * 0.3F;
                        int motionMultiplier = 20;

                        BotaniaAPI.instance().sparkleFX(
                                player.level,
                                message.x,
                                message.y,
                                message.z,
                                r, g, b,
                                size,
                                motionMultiplier
                        );
                    }
                }
            });
        }
        context.setPacketHandled(true);
    }
}