package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import vazkii.botania.client.fx.BoltParticleOptions;
import vazkii.botania.client.fx.BoltParticleOptions.BoltRenderInfo;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;
import com.mojang.math.Vector4f;

import java.util.Random;
import java.util.function.Supplier;

public class TelekinesisTomeLevelParticleMessage {
    private final double x, y, z;
    private final double endX, endY, endZ;
    private final Type type;
    private final float intensity;
    private final boolean hasEnd;

    public enum Type {
        PASSIVE_AURA,
        TELEKINESIS,
        LIGHTNING,
        AOE_BURST
    }

    public TelekinesisTomeLevelParticleMessage(double x, double y, double z, Type type, float intensity) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.endX = 0;
        this.endY = 0;
        this.endZ = 0;
        this.type = type;
        this.intensity = intensity;
        this.hasEnd = false;
    }

    public TelekinesisTomeLevelParticleMessage(double x, double y, double z, double endX, double endY, double endZ, Type type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
        this.type = type;
        this.intensity = 1.0F;
        this.hasEnd = true;
    }

    public static void encode(TelekinesisTomeLevelParticleMessage msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeDouble(msg.endX);
        buf.writeDouble(msg.endY);
        buf.writeDouble(msg.endZ);
        buf.writeEnum(msg.type);
        buf.writeFloat(msg.intensity);
        buf.writeBoolean(msg.hasEnd);
    }

    public static TelekinesisTomeLevelParticleMessage decode(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        double endX = buf.readDouble();
        double endY = buf.readDouble();
        double endZ = buf.readDouble();
        Type type = buf.readEnum(Type.class);
        float intensity = buf.readFloat();
        boolean hasEnd = buf.readBoolean();

        if (hasEnd) {
            return new TelekinesisTomeLevelParticleMessage(x, y, z, endX, endY, endZ, type);
        } else {
            return new TelekinesisTomeLevelParticleMessage(x, y, z, type, intensity);
        }
    }

    public static void handle(TelekinesisTomeLevelParticleMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleClient(msg);
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(TelekinesisTomeLevelParticleMessage msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Random random = new Random();

        switch (msg.type) {
            case PASSIVE_AURA -> spawnPassiveAura(mc, msg, random);
            case TELEKINESIS -> spawnTelekinesisParticles(mc, msg, random);
            case LIGHTNING -> spawnLightningEffect(mc, msg, random);
            case AOE_BURST -> spawnAOEBurst(mc, msg, random);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void spawnPassiveAura(Minecraft mc, TelekinesisTomeLevelParticleMessage msg, Random random) {
        for (int i = 0; i < 8; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = 1.5 + random.nextDouble() * 0.5;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            double offsetY = random.nextDouble() * 2 - 0.5;

            float r = 0.8F + random.nextFloat() * 0.2F;
            float g = 0.1F + random.nextFloat() * 0.2F;
            float b = 0.3F + random.nextFloat() * 0.3F;

            WispParticleData wisp = WispParticleData.wisp(
                    0.3F + random.nextFloat() * 0.2F,
                    r, g, b,
                    1.0F
            );

            mc.level.addParticle(wisp,
                    msg.x + offsetX,
                    msg.y + offsetY,
                    msg.z + offsetZ,
                    0, 0.02, 0
            );
        }

        for (int i = 0; i < 3; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 3;
            double offsetY = random.nextDouble() * 2;
            double offsetZ = (random.nextDouble() - 0.5) * 3;

            SparkleParticleData sparkle = SparkleParticleData.sparkle(
                    0.5F + random.nextFloat() * 0.3F,
                    1.0F, 0.3F, 0.5F,
                    5
            );

            mc.level.addParticle(sparkle,
                    msg.x + offsetX,
                    msg.y + offsetY,
                    msg.z + offsetZ,
                    0, 0, 0
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void spawnTelekinesisParticles(Minecraft mc, TelekinesisTomeLevelParticleMessage msg, Random random) {
        for (int i = 0; i < 12; i++) {
            double angle = (i / 12.0) * Math.PI * 2;
            double radius = 0.5 + random.nextDouble() * 0.3;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            float r = 0.9F;
            float g = 0.2F + random.nextFloat() * 0.2F;
            float b = 0.6F + random.nextFloat() * 0.2F;

            WispParticleData wisp = WispParticleData.wisp(
                    0.2F + random.nextFloat() * 0.15F,
                    r, g, b,
                    0.5F
            );

            mc.level.addParticle(wisp,
                    msg.x + offsetX,
                    msg.y + (random.nextDouble() - 0.5) * 0.5,
                    msg.z + offsetZ,
                    -offsetX * 0.05, 0.02, -offsetZ * 0.05
            );
        }

        SparkleParticleData sparkle = SparkleParticleData.sparkle(
                0.8F, 1.0F, 0.4F, 0.7F, 3
        );
        mc.level.addParticle(sparkle, msg.x, msg.y, msg.z, 0, 0, 0);
    }

    @OnlyIn(Dist.CLIENT)
    private static void spawnLightningEffect(Minecraft mc, TelekinesisTomeLevelParticleMessage msg, Random random) {
        Vec3 start = new Vec3(msg.x, msg.y, msg.z);
        Vec3 end = new Vec3(msg.endX, msg.endY, msg.endZ);

        BoltRenderInfo renderInfo = BoltRenderInfo.DEFAULT
                .color(new Vector4f(1.0F, 0.3F, 0.5F, 0.9F))
                .noise(0.15F, 0.2F)
                .branching(0.3F, 0.5F);

        BoltParticleOptions bolt = new BoltParticleOptions(renderInfo, start, end)
                .count(3)
                .size(0.08F)
                .lifespan(8);

        mc.level.addParticle((ParticleOptions) bolt, msg.x, msg.y, msg.z, 0, 0, 0);

        spawnBurstAt(mc, msg.x, msg.y, msg.z, random, 8);
        spawnBurstAt(mc, msg.endX, msg.endY, msg.endZ, random, 12);
    }

    @OnlyIn(Dist.CLIENT)
    private static void spawnAOEBurst(Minecraft mc, TelekinesisTomeLevelParticleMessage msg, Random random) {
        float radius = msg.intensity;

        for (int ring = 0; ring < 3; ring++) {
            float ringRadius = radius * (ring + 1) / 3.0F;
            int particleCount = (int)(ringRadius * 16);

            for (int i = 0; i < particleCount; i++) {
                double angle = (i / (double)particleCount) * Math.PI * 2;
                double offsetX = Math.cos(angle) * ringRadius;
                double offsetZ = Math.sin(angle) * ringRadius;

                float r = 0.8F + random.nextFloat() * 0.2F;
                float g = 0.1F + random.nextFloat() * 0.15F;
                float b = 0.4F + random.nextFloat() * 0.3F;

                WispParticleData wisp = WispParticleData.wisp(
                        0.4F + random.nextFloat() * 0.2F,
                        r, g, b,
                        1.5F
                );

                mc.level.addParticle(wisp,
                        msg.x + offsetX,
                        msg.y + 0.1,
                        msg.z + offsetZ,
                        0, 0.1 + random.nextDouble() * 0.1, 0
                );
            }
        }

        for (int h = 0; h < 20; h++) {
            double height = h * 0.5;
            for (int i = 0; i < 6; i++) {
                double angle = (i / 6.0) * Math.PI * 2 + height * 0.3;
                double r = 0.3 + (1 - height / 10.0) * 0.5;
                double offsetX = Math.cos(angle) * r;
                double offsetZ = Math.sin(angle) * r;

                SparkleParticleData sparkle = SparkleParticleData.sparkle(
                        0.6F, 1.0F, 0.2F, 0.6F, 8
                );

                mc.level.addParticle(sparkle,
                        msg.x + offsetX,
                        msg.y + height,
                        msg.z + offsetZ,
                        0, 0.05, 0
                );
            }
        }

        for (int i = 0; i < 8; i++) {
            double angle = (i / 8.0) * Math.PI * 2;
            double endX = msg.x + Math.cos(angle) * radius;
            double endZ = msg.z + Math.sin(angle) * radius;

            Vec3 start = new Vec3(msg.x, msg.y + 5, msg.z);
            Vec3 end = new Vec3(endX, msg.y + 0.5, endZ);

            BoltRenderInfo renderInfo = BoltRenderInfo.DEFAULT
                    .color(new Vector4f(1.0F, 0.4F, 0.6F, 0.8F))
                    .noise(0.2F, 0.3F);

            BoltParticleOptions bolt = new BoltParticleOptions(renderInfo, start, end)
                    .count(1)
                    .size(0.05F)
                    .lifespan(15);

            mc.level.addParticle((ParticleOptions) bolt, start.x, start.y, start.z, 0, 0, 0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void spawnBurstAt(Minecraft mc, double x, double y, double z, Random random, int count) {
        for (int i = 0; i < count; i++) {
            double vx = (random.nextDouble() - 0.5) * 0.3;
            double vy = random.nextDouble() * 0.2;
            double vz = (random.nextDouble() - 0.5) * 0.3;

            SparkleParticleData sparkle = SparkleParticleData.sparkle(
                    0.4F + random.nextFloat() * 0.3F,
                    1.0F, 0.3F + random.nextFloat() * 0.2F, 0.5F + random.nextFloat() * 0.3F,
                    5
            );

            mc.level.addParticle(sparkle, x, y, z, vx, vy, vz);
        }
    }
}
