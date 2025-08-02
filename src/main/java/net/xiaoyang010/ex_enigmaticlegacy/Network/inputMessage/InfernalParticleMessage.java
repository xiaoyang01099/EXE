package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import vazkii.botania.client.fx.WispParticleData;

import java.util.function.Supplier;

public class InfernalParticleMessage {
    private double x;
    private double y;
    private double z;
    private int amount;

    public InfernalParticleMessage() {
    }

    public InfernalParticleMessage(double x, double y, double z, int amount) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.amount = amount;
    }

    public static void encode(InfernalParticleMessage message, FriendlyByteBuf buf) {
        buf.writeDouble(message.x);
        buf.writeDouble(message.y);
        buf.writeDouble(message.z);
        buf.writeInt(message.amount);
    }

    public static InfernalParticleMessage decode(FriendlyByteBuf buf) {
        return new InfernalParticleMessage(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readInt()
        );
    }

    public static void handle(InfernalParticleMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            handleClientSide(message);
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClientSide(InfernalParticleMessage message) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        Level world = player.level;

        for (int i = 0; i <= message.amount; ++i) {
            float r = 0.9F + (float) Math.random() * 0.1F;
            float g = 0.1F + (float) Math.random() * 0.15F;
            float b = 0.0F;
            float s = 0.4F + (float) Math.random() * 0.4F;
            float m = 0.5F;

            double xm = ((double) Math.random() - 0.5) * m;
            double ym = ((double) Math.random() - 0.5) * m;
            double zm = ((double) Math.random() - 0.5) * m;

            WispParticleData particleData = WispParticleData.wisp(s, r, g, b, 1.0F, false);
            world.addParticle(particleData,
                    message.x, message.y, message.z,
                    xm, ym, zm);
        }
    }
}