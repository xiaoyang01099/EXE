package org.xiaoyang.ex_enigmaticlegacy.Client.particle.state;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

import java.util.*;
import java.util.function.Consumer;

// SparklingOutlineClientState 保存客户端收到的闪闪果实火焰描边实体状态。
public class SparklingOutlineClientState {
    public static final Map<Integer, Long> ACTIVE_OUTLINES = new HashMap<>(); // 当前客户端激活的实体 ID 到结束游戏时间。

    // 激活指定实体的火焰描边状态，重复 active 包会覆盖本地结束时间。
    public static void activate(int entityId, int durationTicks) {
        ClientLevel level = Minecraft.getInstance().level;
        long now = level == null ? 0L : level.getGameTime();
        long endGameTime = now + Math.max(1, durationTicks);
        ACTIVE_OUTLINES.put(entityId, endGameTime);
    }

    // 移除指定实体的火焰描边状态。
    public static void deactivate(int entityId) {
        ACTIVE_OUTLINES.remove(entityId);
    }

    // 判断指定实体当前是否处于火焰描边状态。
    public static boolean isActive(int entityId) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;
        Long endGameTime = ACTIVE_OUTLINES.get(entityId);
        return endGameTime != null && endGameTime > level.getGameTime();
    }

    // 返回当前激活实体 ID 的快照，避免渲染遍历时被网络包修改。
    public static List<Integer> activeEntityIds() {
        return new ArrayList<>(ACTIVE_OUTLINES.keySet());
    }

    // 只遍历当前仍然有效且能在客户端找到的火焰描边实体。
    public static void forEachActive(ClientLevel level, Consumer<Entity> consumer) {
        if (level == null || consumer == null) return;
        if(ACTIVE_OUTLINES.isEmpty()) return;
        for (int entityId : activeEntityIds()) {
            if (!isActive(entityId)) continue;
            Entity entity = level.getEntity(entityId);
            if (entity == null || !entity.isAlive()) continue;
            consumer.accept(entity);
        }
    }

    // 每客户端 tick 清理过期、实体消失或世界为空的描边状态。
    public static void tick(ClientLevel level) {
        if (level == null) {
            clear();
            return;
        }

        long now = level.getGameTime();
        Iterator<Map.Entry<Integer, Long>> iterator = ACTIVE_OUTLINES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Long> entry = iterator.next();
            Entity entity = level.getEntity(entry.getKey());
            if (entry.getValue() <= now || entity == null || !entity.isAlive()) {
                iterator.remove();
            }
        }
    }

    // 清空所有客户端描边状态，防止切世界后实体 ID 复用。
    public static void clear() {
        ACTIVE_OUTLINES.clear();
    }
}
