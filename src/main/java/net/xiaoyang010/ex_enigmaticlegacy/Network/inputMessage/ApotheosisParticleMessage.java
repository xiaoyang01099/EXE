package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import vazkii.botania.api.BotaniaAPI;

import java.util.function.Supplier;

public class ApotheosisParticleMessage {
    private final double x;
    private final double y;
    private final double z;
    private final int quantity;

    public ApotheosisParticleMessage(double x, double y, double z, int quantity) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.quantity = quantity;
    }

    public static void encode(ApotheosisParticleMessage message, FriendlyByteBuf buf) {
        buf.writeDouble(message.x);
        buf.writeDouble(message.y);
        buf.writeDouble(message.z);
        buf.writeInt(message.quantity);
    }

    public static ApotheosisParticleMessage decode(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        int quantity = buf.readInt();
        return new ApotheosisParticleMessage(x, y, z, quantity);
    }

    public static void handle(ApotheosisParticleMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> {
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    for (int i = 0; i < message.quantity; i++) {
                        float r = 0.8F + (float) Math.random() * 0.2F;
                        float g = 0.8F + (float) Math.random() * 0.2F;
                        float b = 0.0F;

                        float size = 0.3F + (float) Math.random() * 0.3F;

                        float motionRange = 0.25F;
                        double motionX = ((Math.random() - 0.5) * motionRange);
                        double motionY = ((Math.random() - 0.5) * motionRange);
                        double motionZ = ((Math.random() - 0.5) * motionRange);

                        double offsetX = (Math.random() - 0.5) * 0.5;
                        double offsetY = (Math.random() - 0.5) * 0.5;
                        double offsetZ = (Math.random() - 0.5) * 0.5;

                        BotaniaAPI.instance().sparkleFX(
                                player.level,
                                message.x + offsetX,
                                message.y + offsetY,
                                message.z + offsetZ,
                                r, g, b,
                                size,
                                1
                        );

                        if (i % 3 == 0) {
                            BotaniaAPI.instance().sparkleFX(
                                    player.level,
                                    message.x + offsetX * 2,
                                    message.y + offsetY * 2,
                                    message.z + offsetZ * 2,
                                    1.0F,
                                    1.0F,
                                    0.8F,
                                    size * 0.7F,
                                    2
                            );
                        }
                    }
                }
            });
        }
        context.setPacketHandled(true);
    }
}