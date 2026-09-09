package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Client.renderer.bolt.TrueBolt;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TrueBoltSpawnPacket {
    private final double startX;
    private final double startY;
    private final double startZ;
    private final double endX;
    private final double endY;
    private final double endZ;
    private final long seed;
    private final int maxAge;
    private final float width;
    private final float r;
    private final float g;
    private final float b;

    public TrueBoltSpawnPacket(Vec3 start, Vec3 end, long seed, int maxAge, float width, float r, float g, float b) {
        this.startX = start.x;
        this.startY = start.y;
        this.startZ = start.z;
        this.endX = end.x;
        this.endY = end.y;
        this.endZ = end.z;
        this.seed = seed;
        this.maxAge = maxAge;
        this.width = width;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public TrueBoltSpawnPacket(FriendlyByteBuf buffer) {
        this.startX = buffer.readDouble();
        this.startY = buffer.readDouble();
        this.startZ = buffer.readDouble();
        this.endX = buffer.readDouble();
        this.endY = buffer.readDouble();
        this.endZ = buffer.readDouble();
        this.seed = buffer.readLong();
        this.maxAge = buffer.readInt();
        this.width = buffer.readFloat();
        this.r = buffer.readFloat();
        this.g = buffer.readFloat();
        this.b = buffer.readFloat();
    }

    public static void encode(TrueBoltSpawnPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.startX);
        buf.writeDouble(msg.startY);
        buf.writeDouble(msg.startZ);
        buf.writeDouble(msg.endX);
        buf.writeDouble(msg.endY);
        buf.writeDouble(msg.endZ);
        buf.writeLong(msg.seed);
        buf.writeInt(msg.maxAge);
        buf.writeFloat(msg.width);
        buf.writeFloat(msg.r);
        buf.writeFloat(msg.g);
        buf.writeFloat(msg.b);
    }

    public static void handle(TrueBoltSpawnPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                handleClient(msg, ctx);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void handleClient(TrueBoltSpawnPacket msg, Supplier<NetworkEvent.Context> ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Vec3 start = new Vec3(msg.startX, msg.startY, msg.startZ);
            Vec3 end = new Vec3(msg.endX, msg.endY, msg.endZ);
            TrueBolt bolt = new TrueBolt(start, end, msg.seed, msg.maxAge, msg.width, msg.r, msg.g, msg.b);
            TrueBolt.Manager.spawn(bolt);
        }
    }
}