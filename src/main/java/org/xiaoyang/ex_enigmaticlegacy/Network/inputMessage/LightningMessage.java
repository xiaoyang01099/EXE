package org.xiaoyang.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import vazkii.botania.common.proxy.Proxy;

import java.util.function.Supplier;

public class LightningMessage {
    private double x;
    private double y;
    private double z;
    private double destx;
    private double desty;
    private double destz;
    private int duration;
    private float curve;
    private int speed;
    private int type;
    private float width;

    public LightningMessage() {
    }

    public LightningMessage(double x, double y, double z, double destx, double desty, double destz, int duration, float curve, int speed, int type, float width) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.destx = destx;
        this.desty = desty;
        this.destz = destz;
        this.duration = duration;
        this.curve = curve;
        this.speed = speed;
        this.type = type;
        this.width = width;
    }

    public static void encode(LightningMessage message, FriendlyByteBuf buf) {
        buf.writeDouble(message.x);
        buf.writeDouble(message.y);
        buf.writeDouble(message.z);
        buf.writeDouble(message.destx);
        buf.writeDouble(message.desty);
        buf.writeDouble(message.destz);
        buf.writeInt(message.duration);
        buf.writeFloat(message.curve);
        buf.writeInt(message.speed);
        buf.writeInt(message.type);
        buf.writeFloat(message.width);
    }

    public static LightningMessage decode(FriendlyByteBuf buf) {
        return new LightningMessage(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readInt(),
                buf.readFloat(),
                buf.readInt(),
                buf.readInt(),
                buf.readFloat()
        );
    }

    public static void handle(LightningMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            handleClientSide(message);
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClientSide(LightningMessage message) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            Level level = player.level();

            Vec3 vectorStart = new Vec3(message.x, message.y, message.z);
            Vec3 vectorEnd = new Vec3(message.destx, message.desty, message.destz);

            int colorOuter, colorInner;
            switch (message.type) {
                case 0:
                    colorOuter = 0x4169E1;
                    colorInner = 0xFFFFFF;
                    break;
                case 1:
                    colorOuter = 0x8A2BE2;
                    colorInner = 0xE6E6FA;
                    break;
                case 2:
                    colorOuter = 0xFF0000;
                    colorInner = 0xFFAAAA;
                    break;
                default:
                    colorOuter = 0x4169E1;
                    colorInner = 0xFFFFFF;
                    break;
            }

            float ticksPerMeter = Math.max(1.0F, message.speed / 6.0F);

            int boltCount = Math.max(1, (int) (message.width * 2));
            for (int i = 0; i < boltCount; i++) {

                Proxy.INSTANCE.lightningFX(level, vectorStart, vectorEnd, ticksPerMeter, colorOuter, colorInner);
            }
        }
    }
}