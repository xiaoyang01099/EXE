package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Event.registry.ClientKeyChargeRegistry;

import java.util.function.Supplier;

// HeavenlyThunderChargeSyncS2CPacket 同步玩家天雷蓄力动作给追踪客户端。
public class HeavenlyThunderChargeSyncS2CPacket {
    public final int entityId; // 蓄力玩家实体 ID。
    public final boolean active; // 是否正在蓄力。
    public final InteractionHand hand; // 持有天雷战戟的手。
    public final int chargeTicks; // 满蓄力所需 tick。
    public final int elapsedTicks; // 服务端发包时已经过的蓄力 tick。

    public HeavenlyThunderChargeSyncS2CPacket() {
        this(0, false, InteractionHand.MAIN_HAND, 1, 0);
    }

    public HeavenlyThunderChargeSyncS2CPacket(int entityId, boolean active, InteractionHand hand, int chargeTicks, int elapsedTicks) {
        this.entityId = entityId;
        this.active = active;
        this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
        this.chargeTicks = Math.max(1, chargeTicks);
        this.elapsedTicks = Math.max(0, elapsedTicks);
    }

    public HeavenlyThunderChargeSyncS2CPacket(FriendlyByteBuf buffer) {
        this.entityId = buffer.readVarInt();
        this.active = buffer.readBoolean();
        this.hand = buffer.readEnum(InteractionHand.class);
        this.chargeTicks = Math.max(1, buffer.readVarInt());
        this.elapsedTicks = Math.max(0, buffer.readVarInt());
    }

    // 把玩家实体、手部和服务端蓄力时间写入网络缓冲区。
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeBoolean(active);
        buffer.writeEnum(hand);
        buffer.writeVarInt(chargeTicks);
        buffer.writeVarInt(elapsedTicks);
    }

    // 客户端收到后更新本地和远端玩家共用的蓄力视觉缓存。
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        int safeEntityId = entityId;
        boolean safeActive = active;
        InteractionHand safeHand = hand;
        int safeChargeTicks = chargeTicks;
        int safeElapsedTicks = elapsedTicks;
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientKeyChargeRegistry.apply(safeEntityId, safeActive, safeHand, safeChargeTicks, safeElapsedTicks)));
        context.setPacketHandled(true);
    }
}
