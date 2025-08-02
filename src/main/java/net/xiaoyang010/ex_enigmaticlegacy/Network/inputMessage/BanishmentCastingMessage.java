package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import vazkii.botania.client.fx.WispParticleData;

import java.util.function.Supplier;

public class BanishmentCastingMessage {
    private double x;
    private double y;
    private double z;
    private int amount;

    public BanishmentCastingMessage() {
    }

    public BanishmentCastingMessage(double x, double y, double z, int amount) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.amount = amount;
    }

    public static void encode(BanishmentCastingMessage message, FriendlyByteBuf buf) {
        buf.writeDouble(message.x);
        buf.writeDouble(message.y);
        buf.writeDouble(message.z);
        buf.writeInt(message.amount);
    }

    public static BanishmentCastingMessage decode(FriendlyByteBuf buf) {
        return new BanishmentCastingMessage(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readInt()
        );
    }

    public static void handle(BanishmentCastingMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            handleClientSide(message);
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClientSide(BanishmentCastingMessage message) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        Level world = player.level;
        Vector3 thisPos = new Vector3(message.x, message.y, message.z);

        for (int counter = 0; counter < message.amount; ++counter) {
            double calculatedPositionX = message.x + (Math.random() - 0.5) * 8.0;
            double calculatedPositionY = message.y + (Math.random() - 0.5) * 8.0;
            double calculatedPositionZ = message.z + (Math.random() - 0.5) * 8.0;

            Vector3 targetPos = new Vector3(calculatedPositionX, calculatedPositionY, calculatedPositionZ);
            Vector3 diff = thisPos.copy().sub(targetPos);
            diff.multiply(0.08);

            double calculatedMotionX = diff.x;
            double calculatedMotionY = diff.y;
            double calculatedMotionZ = diff.z;

            float r = 0.9F + (float) Math.random() * 0.1F;
            float g = 0.1F + (float) Math.random() * 0.15F;
            float b = 0.0F;
            float s = 0.2F + (float) Math.random() * 0.2F;

            WispParticleData particleData = WispParticleData.wisp(s, r, g, b, 0.5F, false);
            world.addParticle(particleData,
                    calculatedPositionX, calculatedPositionY, calculatedPositionZ,
                    calculatedMotionX, calculatedMotionY, calculatedMotionZ);
        }
    }
}