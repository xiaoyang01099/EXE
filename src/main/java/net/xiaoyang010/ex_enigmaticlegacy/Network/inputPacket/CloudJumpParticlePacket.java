package net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CloudJumpParticlePacket {
    private final double x;
    private final double y;
    private final double z;

    public CloudJumpParticlePacket(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public CloudJumpParticlePacket(FriendlyByteBuf buf) {
        this(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void encode(CloudJumpParticlePacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
    }

    public static CloudJumpParticlePacket decode(FriendlyByteBuf buf) {
        return new CloudJumpParticlePacket(buf);
    }

    public static void handle(CloudJumpParticlePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                Level level = net.minecraft.client.Minecraft.getInstance().level;
                if (level != null) {
                    for (int i = 0; i < 80; i++) {
                        double angle = level.random.nextDouble() * Math.PI * 2;
                        double distance = level.random.nextDouble() * 1.8D;
                        double offsetX = Math.sin(angle) * distance;
                        double offsetZ = Math.cos(angle) * distance;

                        double offsetY = level.random.nextDouble() * 0.4D;

                        level.addParticle(
                                ParticleTypes.SMOKE,
                                msg.x + offsetX,
                                msg.y + offsetY,
                                msg.z + offsetZ,
                                offsetX * 0.08D,
                                level.random.nextDouble() * 0.1D,
                                offsetZ * 0.08D
                        );

                        if (i % 2 == 0) {
                            level.addParticle(
                                    ParticleTypes.LARGE_SMOKE,
                                    msg.x + offsetX * 0.7D,
                                    msg.y + offsetY * 0.5D,
                                    msg.z + offsetZ * 0.7D,
                                    offsetX * 0.1D,
                                    level.random.nextDouble() * 0.15D,
                                    offsetZ * 0.1D
                            );
                        }

                        if (i % 6 == 0) {
                            double blastDistance = distance * 1.2;
                            double blastX = Math.sin(angle) * blastDistance;
                            double blastZ = Math.cos(angle) * blastDistance;

                            level.addParticle(
                                    ParticleTypes.POOF,
                                    msg.x + blastX,
                                    msg.y + 0.1D,
                                    msg.z + blastZ,
                                    blastX * 0.15D,
                                    0.05D,
                                    blastZ * 0.15D
                            );
                        }

                        if (i % 7 == 0) {
                            double flameAngle = angle + (level.random.nextDouble() - 0.5) * 0.5;
                            double flameDistance = distance * 0.9;
                            double flameX = Math.sin(flameAngle) * flameDistance;
                            double flameZ = Math.cos(flameAngle) * flameDistance;

                            level.addParticle(
                                    ParticleTypes.SOUL_FIRE_FLAME,
                                    msg.x + flameX,
                                    msg.y + level.random.nextDouble() * 0.3D,
                                    msg.z + flameZ,
                                    0,
                                    0.1D + level.random.nextDouble() * 0.1D,
                                    0
                            );
                        }

                        if (i % 10 == 0 && distance < 0.7) {
                            level.addParticle(
                                    ParticleTypes.SMALL_FLAME,
                                    msg.x + offsetX * 0.5D,
                                    msg.y + 0.05D,
                                    msg.z + offsetZ * 0.5D,
                                    0,
                                    0.15D + level.random.nextDouble() * 0.1D,
                                    0
                            );
                        }

                        if (i % 8 == 0) {
                            double ashDistance = distance * 1.3;
                            double ashX = Math.sin(angle + level.random.nextDouble() * 0.3) * ashDistance;
                            double ashZ = Math.cos(angle + level.random.nextDouble() * 0.3) * ashDistance;

                            level.addParticle(
                                    ParticleTypes.ASH,
                                    msg.x + ashX,
                                    msg.y + level.random.nextDouble() * 0.5D,
                                    msg.z + ashZ,
                                    (level.random.nextDouble() - 0.5) * 0.1D,
                                    level.random.nextDouble() * 0.05D,
                                    (level.random.nextDouble() - 0.5) * 0.1D
                            );
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}