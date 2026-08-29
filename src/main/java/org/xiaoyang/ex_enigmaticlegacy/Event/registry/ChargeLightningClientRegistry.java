package org.xiaoyang.ex_enigmaticlegacy.Event.registry;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChargeLightningClientRegistry {
    private static final Map<UUID, ChargeVisualState> ACTIVE_CHARGES = new ConcurrentHashMap<>(); // 客户端活跃蓄力视觉状态。

    public static void start(Player player, boolean colorful) {
        if (player == null || !player.level().isClientSide()) return;
        ChargeVisualType type = colorful ? ChargeVisualType.COLORFUL_COIN : ChargeVisualType.COIN;
        ACTIVE_CHARGES.put(player.getUUID(), new ChargeVisualState(player.getUUID(), type, colorful));
    }

    public static void startBeamCross(Player player) {
        if (player == null || !player.level().isClientSide()) return;

        ACTIVE_CHARGES.put(player.getUUID(), new ChargeVisualState(player.getUUID(), ChargeVisualType.BEAM_CROSS, false));
    }

    public static void stop(Player player) {
        if (player == null) return;

        stop(player.getUUID());
    }

    public static void stop(UUID playerId) {
        if (playerId == null) return;

        ACTIVE_CHARGES.remove(playerId);
    }

    public static Collection<ChargeVisualState> activeCharges() {
        return ACTIVE_CHARGES.values();
    }

    public static void cleanup(Level level) {
        if (level == null) {
            clearAll();
            return;
        }

        ACTIVE_CHARGES.keySet().removeIf(playerId -> {
            Player player = level.getPlayerByUUID(playerId);
            return player == null || !player.isAlive();
        });
    }

    public static void clearAll() {
        ACTIVE_CHARGES.clear();
    }

    public enum ChargeVisualType {
        COIN,
        COLORFUL_COIN,
        BEAM_CROSS
    }

    public record ChargeVisualState(UUID playerId, ChargeVisualType type, boolean colorful) {
    }
}
