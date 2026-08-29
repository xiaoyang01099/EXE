package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Event.SparklingOutlineClientHandler;

import java.util.function.Supplier;

// SparklingFruitOutlineS2CPacket 同步客户端指定实体的闪闪果实火焰描边显示状态。
public class SparklingFruitOutlineS2CPacket {
    public final int entityId; // 需要显示或关闭火焰描边的实体 ID。
    public final boolean active; // 是否开启火焰描边。
    public final int durationTicks; // 客户端显示持续 tick，关闭时忽略。

    // 创建空火焰描边同步包。
    public SparklingFruitOutlineS2CPacket() {
        this(0, false, 0);
    }

    // 根据实体 ID、状态和持续时间创建火焰描边同步包。
    public SparklingFruitOutlineS2CPacket(int entityId, boolean active, int durationTicks) {
        this.entityId = entityId;
        this.active = active;
        this.durationTicks = durationTicks;
    }

    // 从网络缓冲区读取火焰描边同步包。
    public SparklingFruitOutlineS2CPacket(FriendlyByteBuf buffer) {
        this.entityId = buffer.readVarInt();
        this.active = buffer.readBoolean();
        this.durationTicks = buffer.readVarInt();
    }

    // 写入火焰描边同步包。
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeBoolean(active);
        buffer.writeVarInt(durationTicks);
    }

    // 客户端收到后更新本地火焰描边状态缓存。
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        int safeEntityId = entityId;
        boolean safeActive = active;
        int safeDurationTicks = durationTicks;
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SparklingOutlineClientHandler.apply(safeEntityId, safeActive, safeDurationTicks)));
        context.setPacketHandled(true);
    }
}
