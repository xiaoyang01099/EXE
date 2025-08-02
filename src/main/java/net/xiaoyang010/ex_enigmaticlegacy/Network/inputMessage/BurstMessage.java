package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import vazkii.botania.api.BotaniaAPI;

import java.util.Random;
import java.util.function.Supplier;

public class BurstMessage {
    private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0F) / 2.0F);

    private final double x;
    private final double y;
    private final double z;
    private final float size;

    public BurstMessage(double x, double y, double z, float size) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.size = size;
    }

    public BurstMessage(FriendlyByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.size = buf.readFloat();
    }

    public static void encode(BurstMessage message, FriendlyByteBuf buf) {
        buf.writeDouble(message.x);
        buf.writeDouble(message.y);
        buf.writeDouble(message.z);
        buf.writeFloat(message.size);
    }

    public static BurstMessage decode(FriendlyByteBuf buf) {
        return new BurstMessage(buf);
    }

    public static void handle(BurstMessage message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                createThaumcraftStyleBurst(level, message.x, message.y, message.z, message.size);
            }
        });
        ctx.setPacketHandled(true);
    }

    private static void createThaumcraftStyleBurst(Level level, double x, double y, double z, float size) {
        BotaniaAPI api = BotaniaAPI.instance();
        Random random = new Random(System.currentTimeMillis());

        api.sparkleFX(level, x, y, z,
                1.0f, 1.0f, 1.0f,
                size * 1.2f,
                31);

        api.sparkleFX(level, x, y, z,
                0.0f, 0.8f + random.nextFloat() * 0.2f, 0.4f + random.nextFloat() * 0.6f,
                size * 0.9f,
                28);

        for (int ring = 1; ring <= 3; ring++) {
            int particleCount = 8 * ring;
            double ringRadius = size * 0.25 * ring;

            for (int i = 0; i < particleCount; i++) {
                double angle = i * Math.PI * 2 / particleCount;
                double offsetX = Math.cos(angle) * ringRadius;
                double offsetZ = Math.sin(angle) * ringRadius;

                float whiteIntensity = 1.0f - (ring - 1) * 0.3f;
                float blueIntensity = 0.4f + ring * 0.2f;

                api.sparkleFX(level,
                        x + offsetX, y, z + offsetZ,
                        whiteIntensity, whiteIntensity * 0.9f, blueIntensity,
                        size * (0.8f - ring * 0.15f),
                        31 - ring * 3);
            }
        }

        for (int layer = 1; layer <= 4; layer++) {
            float heightOffset = size * 0.3f * layer;
            float layerSize = size * (0.7f - layer * 0.1f);

            api.sparkleFX(level, x, y + heightOffset, z,
                    1.0f, 0.9f, 0.6f,
                    layerSize, 25 - layer * 2);

            api.sparkleFX(level, x, y - heightOffset, z,
                    1.0f, 0.9f, 0.6f,
                    layerSize, 25 - layer * 2);
        }

        int burstCount = (int)(size * size * 40);
        for (int i = 0; i < burstCount; i++) {
            double theta = random.nextDouble() * Math.PI * 2;
            double phi = Math.acos(2 * random.nextDouble() - 1);
            double radius = random.nextDouble() * size * 2.5;

            double offsetX = radius * Math.sin(phi) * Math.cos(theta);
            double offsetY = radius * Math.cos(phi);
            double offsetZ = radius * Math.sin(phi) * Math.sin(theta);

            float distance = (float)radius / (size * 2.5f);
            float brightness = 1.0f - distance * 0.7f;

            api.sparkleFX(level,
                    x + offsetX, y + offsetY, z + offsetZ,
                    brightness,
                    brightness * (0.8f + 0.2f * distance),
                    0.4f + distance * 0.6f,
                    0.15f + random.nextFloat() * 0.1f,
                    15 + random.nextInt(10));
        }

        for (int spiral = 0; spiral < 2; spiral++) {
            for (int i = 0; i < 20; i++) {
                double angle = i * Math.PI * 0.5 + spiral * Math.PI;
                double spiralRadius = size * 0.4 * (1.0 - i / 20.0);
                double height = size * (i / 10.0 - 1.0);

                double offsetX = Math.cos(angle) * spiralRadius;
                double offsetZ = Math.sin(angle) * spiralRadius;

                api.sparkleFX(level,
                        x + offsetX, y + height, z + offsetZ,
                        0.8f, 0.9f, 1.0f,
                        0.2f, 20);
            }
        }
    }
}