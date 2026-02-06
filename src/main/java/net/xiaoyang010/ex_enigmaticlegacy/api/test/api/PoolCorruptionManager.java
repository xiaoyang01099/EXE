package net.xiaoyang010.ex_enigmaticlegacy.api.test.api;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;

/**
 * 魔力池污染管理器(持久化存储)
 */
public class PoolCorruptionManager extends SavedData {
    private static final String DATA_NAME = "ex_enigmaticlegacy_pool_corruption";

    private final Map<BlockPos, PoolCorruptionData> corruptionMap = new HashMap<>();

    public PoolCorruptionManager() {}

    public static PoolCorruptionManager get(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException("Cannot get PoolCorruptionManager on client side!");
        }

        DimensionDataStorage storage = serverLevel.getDataStorage();
        return storage.computeIfAbsent(PoolCorruptionManager::load, PoolCorruptionManager::new, DATA_NAME);
    }

    public static PoolCorruptionData getOrCreate(Level level, BlockPos pos) {
        PoolCorruptionManager manager = get(level);
        return manager.corruptionMap.computeIfAbsent(pos, p -> new PoolCorruptionData());
    }

    public static void remove(Level level, BlockPos pos) {
        PoolCorruptionManager manager = get(level);
        manager.corruptionMap.remove(pos);
        manager.setDirty();
    }

    public static PoolCorruptionManager load(CompoundTag tag) {
        PoolCorruptionManager manager = new PoolCorruptionManager();

        ListTag list = tag.getList("pools", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag poolTag = list.getCompound(i);

            int x = poolTag.getInt("x");
            int y = poolTag.getInt("y");
            int z = poolTag.getInt("z");
            BlockPos pos = new BlockPos(x, y, z);

            PoolCorruptionData data = PoolCorruptionData.load(poolTag.getCompound("data"));
            manager.corruptionMap.put(pos, data);
        }

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();

        for (Map.Entry<BlockPos, PoolCorruptionData> entry : corruptionMap.entrySet()) {
            CompoundTag poolTag = new CompoundTag();

            BlockPos pos = entry.getKey();
            poolTag.putInt("x", pos.getX());
            poolTag.putInt("y", pos.getY());
            poolTag.putInt("z", pos.getZ());

            poolTag.put("data", entry.getValue().save());

            list.add(poolTag);
        }

        tag.put("pools", list);
        return tag;
    }
}
