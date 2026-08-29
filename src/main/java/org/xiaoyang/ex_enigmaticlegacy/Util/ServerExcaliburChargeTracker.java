package org.xiaoyang.ex_enigmaticlegacy.Util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Config.ExExcaliburConfig;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.ExcaliburChargeEntity;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.ExcaliburSwordWaveEntity;
import org.xiaoyang.ex_enigmaticlegacy.Item.FlySwordItem;
import org.xiaoyang.ex_enigmaticlegacy.Item.FlySwordPlusItem;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.ExcaliburChargeSyncS2CPacket;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModSounds;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

// ServerExcaliburChargeTracker 记录服务端咖喱棒蓄力时间、同步实体和玩家移动锁定。
public class ServerExcaliburChargeTracker {
    public static final Map<UUID, ChargeState> EXCALIBUR_CHARGES = new HashMap<>(); // 咖喱棒玩家蓄力表。
    public static final String COOLDOWN_MESSAGE_KEY = "message.exe.excalibur.cooldown"; // 咖喱棒服务端冷却提示翻译键。

    // 校验咖喱棒开始条件，登记服务端时间并生成同步实体。
    public static boolean start(ServerPlayer player) {
        if (!canStart(player)) {
            sendInactiveToPlayer(player);
            return false;
        }
        ChargeState existing = EXCALIBUR_CHARGES.get(player.getUUID());
        if (existing != null) {
            sendStateToPlayer(player, player);
            return true;
        }

        InteractionHand hand = FlySwordItem.getHeldFlySwordPlusHand(player);
        ExcaliburChargeEntity entity = ExcaliburChargeEntity.create(player, hand);
        player.level().addFreshEntity(entity);
        ChargeState state = new ChargeState(player.level().getGameTime(), hand, player.position(), entity.getId());
        EXCALIBUR_CHARGES.put(player.getUUID(), state);
        broadcast(player, true, state, 0);
        return true;
    }

    // 判断服务端是否允许开始咖喱棒蓄力。
    public static boolean canStart(ServerPlayer player) {
        if (player == null || !player.isAlive() || player.isSpectator()) return false;
        return FlySwordItem.isHoldingFlySwordPlus(player);
    }

    // 校验释放包是否满足服务端记录的真实蓄力时间和当前物品条件，不检查冷却。
    public static boolean canReleaseIgnoringCooldown(ServerPlayer player) {
        if (player == null) return false;
        ChargeState state = EXCALIBUR_CHARGES.get(player.getUUID());
        if (state == null || !canStart(player)) return false;
        if (FlySwordItem.getHeldFlySwordPlusHand(player) != state.hand) return false;
        long elapsed = player.level().getGameTime() - state.startGameTime;
        return elapsed >= state.fullChargeTicks && elapsed <= state.maxChargeTicks;
    }

    // 校验释放包是否满足服务端蓄力条件和通用技能冷却。
    public static boolean canRelease(ServerPlayer player) {
        return canReleaseIgnoringCooldown(player) && !ServerSkillCooldowns.isCoolingDown(player, ServerSkillCooldowns.EXCALIBUR);
    }

    // 满蓄力释放咖喱棒，生成 EX 剑气伤害实体并保留原释放视觉。
    public static boolean release(ServerPlayer player) {
        if (!canReleaseIgnoringCooldown(player)) return false;
        if (ServerSkillCooldowns.isCoolingDown(player, ServerSkillCooldowns.EXCALIBUR)) {
            sendCooldownMessage(player);
            return false;
        }
        ChargeState state = EXCALIBUR_CHARGES.remove(player.getUUID());

        // 服务端固定松键瞬间的玩家方向，客户端不能绕过满蓄力校验生成伤害实体。
        ExcaliburSwordWaveEntity swordWaveEntity = ExcaliburSwordWaveEntity.create(player);
        player.level().addFreshEntity(swordWaveEntity);
        playCastSound(player);
        ServerSkillCooldowns.setCooldown(player, ServerSkillCooldowns.EXCALIBUR, ExExcaliburConfig.cooldownTicks());

        Entity entity = player.level().getEntity(state.syncEntityId);
        if (entity instanceof ExcaliburChargeEntity chargeEntity) {
            chargeEntity.startReleaseVisual();
        }
        EntityUtil.clearMovementLock(player);
        broadcast(player, false, state, state.fullChargeTicks);
        return true;
    }

    // 向玩家提示咖喱棒服务端冷却剩余秒数。
    public static void sendCooldownMessage(ServerPlayer player) {
        if (player == null) return;
        int remainingTicks = ServerSkillCooldowns.getRemainingTicks(player, ServerSkillCooldowns.EXCALIBUR);
        if (remainingTicks <= 0) return;
        int remainingSeconds = Mth.ceil(remainingTicks / 20.0F);
        player.displayClientMessage(Component.translatable(COOLDOWN_MESSAGE_KEY, remainingSeconds), true);
    }

    // 服务端确认成功释放后向附近玩家播放咖喱棒发射音效。
    public static void playCastSound(ServerPlayer player) {
        if (player == null) return;
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.AAAAA.get(), SoundSource.PLAYERS, 1.6F, 1.0F);
    }

    // 停止指定玩家的咖喱棒蓄力并清理同步实体。
    public static boolean stop(ServerPlayer player) {
        if (player == null) return false;
        ChargeState removed = EXCALIBUR_CHARGES.remove(player.getUUID());
        if (removed == null) return false;
        Entity entity = player.level().getEntity(removed.syncEntityId);
        if (entity instanceof ExcaliburChargeEntity) {
            entity.discard();
        }
        EntityUtil.clearMovementLock(player);
        broadcast(player, false, removed, removed.fullChargeTicks);
        return true;
    }

    // 服务端每 tick 清理无效、切换物品或超过最大蓄力的玩家，并刷新移动锁。
    public static void tick(MinecraftServer server) {
        if (server == null || EXCALIBUR_CHARGES.isEmpty()) return;
        Iterator<Map.Entry<UUID, ChargeState>> iterator = EXCALIBUR_CHARGES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ChargeState> entry = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            ChargeState state = entry.getValue();
            boolean expired = player == null || player.level().getGameTime() - state.startGameTime > state.maxChargeTicks;
            boolean invalid = player == null || !player.isAlive() || player.isSpectator()
                    || FlySwordItem.getHeldFlySwordPlusHand(player) != state.hand;
            if (!expired && !invalid) {
                EntityUtil.lockMovement(player, 2, state.anchor);
                continue;
            }
            iterator.remove();
            if (player != null) {
                Entity entity = player.level().getEntity(state.syncEntityId);
                if (entity instanceof ExcaliburChargeEntity) entity.discard();
                EntityUtil.clearMovementLock(player);
                broadcast(player, false, state, state.fullChargeTicks);
            }
        }
    }

    // 清理全部服务端咖喱棒蓄力状态，服务器关闭时使用。
    public static void clearAll() {
        EXCALIBUR_CHARGES.clear();
    }

    // 向刚开始追踪蓄力玩家的客户端补发当前状态。
    public static void sendStateToPlayer(ServerPlayer chargingPlayer, ServerPlayer receiver) {
        if (chargingPlayer == null || receiver == null) return;
        ChargeState state = EXCALIBUR_CHARGES.get(chargingPlayer.getUUID());
        if (state == null) return;
        int elapsed = (int) Math.max(0L, chargingPlayer.level().getGameTime() - state.startGameTime);
        NetworkHandler.sendToPlayer(new ExcaliburChargeSyncS2CPacket(chargingPlayer.getId(), true,
                state.hand, state.fullChargeTicks, elapsed, state.maxChargeTicks), receiver);
    }

    // 向请求玩家发送关闭状态，用于服务端拒绝开始蓄力时回滚本地视觉。
    public static void sendInactiveToPlayer(ServerPlayer player) {
        if (player == null) return;
        NetworkHandler.sendToPlayer(new ExcaliburChargeSyncS2CPacket(player.getId(), false,
                InteractionHand.MAIN_HAND, FlySwordPlusItem.getExcaliburFullChargeTicks(), 0,
                FlySwordPlusItem.EXCALIBUR_MAX_CHARGE_TICKS), player);
    }

    // 向追踪玩家和释放者同步咖喱棒蓄力状态。
    public static void broadcast(ServerPlayer player, boolean active, ChargeState state, int elapsedTicks) {
        if (player == null || state == null) return;
        NetworkHandler.sendToTrackingEntityAndSelf(new ExcaliburChargeSyncS2CPacket(player.getId(), active,
                state.hand, state.fullChargeTicks, elapsedTicks, state.maxChargeTicks), player);
    }

    // ChargeState 保存服务端可信的咖喱棒蓄力起点、手部、锚点和同步实体。
    public static class ChargeState {
        public final long startGameTime; // 服务端开始蓄力 gameTime。
        public final int fullChargeTicks; // 满蓄力 tick。
        public final int maxChargeTicks; // 最大蓄力 tick。
        public final InteractionHand hand; // 开始蓄力时真·飞剑所在手。
        public final Vec3 anchor; // 开始蓄力位置。
        public final int syncEntityId; // 蓄力同步实体 ID。

        public ChargeState(long startGameTime, InteractionHand hand, Vec3 anchor, int syncEntityId) {
            this.startGameTime = startGameTime;
            this.fullChargeTicks = FlySwordPlusItem.getExcaliburFullChargeTicks();
            this.maxChargeTicks = FlySwordPlusItem.EXCALIBUR_MAX_CHARGE_TICKS;
            this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
            this.anchor = anchor == null ? Vec3.ZERO : anchor;
            this.syncEntityId = syncEntityId;
        }
    }
}
