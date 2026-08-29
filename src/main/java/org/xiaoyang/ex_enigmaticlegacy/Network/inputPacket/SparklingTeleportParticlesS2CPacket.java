package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Event.SparklingTeleportParticlesClientHandler;

import java.util.function.Supplier;

// SparklingTeleportParticlesS2CPacket 通知客户端在闪闪果实瞬移路径发射 GPU 粒子。
public class SparklingTeleportParticlesS2CPacket {
    public final double x; // 粒子原点 X。
    public final double y; // 粒子原点 Y。
    public final double z; // 粒子原点 Z。
    public final double targetX; // 瞬移目标点 X。
    public final double targetY; // 瞬移目标点 Y。
    public final double targetZ; // 瞬移目标点 Z。
    public final float height; // 玩家碰撞箱高度。
    public final float width; // 玩家碰撞箱宽度。

    // 创建空粒子通知包。
    public SparklingTeleportParticlesS2CPacket() {
        this(Vec3.ZERO, Vec3.ZERO, 1.8F, 0.6F);
    }

    // 根据原位置、目标位置和玩家尺寸创建粒子通知包。
    public SparklingTeleportParticlesS2CPacket(Vec3 origin, Vec3 target, float height, float width) {
        this.x = origin.x;
        this.y = origin.y;
        this.z = origin.z;
        this.targetX = target.x;
        this.targetY = target.y;
        this.targetZ = target.z;
        this.height = height;
        this.width = width;
    }

    // 从网络缓冲区读取粒子通知包。
    public SparklingTeleportParticlesS2CPacket(FriendlyByteBuf buffer) {
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
        this.targetX = buffer.readDouble();
        this.targetY = buffer.readDouble();
        this.targetZ = buffer.readDouble();
        this.height = buffer.readFloat();
        this.width = buffer.readFloat();
    }

    // 写入粒子通知包。
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeDouble(targetX);
        buffer.writeDouble(targetY);
        buffer.writeDouble(targetZ);
        buffer.writeFloat(height);
        buffer.writeFloat(width);
    }

    // 客户端收到后只在本地提交 GPU 粒子 burst。
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SparklingTeleportParticlesClientHandler.spawn(x, y, z, targetX, targetY, targetZ, height, width)));
        context.setPacketHandled(true);
    }
}
