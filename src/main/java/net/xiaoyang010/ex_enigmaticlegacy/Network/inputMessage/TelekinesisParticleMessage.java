package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Network.ClientProxy;
import vazkii.botania.api.BotaniaAPI;

import java.util.function.Supplier;

public class TelekinesisParticleMessage {
    private double x;
    private double y;
    private double z;
    private float modifier;

    public TelekinesisParticleMessage() {
    }

    public TelekinesisParticleMessage(double x, double y, double z, float modifier) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.modifier = modifier;
    }

    public static void encode(TelekinesisParticleMessage message, FriendlyByteBuf buf) {
        buf.writeDouble(message.x);
        buf.writeDouble(message.y);
        buf.writeDouble(message.z);
        buf.writeFloat(message.modifier);
    }

    public static TelekinesisParticleMessage decode(FriendlyByteBuf buf) {
        return new TelekinesisParticleMessage(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readFloat()
        );
    }

    public static void handle(TelekinesisParticleMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            handleClientSide(message);
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClientSide(TelekinesisParticleMessage message) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            int wisps = (int)(1.0F * message.modifier);
            int supers = (int)(3.0F * message.modifier);

            for (int i = 0; i <= wisps; i++) {
                float r = 0.2F + (float)Math.random() * 0.3F;
                float g = 0.0F;
                float b = 0.5F + (float)Math.random() * 0.2F;
                float s = 0.2F + (float)Math.random() * 0.1F;

                BotaniaAPI.instance().sparkleFX(player.level, message.x, message.y, message.z, r, g, b, s, 10);
            }

            ClientProxy proxy = new ClientProxy();
            for (int counter = 0; counter <= supers; counter++) {
                proxy.spawnSuperParticle(
                        player.level,
                        "portalstuff",
                        message.x, message.y, message.z,
                        (Math.random() - 0.5) * 3.0,
                        (Math.random() - 0.5) * 3.0,
                        (Math.random() - 0.5) * 3.0,
                        1.0F,
                        64.0
                );
            }
        }
    }
}