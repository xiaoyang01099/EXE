package net.xiaoyang010.ex_enigmaticlegacy.Util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.Registry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局花朵位置注册表 - 跟踪所有维度中的跨维度传送花朵
 */
public class InterdimensionalFlowerRegistry extends SavedData {

    private static final String DATA_NAME = "interdimensional_flower_registry";

    // 存储格式: 维度 -> 花朵位置列表
    private final Map<ResourceKey<Level>, Set<BlockPos>> flowerPositions = new ConcurrentHashMap<>();

    // 添加轮询索引，用于实现轮询传送
    private final Map<ResourceKey<Level>, Integer> dimensionRotationIndex = new ConcurrentHashMap<>();

    public InterdimensionalFlowerRegistry() {
        super();
    }

    /**
     * 获取注册表实例
     */
    public static InterdimensionalFlowerRegistry getInstance(MinecraftServer server) {
        var serverLevel = server.overworld();
        return serverLevel.getDataStorage().computeIfAbsent(
                InterdimensionalFlowerRegistry::load,
                InterdimensionalFlowerRegistry::new,
                DATA_NAME
        );
    }

    /**
     * 注册花朵位置
     */
    public synchronized void registerFlower(ResourceKey<Level> dimension, BlockPos pos) {
        Set<BlockPos> positions = flowerPositions.computeIfAbsent(dimension, k -> ConcurrentHashMap.newKeySet());
        boolean isNew = positions.add(pos);

        if (isNew) {
            setDirty();
            System.out.println("[REGISTRY] Registered new flower at " + pos + " in " + dimension.location());
            printRegistryStatus();
        }
    }

    /**
     * 注销花朵位置
     */
    public synchronized void unregisterFlower(ResourceKey<Level> dimension, BlockPos pos) {
        Set<BlockPos> positions = flowerPositions.get(dimension);
        if (positions != null && positions.remove(pos)) {
            if (positions.isEmpty()) {
                flowerPositions.remove(dimension);
                dimensionRotationIndex.remove(dimension); // 清理轮询索引
            }
            setDirty();
            System.out.println("[REGISTRY] Unregistered flower at " + pos + " in " + dimension.location());
            printRegistryStatus();
        }
    }

    /**
     * 获取指定维度的所有花朵位置
     */
    public Set<BlockPos> getFlowersInDimension(ResourceKey<Level> dimension) {
        return flowerPositions.getOrDefault(dimension, Collections.emptySet());
    }

    /**
     * 查找目标花朵 - 修复后的智能查找算法
     */
    public FlowerLocation findTargetFlower(ResourceKey<Level> excludeDimension) {
        // 获取所有有花朵的维度（排除当前维度）
        List<ResourceKey<Level>> availableDimensions = new ArrayList<>();

        for (ResourceKey<Level> dimension : flowerPositions.keySet()) {
            if (!dimension.equals(excludeDimension) && !flowerPositions.get(dimension).isEmpty()) {
                availableDimensions.add(dimension);
            }
        }

        if (availableDimensions.isEmpty()) {
            return null;
        }

        // 如果只有一个可用维度，直接选择
        if (availableDimensions.size() == 1) {
            ResourceKey<Level> targetDim = availableDimensions.get(0);
            BlockPos pos = flowerPositions.get(targetDim).iterator().next();
            return new FlowerLocation(targetDim, pos);
        }

        // 多个维度时，使用轮询算法确保均匀分布
        return selectTargetByRotation(excludeDimension, availableDimensions);
    }

    /**
     * 轮询选择目标维度
     */
    private FlowerLocation selectTargetByRotation(ResourceKey<Level> sourceDimension, List<ResourceKey<Level>> availableDimensions) {
        // 为稳定性，对维度列表排序
        availableDimensions.sort(Comparator.comparing(dim -> dim.location().toString()));

        // 获取当前源维度的轮询索引
        int currentIndex = dimensionRotationIndex.getOrDefault(sourceDimension, 0);

        // 确保索引在有效范围内
        currentIndex = currentIndex % availableDimensions.size();

        // 选择目标维度
        ResourceKey<Level> targetDimension = availableDimensions.get(currentIndex);

        // 更新轮询索引（下次选择下一个维度）
        dimensionRotationIndex.put(sourceDimension, (currentIndex + 1) % availableDimensions.size());
        setDirty();

        // 获取目标位置
        Set<BlockPos> positions = flowerPositions.get(targetDimension);
        if (positions != null && !positions.isEmpty()) {
            BlockPos pos = positions.iterator().next();

            System.out.println("[REGISTRY] " + sourceDimension.location() + " -> " + targetDimension.location() +
                    " (rotation index: " + currentIndex + "/" + availableDimensions.size() + ")");

            return new FlowerLocation(targetDimension, pos);
        }

        return null;
    }

    /**
     * 获取所有已注册的花朵总数
     */
    public int getTotalFlowerCount() {
        return flowerPositions.values().stream().mapToInt(Set::size).sum();
    }

    /**
     * 获取所有维度信息（用于调试）
     */
    public Map<ResourceKey<Level>, Set<BlockPos>> getAllFlowers() {
        return Collections.unmodifiableMap(flowerPositions);
    }

    /**
     * 打印注册表状态
     */
    public void printRegistryStatus() {
        System.out.println("=== Flower Registry Status ===");
        System.out.println("Total flowers: " + getTotalFlowerCount());

        if (flowerPositions.isEmpty()) {
            System.out.println("No flowers registered!");
        } else {
            for (Map.Entry<ResourceKey<Level>, Set<BlockPos>> entry : flowerPositions.entrySet()) {
                String dimName = entry.getKey().location().toString();
                int count = entry.getValue().size();
                int rotationIndex = dimensionRotationIndex.getOrDefault(entry.getKey(), 0);
                System.out.println("  " + dimName + ": " + count + " flower(s) [rotation: " + rotationIndex + "]");

                // 只打印前3个位置，避免日志过长
                int printed = 0;
                for (BlockPos pos : entry.getValue()) {
                    if (printed++ >= 3) {
                        System.out.println("    ... and " + (count - 3) + " more");
                        break;
                    }
                    System.out.println("    - " + pos);
                }
            }
        }
        System.out.println("=== End Registry Status ===");
    }

    // ==== NBT 序列化 ====

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag dimensionsList = new ListTag();

        for (Map.Entry<ResourceKey<Level>, Set<BlockPos>> entry : flowerPositions.entrySet()) {
            CompoundTag dimensionTag = new CompoundTag();
            dimensionTag.putString("dimension", entry.getKey().location().toString());

            ListTag positionsList = new ListTag();
            for (BlockPos pos : entry.getValue()) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("x", pos.getX());
                posTag.putInt("y", pos.getY());
                posTag.putInt("z", pos.getZ());
                positionsList.add(posTag);
            }

            dimensionTag.put("positions", positionsList);

            // 保存轮询索引
            int rotationIndex = dimensionRotationIndex.getOrDefault(entry.getKey(), 0);
            dimensionTag.putInt("rotationIndex", rotationIndex);

            dimensionsList.add(dimensionTag);
        }

        tag.put("dimensions", dimensionsList);
        System.out.println("[REGISTRY] Saved " + getTotalFlowerCount() + " flowers to NBT");
        return tag;
    }

    public static InterdimensionalFlowerRegistry load(CompoundTag tag) {
        InterdimensionalFlowerRegistry registry = new InterdimensionalFlowerRegistry();

        if (tag.contains("dimensions")) {
            ListTag dimensionsList = tag.getList("dimensions", Tag.TAG_COMPOUND);

            for (int i = 0; i < dimensionsList.size(); i++) {
                CompoundTag dimensionTag = dimensionsList.getCompound(i);

                String dimensionString = dimensionTag.getString("dimension");
                ResourceLocation dimensionLocation = new ResourceLocation(dimensionString);
                ResourceKey<Level> dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, dimensionLocation);

                Set<BlockPos> positions = ConcurrentHashMap.newKeySet();
                ListTag positionsList = dimensionTag.getList("positions", Tag.TAG_COMPOUND);

                for (int j = 0; j < positionsList.size(); j++) {
                    CompoundTag posTag = positionsList.getCompound(j);
                    BlockPos pos = new BlockPos(
                            posTag.getInt("x"),
                            posTag.getInt("y"),
                            posTag.getInt("z")
                    );
                    positions.add(pos);
                }

                if (!positions.isEmpty()) {
                    registry.flowerPositions.put(dimension, positions);

                    // 加载轮询索引
                    if (dimensionTag.contains("rotationIndex")) {
                        int rotationIndex = dimensionTag.getInt("rotationIndex");
                        registry.dimensionRotationIndex.put(dimension, rotationIndex);
                    }
                }
            }
        }

        System.out.println("[REGISTRY] Loaded " + registry.getTotalFlowerCount() + " flowers from NBT");
        registry.printRegistryStatus();
        return registry;
    }

    /**
     * 花朵位置信息类
     */
    public static class FlowerLocation {
        public final ResourceKey<Level> dimension;
        public final BlockPos position;

        public FlowerLocation(ResourceKey<Level> dimension, BlockPos position) {
            this.dimension = dimension;
            this.position = position;
        }

        @Override
        public String toString() {
            return "FlowerLocation{dimension=" + dimension.location() + ", position=" + position + "}";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            FlowerLocation that = (FlowerLocation) obj;
            return Objects.equals(dimension, that.dimension) && Objects.equals(position, that.position);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dimension, position);
        }
    }
}