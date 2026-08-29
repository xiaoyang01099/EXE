package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Util.SkillCooldownType;
import org.xiaoyang.ex_enigmaticlegacy.Event.DimensionSlashKeyInputHandler;

import java.util.function.Supplier;

// HeavenlyThunderCastResultS2CPacket 把服务端天雷释放结果同步给客户端。
public class HeavenlyThunderCastResultS2CPacket {
    public final boolean success; // 本次天雷是否已由服务端成功释放。
    public final int cooldownTicks; // 服务端要求客户端同步的冷却 tick。
    public final boolean notEnoughFood; // 失败原因是否为饱食度不足。

    public HeavenlyThunderCastResultS2CPacket() {
        this(false, 0, false);
    }

    public HeavenlyThunderCastResultS2CPacket(boolean success, int cooldownTicks, boolean notEnoughFood) {
        this.success = success;
        this.cooldownTicks = Math.max(0, cooldownTicks);
        this.notEnoughFood = notEnoughFood;
    }

    public HeavenlyThunderCastResultS2CPacket(FriendlyByteBuf buffer) {
        this.success = buffer.readBoolean();
        this.cooldownTicks = Math.max(0, buffer.readVarInt());
        this.notEnoughFood = buffer.readBoolean();
    }

    // 写入天雷释放结果、冷却和失败原因。
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(success);
        buffer.writeVarInt(cooldownTicks);
        buffer.writeBoolean(notEnoughFood);
    }

    // 客户端按服务端权威结果写入冷却或提示失败原因。
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        boolean safeSuccess = success;
        int safeCooldownTicks = cooldownTicks;
        boolean safeNotEnoughFood = notEnoughFood;
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                DimensionSlashKeyInputHandler.applyHeavenlyThunderCastResult(
                        safeSuccess,
                        safeCooldownTicks <= 0 && safeSuccess ? SkillCooldownType.HEAVENLY_THUNDER.cooldownTicks() : safeCooldownTicks,
                        safeNotEnoughFood
                )));
        context.setPacketHandled(true);
    }
}
