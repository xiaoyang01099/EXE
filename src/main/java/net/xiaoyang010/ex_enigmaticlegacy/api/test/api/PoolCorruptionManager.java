package net.xiaoyang010.ex_enigmaticlegacy.api.test.api;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PoolCorruptionManager extends SavedData {

    private static final String DATA_NAME = "ex_enigmaticlegacy_pool_corruption";
    private final Map<BlockPos, PoolCorruptionData> corruptionMap = new HashMap<>();
    private ServerLevel serverLevel = null;

    public PoolCorruptionManager() {}

    public static PoolCorruptionManager get(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException(
                    "PoolCorruptionManager 只能在服务端使用！当前调用位置可能在客户端。"
            );
        }
        DimensionDataStorage storage = serverLevel.getDataStorage();
        PoolCorruptionManager manager = storage.computeIfAbsent(
                PoolCorruptionManager::load,
                PoolCorruptionManager::new,
                DATA_NAME
        );
        manager.serverLevel = serverLevel;
        return manager;
    }

    public static PoolCorruptionData getOrCreate(Level level, BlockPos pos) {
        PoolCorruptionManager manager = get(level);
        return manager.corruptionMap.computeIfAbsent(pos.immutable(), p -> new PoolCorruptionData());
    }

    public static void addCorruption(Level level, BlockPos pos, int amount) {
        if (level.isClientSide) return;
        PoolCorruptionManager manager = get(level);
        PoolCorruptionData data = manager.corruptionMap
                .computeIfAbsent(pos.immutable(), p -> new PoolCorruptionData());
        int oldCorruption = data.getCorruption();
        data.addCorruption(amount);
        if (data.getCorruption() != oldCorruption) {
            manager.setDirty();
            manager.broadcastUpdate(pos, data.getCorruption());
        }
    }

    public static void reduceCorruption(Level level, BlockPos pos, int amount) {
        if (level.isClientSide) return;
        PoolCorruptionManager manager = get(level);
        PoolCorruptionData data = manager.corruptionMap.get(pos);
        if (data == null) return;
        int oldCorruption = data.getCorruption();
        data.reduceCorruption(amount);
        manager.setDirty();
        if (data.getCorruption() != oldCorruption) {
            if (data.getCorruption() <= 0) {
                manager.corruptionMap.remove(pos);
                manager.broadcastUpdate(pos, 0);
            } else {
                manager.broadcastUpdate(pos, data.getCorruption());
            }
        }
    }

    public static void remove(Level level, BlockPos pos) {
        if (level.isClientSide) return;
        PoolCorruptionManager manager = get(level);
        if (manager.corruptionMap.remove(pos) != null) {
            manager.setDirty();
            manager.broadcastUpdate(pos, 0);
        }
    }

    public static Map<BlockPos, Integer> getAllCorruptionData(Level level) {
        PoolCorruptionManager manager = get(level);
        return manager.corruptionMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getCorruption()
                ));
    }


    public static void syncToPlayer(Level level, ServerPlayer player) {
        Map<BlockPos, Integer> allData = getAllCorruptionData(level);
        if (!allData.isEmpty()) {
            NetworkHandler.sendAllCorruptionData(player, allData);
        }
    }

    private void broadcastUpdate(BlockPos pos, int corruption) {
        if (serverLevel == null) return;
        NetworkHandler.broadcastCorruptionUpdate(serverLevel, pos, corruption);
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