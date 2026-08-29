package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.slash.ClientEffects;

import java.util.UUID;
import java.util.function.Supplier;

public class DimensionalSlashS2CPacket {

    private final double x, y, z;
    private final int slashCount;
    private final float length, radius;
    private final long seed;
    private final float facingX, facingZ;
    private final UUID ownerId;

    public DimensionalSlashS2CPacket(double x, double y, double z, int slashCount,float length, float radius, long seed,
                                     float facingX, float facingZ, UUID ownerId) {
        this.x = x; this.y = y; this.z = z;
        this.slashCount = slashCount;
        this.length = length; this.radius = radius;
        this.seed = seed;
        this.facingX = facingX; this.facingZ = facingZ;
        this.ownerId = ownerId;
    }

    public static void encode(DimensionalSlashS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeVarInt(msg.slashCount);
        buf.writeFloat(msg.length);
        buf.writeFloat(msg.radius);
        buf.writeLong(msg.seed);
        buf.writeFloat(msg.facingX);
        buf.writeFloat(msg.facingZ);
        buf.writeUUID(msg.ownerId);
    }

    public static DimensionalSlashS2CPacket decode(FriendlyByteBuf buf) {
        return new DimensionalSlashS2CPacket(
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readVarInt(),
                buf.readFloat(), buf.readFloat(),
                buf.readLong(),
                buf.readFloat(), buf.readFloat(),
                buf.readUUID()
        );
    }

    public static void handle(DimensionalSlashS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientEffects.trigger(
                                new Vec3(msg.x, msg.y, msg.z),
                                msg.slashCount,
                                msg.length,
                                msg.radius,
                                msg.seed,
                                new Vec3(msg.facingX, 0.0, msg.facingZ),
                                msg.ownerId
                        )
                )
        );ctx.get().setPacketHandled(true);
    }
}