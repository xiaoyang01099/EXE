package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Event.registry.ClientExcaliburChargeRegistry;

import java.util.function.Supplier;

// ExcaliburChargeSyncS2CPacket 同步玩家咖喱棒蓄力动作给追踪客户端。
public class ExcaliburChargeSyncS2CPacket {
    public final int entityId; // 蓄力玩家实体 ID。
    public final boolean active; // 是否正在蓄力。
    public final InteractionHand hand; // 持有真·飞剑的手。
    public final int fullChargeTicks; // 满蓄力所需 tick。
    public final int elapsedTicks; // 服务端发包时已经过的蓄力 tick。
    public final int maxChargeTicks; // 最大蓄力 tick。

    public ExcaliburChargeSyncS2CPacket() {
        this(0, false, InteractionHand.MAIN_HAND, 1, 0, 1);
    }

    public ExcaliburChargeSyncS2CPacket(int entityId, boolean active, InteractionHand hand, int fullChargeTicks, int elapsedTicks, int maxChargeTicks) {
        this.entityId = entityId;
        this.active = active;
        this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
        this.fullChargeTicks = Math.max(1, fullChargeTicks);
        this.elapsedTicks = Math.max(0, elapsedTicks);
        this.maxChargeTicks = Math.max(this.fullChargeTicks, maxChargeTicks);
    }

    public ExcaliburChargeSyncS2CPacket(FriendlyByteBuf buffer) {
        this.entityId = buffer.readVarInt();
        this.active = buffer.readBoolean();
        this.hand = buffer.readEnum(InteractionHand.class);
        this.fullChargeTicks = Math.max(1, buffer.readVarInt());
        this.elapsedTicks = Math.max(0, buffer.readVarInt());
        this.maxChargeTicks = Math.max(this.fullChargeTicks, buffer.readVarInt());
    }

    // 把玩家实体、手部和服务端蓄力时间写入网络缓冲区。
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeBoolean(active);
        buffer.writeEnum(hand);
        buffer.writeVarInt(fullChargeTicks);
        buffer.writeVarInt(elapsedTicks);
        buffer.writeVarInt(maxChargeTicks);
    }

    // 客户端收到后更新本地和远端玩家共用的咖喱棒蓄力视觉缓存。
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        int safeEntityId = entityId;
        boolean safeActive = active;
        InteractionHand safeHand = hand;
        int safeFullChargeTicks = fullChargeTicks;
        int safeElapsedTicks = elapsedTicks;
        int safeMaxChargeTicks = maxChargeTicks;
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientExcaliburChargeRegistry.apply(safeEntityId, safeActive, safeHand, safeFullChargeTicks, safeElapsedTicks, safeMaxChargeTicks)));
        context.setPacketHandled(true);
    }
}
