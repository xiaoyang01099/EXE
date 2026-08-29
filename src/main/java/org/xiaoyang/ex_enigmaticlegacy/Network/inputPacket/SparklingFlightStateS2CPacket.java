package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Event.SparklingFlightClientHandler;

import java.util.function.Supplier;

// SparklingFlightStateS2CPacket 同步指定玩家的闪闪果实 Ctrl 加速开关供客户端生成轨迹。
public class SparklingFlightStateS2CPacket {
    public final int entityId; // 正在开始或停止加速的玩家实体 ID。
    public final boolean boosting; // 玩家当前是否处于持续加速状态。
    public final boolean horizontalPose; // 玩家是否已达到最大速度并使用横向飞行姿态。
    public final double maxSpeed; // 服务端最大速度，用于客户端区分合法高速段和异常跳变。

    // 创建空的停止状态包。
    public SparklingFlightStateS2CPacket() {
        this(0, false, false, 0.0D);
    }

    // 根据实体 ID、加速开关和横向姿态创建同步包。
    public SparklingFlightStateS2CPacket(int entityId, boolean boosting, boolean horizontalPose, double maxSpeed) {
        this.entityId = entityId;
        this.boosting = boosting;
        this.horizontalPose = horizontalPose;
        this.maxSpeed = maxSpeed;
    }

    // 从网络缓冲区读取实体和加速状态。
    public SparklingFlightStateS2CPacket(FriendlyByteBuf buffer) {
        this.entityId = buffer.readVarInt();
        this.boosting = buffer.readBoolean();
        this.horizontalPose = buffer.readBoolean();
        this.maxSpeed = buffer.readDouble();
    }

    // 把实体和加速状态写入网络缓冲区。
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeBoolean(boosting);
        buffer.writeBoolean(horizontalPose);
        buffer.writeDouble(maxSpeed);
    }

    // 客户端收到后更新活动玩家和历史轨迹缓存。
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        int safeEntityId = entityId;
        boolean safeBoosting = boosting;
        boolean safeHorizontalPose = horizontalPose;
        double safeMaxSpeed = maxSpeed;
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> SparklingFlightClientHandler.apply(safeEntityId, safeBoosting, safeHorizontalPose, safeMaxSpeed)));
        context.setPacketHandled(true);
    }
}
