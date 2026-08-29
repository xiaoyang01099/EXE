package org.xiaoyang.ex_enigmaticlegacy.Entity.others.colorful;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 电磁炮蓄力状态追踪器。
public class ChargeTracker {
    // 默认蓄力参数。
    public static final int DEFAULT_FULL_CHARGE_TIME = 50;
    public static final float MIN_LAUNCH_THRESHOLD = 0.2f;// 最小发射蓄力阈值（0.0 ~ 1.0）

    // 按逻辑端隔离蓄力状态，避免单人游戏中客户端特效状态提前移除服务端发射状态。
    private static final Map<ChargeKey, ChargeInfo> chargeMap = new ConcurrentHashMap<>();

    // 使用默认蓄力时间开始蓄力。
    public static void startCharge(Player player, int currentTick) {
        startCharge(player, currentTick, DEFAULT_FULL_CHARGE_TIME);
    }

    // 使用指定蓄力时间开始蓄力。
    public static void startCharge(Player player, int currentTick, int fullChargeTime) {
        ChargeKey playerId = key(player);
        if (chargeMap.containsKey(playerId)) {
            return;
        }

        ChargeInfo info = new ChargeInfo(currentTick, fullChargeTime);
        chargeMap.put(playerId, info);
    }

    // 更新蓄力进度。
    public static void updateCharge(Player player, int currentTick) {
        ChargeKey playerId = key(player);
        ChargeInfo info = chargeMap.get(playerId);
        if (info == null) {
            return;
        }

        info.update(currentTick);
    }

    // 停止蓄力并返回进度。
    public static float stopCharge(Player player) {
        ChargeKey playerId = key(player);
        ChargeInfo info = chargeMap.remove(playerId);
        if (info == null) {
            return -1;
        }

        return info.stop();
    }

    // 获取当前蓄力进度。
    public static float getProgress(Player player) {
        ChargeKey playerId = key(player);
        ChargeInfo info = chargeMap.get(playerId);
        return info != null ? info.getProgress() : 0.0f;
    }

    // 检查玩家是否正在蓄力。
    public static boolean isCharging(Player player) {
        ChargeKey playerId = key(player);
        return chargeMap.containsKey(playerId);
    }

    // 检查玩家是否已完成蓄力。
    public static boolean isFullyCharged(Player player) {
        ChargeKey playerId = key(player);
        ChargeInfo info = chargeMap.get(playerId);
        return info != null && info.isFullyCharged();
    }

    // 检查是否达到发射阈值。
    public static boolean canLaunch(Player player) {
        return getProgress(player) >= MIN_LAUNCH_THRESHOLD;
    }

    // 清理所有蓄力状态。
    public static void clearAll() {
        chargeMap.clear();
    }

    // 生成带逻辑端信息的 key。
    private static ChargeKey key(Player player) {
        return new ChargeKey(player.getUUID(), player.level().isClientSide());
    }

    // 玩家蓄力状态 key。
    private record ChargeKey(UUID playerId, boolean clientSide) {}
}
