package org.xiaoyang.ex_enigmaticlegacy.Util;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// ServerSkillCooldowns 负责服务端技能防刷冷却，不使用原版物品冷却。
public class ServerSkillCooldowns {
    public static final String BATTO_SLASH = "batto_slash"; // 拔刀斩服务端冷却 key。
    public static final String SUMMON_FLY_SWORD = "summon_fly_sword"; // 召唤飞剑服务端冷却 key。
    public static final String DIMENSION_SLASH = "dimension_slash"; // 次元斩服务端冷却 key。
    public static final String HEAVENLY_THUNDER = "heavenly_thunder"; // 天雷技能服务端冷却 key。
    public static final String EXCALIBUR = "excalibur"; // 咖喱棒服务端冷却 key。
    public static final int BATTO_SLASH_CLIENT_COOLDOWN_TICKS = 100; // 拔刀斩客户端冷却 tick。
    public static final int SUMMON_FLY_SWORD_CLIENT_COOLDOWN_TICKS = 200; // 召唤飞剑客户端冷却 tick。
    public static final Map<String, Map<UUID, Long>> COOLDOWNS = new HashMap<>(); // 按技能 key 和玩家 UUID 保存冷却结束时间。

    // 判断玩家指定技能是否仍在服务端冷却中。
    public static boolean isCoolingDown(ServerPlayer player, String skillKey) {
        return getRemainingTicks(player, skillKey) > 0;
    }

    // 判断玩家指定技能是否仍在服务端冷却中。
    public static boolean isCoolingDown(ServerPlayer player, SkillCooldownType skillType) {
        return skillType != null && isCoolingDown(player, skillType.key());
    }

    // 取得玩家指定技能剩余服务端冷却 tick。
    public static int getRemainingTicks(ServerPlayer player, String skillKey) {
        if (player == null || skillKey == null) return 0;
        Map<UUID, Long> skillCooldowns = COOLDOWNS.get(skillKey);
        if (skillCooldowns == null) return 0;
        long gameTime = player.level().getGameTime();
        long expiry = skillCooldowns.getOrDefault(player.getUUID(), 0L);
        return expiry > gameTime ? (int) (expiry - gameTime) : 0;
    }

    // 取得玩家指定技能剩余服务端冷却 tick。
    public static int getRemainingTicks(ServerPlayer player, SkillCooldownType skillType) {
        return skillType == null ? 0 : getRemainingTicks(player, skillType.key());
    }

    // 写入玩家指定技能的服务端冷却结束时间。
    public static void setCooldown(ServerPlayer player, String skillKey, int cooldownTicks) {
        if (player == null || skillKey == null) return;
        int safeCooldown = Math.max(1, cooldownTicks);
        COOLDOWNS.computeIfAbsent(skillKey, key -> new HashMap<>())
                .put(player.getUUID(), player.level().getGameTime() + safeCooldown);
    }

    // 按统一技能定义写入玩家服务端冷却结束时间。
    public static void setCooldown(ServerPlayer player, SkillCooldownType skillType) {
        if (skillType == null) return;
        setCooldown(player, skillType.key(), skillType.serverCooldownTicks());
    }

    // 清理指定玩家的所有服务端技能冷却。
    public static void clearPlayer(ServerPlayer player) {
        if (player == null) return;
        UUID uuid = player.getUUID();
        for (Map<UUID, Long> skillCooldowns : COOLDOWNS.values()) {
            skillCooldowns.remove(uuid);
        }
    }

    // 清理全部服务端技能冷却，服务器关闭时使用。
    public static void clearAll() {
        COOLDOWNS.clear();
    }

    // 若技能不在冷却中则写入冷却并返回 true，否则返回 false。
    public static boolean tryStartCooldown(ServerPlayer player, String skillKey, int cooldownTicks) {
        if (isCoolingDown(player, skillKey)) return false;
        setCooldown(player, skillKey, cooldownTicks);
        return true;
    }

    // 若统一技能定义不在冷却中则写入冷却并返回 true，否则返回 false。
    public static boolean tryStartCooldown(ServerPlayer player, SkillCooldownType skillType) {
        if (skillType == null || isCoolingDown(player, skillType)) return false;
        setCooldown(player, skillType);
        return true;
    }

    // 服务端冷却比客户端冷却少 1 秒，给网络延迟留缓冲。
    public static int serverCooldownTicks(int clientCooldownTicks) {
        return Math.max(1, clientCooldownTicks - 20);
    }
}
