package org.xiaoyang.ex_enigmaticlegacy.Util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.xiaoyang.ex_enigmaticlegacy.Config.TridentPlusConfig;
import org.xiaoyang.ex_enigmaticlegacy.Item.TridentPlusItem;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.HeavenlyThunderChargeSyncS2CPacket;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

// ServerKeyChargeTracker 记录服务端按键蓄力时间并向追踪客户端同步玩家动作。
public class ServerKeyChargeTracker {
    public static final int EXPIRE_GRACE_TICKS = 40; // 满蓄力后等待释放包的兜底 tick。
    public static final Map<UUID, ChargeState> HEAVENLY_THUNDER_CHARGES = new HashMap<>(); // 天雷技能玩家蓄力表。

    // 校验天雷技能开始条件，登记服务端时间并广播蓄力动作。
    public static boolean startHeavenlyThunder(ServerPlayer player) {
        if (!canStartHeavenlyThunder(player)) {
            sendInactiveToPlayer(player);
            return false;
        }
        ChargeState existing = HEAVENLY_THUNDER_CHARGES.get(player.getUUID());
        if (existing != null) {
            sendStateToPlayer(player, player);
            return true;
        }

        InteractionHand hand = TridentPlusItem.getHeldHeavenlyThunderHand(player);
        int chargeTicks = TridentPlusConfig.heavenlyThunderChargeTicks();
        ChargeState state = new ChargeState(player.level().getGameTime(), chargeTicks, hand);
        HEAVENLY_THUNDER_CHARGES.put(player.getUUID(), state);
        broadcast(player, true, state, 0);
        return true;
    }

    // 判断服务端是否允许开始天雷蓄力。
    public static boolean canStartHeavenlyThunder(ServerPlayer player) {
        if (player == null || !player.isAlive() || player.isSpectator()) return false;
        if (ServerSkillCooldowns.isCoolingDown(player, SkillCooldownType.HEAVENLY_THUNDER)) return false;
        if (!TridentPlusItem.isHoldingHeavenlyThunderTrident(player)) return false;
        return TridentPlusItem.hasEnoughHeavenlyThunderFood(player);
    }

    // 校验释放包是否满足服务端记录的真实蓄力时间和当前物品条件。
    public static boolean canReleaseHeavenlyThunder(ServerPlayer player) {
        if (player == null) return false;
        ChargeState state = HEAVENLY_THUNDER_CHARGES.get(player.getUUID());
        if (state == null || !canStartHeavenlyThunder(player)) return false;
        if (TridentPlusItem.getHeldHeavenlyThunderHand(player) != state.hand) return false;
        long elapsed = player.level().getGameTime() - state.startGameTime;
        return elapsed >= state.chargeTicks;
    }

    // 停止指定玩家的天雷蓄力并广播动作结束。
    public static boolean stopHeavenlyThunder(ServerPlayer player) {
        if (player == null) return false;
        ChargeState removed = HEAVENLY_THUNDER_CHARGES.remove(player.getUUID());
        if (removed == null) return false;
        broadcast(player, false, removed, removed.chargeTicks);
        return true;
    }

    // 服务端每 tick 清理无效、切换物品或超时的蓄力玩家。
    public static void tick(MinecraftServer server) {
        if (server == null || HEAVENLY_THUNDER_CHARGES.isEmpty()) return;
        Iterator<Map.Entry<UUID, ChargeState>> iterator = HEAVENLY_THUNDER_CHARGES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ChargeState> entry = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            ChargeState state = entry.getValue();
            boolean expired = player == null || player.level().getGameTime() - state.startGameTime > state.chargeTicks + EXPIRE_GRACE_TICKS;
            boolean invalid = player == null || !player.isAlive() || player.isSpectator()
                    || TridentPlusItem.getHeldHeavenlyThunderHand(player) != state.hand;
            if (!expired && !invalid) continue;
            iterator.remove();
            if (player != null) broadcast(player, false, state, state.chargeTicks);
        }
    }

    // 向刚开始追踪蓄力玩家的客户端补发当前状态。
    public static void sendStateToPlayer(ServerPlayer chargingPlayer, ServerPlayer receiver) {
        if (chargingPlayer == null || receiver == null) return;
        ChargeState state = HEAVENLY_THUNDER_CHARGES.get(chargingPlayer.getUUID());
        if (state == null) return;
        int elapsed = (int) Math.max(0L, chargingPlayer.level().getGameTime() - state.startGameTime);
        NetworkHandler.sendToPlayer(new HeavenlyThunderChargeSyncS2CPacket(chargingPlayer.getId(), true,
                state.hand, state.chargeTicks, elapsed), receiver);
    }

    // 向请求玩家发送关闭状态，用于服务端拒绝开始蓄力时回滚本地视觉。
    public static void sendInactiveToPlayer(ServerPlayer player) {
        if (player == null) return;
        NetworkHandler.sendToPlayer(new HeavenlyThunderChargeSyncS2CPacket(player.getId(), false,
                InteractionHand.MAIN_HAND, TridentPlusConfig.heavenlyThunderChargeTicks(), 0), player);
    }

    // 向追踪玩家和释放者同步蓄力状态。
    public static void broadcast(ServerPlayer player, boolean active, ChargeState state, int elapsedTicks) {
        if (player == null || state == null) return;
        NetworkHandler.sendToTrackingEntityAndSelf(new HeavenlyThunderChargeSyncS2CPacket(player.getId(), active,
                state.hand, state.chargeTicks, elapsedTicks), player);
    }

    // ChargeState 保存服务端可信的蓄力起点、时长和手部。
    public static class ChargeState {
        public final long startGameTime; // 服务端开始蓄力 gameTime。
        public final int chargeTicks; // 满蓄力所需 tick。
        public final InteractionHand hand; // 开始蓄力时持有战戟的手。

        public ChargeState(long startGameTime, int chargeTicks, InteractionHand hand) {
            this.startGameTime = startGameTime;
            this.chargeTicks = Math.max(1, chargeTicks);
            this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
        }
    }
}
