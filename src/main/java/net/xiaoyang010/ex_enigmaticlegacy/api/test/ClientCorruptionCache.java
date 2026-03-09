package net.xiaoyang010.ex_enigmaticlegacy.api.test;

import net.minecraft.core.BlockPos;
import java.util.HashMap;
import java.util.Map;

public class ClientCorruptionCache {
    private static final Map<Long, Integer> cache = new HashMap<>();

    public static int getCorruption(BlockPos pos) {
        return cache.getOrDefault(pos.asLong(), 0);
    }

    public static void updateCorruption(BlockPos pos, int corruption) {
        if (corruption <= 0) {
            cache.remove(pos.asLong());
        } else {
            cache.put(pos.asLong(), Math.min(100, corruption));
        }
    }

    public static void updateBatch(Map<BlockPos, Integer> data) {
        data.forEach((pos, corruption) -> updateCorruption(pos, corruption));
    }

    public static void removeCorruption(BlockPos pos) {
        cache.remove(pos.asLong());
    }

    public static void clearAll() {
        cache.clear();
    }
}