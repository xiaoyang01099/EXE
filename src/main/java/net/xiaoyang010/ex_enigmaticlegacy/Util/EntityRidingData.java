package net.xiaoyang010.ex_enigmaticlegacy.Util;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityRidingData {
    private static final Map<UUID, Player> PLAYERS_ON_TOP = new HashMap<>();

    public static void setPlayerOnTop(UUID entityId, Player player) {
        PLAYERS_ON_TOP.put(entityId, player);
    }

    public static void removePlayerOnTop(UUID entityId) {
        PLAYERS_ON_TOP.remove(entityId);
    }

    public static boolean hasPlayerOnTop(UUID entityId) {
        return PLAYERS_ON_TOP.containsKey(entityId) &&
                PLAYERS_ON_TOP.get(entityId) != null &&
                PLAYERS_ON_TOP.get(entityId).isAlive();
    }
}