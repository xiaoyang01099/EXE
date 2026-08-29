package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Util.AutoTrackingTargetValidator;
import org.xiaoyang.ex_enigmaticlegacy.Item.MagicBowItem;

import java.util.function.Supplier;

// AutoTrackingShootC2SPacket 表示客户端请求服务端按当前锁定目标发射一次魔法箭。
public class AutoTrackingShootC2SPacket {
    private final int targetId; // 客户端锁定目标实体 ID，-1 表示无目标。
    private final boolean restartUsing; // 是否发射后继续下一轮拉弓。

    public AutoTrackingShootC2SPacket() {
        this(-1, false);
    }

    public AutoTrackingShootC2SPacket(int targetId, boolean restartUsing) {
        this.targetId = targetId;
        this.restartUsing = restartUsing;
    }

    public AutoTrackingShootC2SPacket(FriendlyByteBuf buffer) {
        this.targetId = buffer.readVarInt();
        this.restartUsing = buffer.readBoolean();
    }

    // 写入自动追踪射击请求。
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(targetId);
        buffer.writeBoolean(restartUsing);
    }

    // 服务端重新校验锁定目标，校验失败时按普通方向射击。
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            ItemStack stack = findMagicBowStack(player);
            if (!(stack.getItem() instanceof MagicBowItem magicBowItem)) return;
            if (!magicBowItem.hasAutoTracking(stack)) return;

            LivingEntity target = resolveValidTarget(player);
            MagicBowItem.suppressNextAutoTrackingVanillaShot(player);
            magicBowItem.shootMagicArrow(player.level(), player, stack, restartUsing, target);
        });
        context.setPacketHandled(true);
    }

    // 查找玩家当前使用或手持的魔法弓。
    public ItemStack findMagicBowStack(ServerPlayer player) {
        ItemStack useStack = player.getUseItem();
        if (useStack.getItem() instanceof MagicBowItem) return useStack;

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof MagicBowItem) return mainHand;

        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof MagicBowItem) return offHand;

        return ItemStack.EMPTY;
    }

    // 解析并校验服务端目标。
    public LivingEntity resolveValidTarget(ServerPlayer player) {
        if (targetId < 0) return null;
        Entity entity = player.level().getEntity(targetId);
        if (!AutoTrackingTargetValidator.isValidServerTarget(player, entity)) return null;
        return (LivingEntity) entity;
    }
}
