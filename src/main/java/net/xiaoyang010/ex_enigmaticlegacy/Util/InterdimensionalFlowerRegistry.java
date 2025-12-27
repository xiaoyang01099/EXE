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
    private final Map<ResourceKey<Level>, Set<BlockPos>> flowerPositions = new ConcurrentHashMap<>();
    private final Map<ResourceKey<Level>, Integer> dimensionRotationIndex = new ConcurrentHashMap<>();

    public InterdimensionalFlowerRegistry() {
        super();
    }

    public static InterdimensionalFlowerRegistry getInstance(MinecraftServer server) {
        var serverLevel = server.overworld();
        return serverLevel.getDataStorage().computeIfAbsent(
                InterdimensionalFlowerRegistry::load,
                InterdimensionalFlowerRegistry::new,
                DATA_NAME
        );
    }

    public synchronized void registerFlower(ResourceKey<Level> dimension, BlockPos pos) {
        Set<BlockPos> positions = flowerPositions.computeIfAbsent(dimension, k -> ConcurrentHashMap.newKeySet());
        boolean isNew = positions.add(pos);

        if (isNew) {
            setDirty();
            System.out.println("[REGISTRY] Registered new flower at " + pos + " in " + dimension.location());
            printRegistryStatus();
        }
    }

    public synchronized void unregisterFlower(ResourceKey<Level> dimension, BlockPos pos) {
        Set<BlockPos> positions = flowerPositions.get(dimension);
        if (positions != null && positions.remove(pos)) {
            if (positions.isEmpty()) {
                flowerPositions.remove(dimension);
                dimensionRotationIndex.remove(dimension);
            }
            setDirty();
            System.out.println("[REGISTRY] Unregistered flower at " + pos + " in " + dimension.location());
            printRegistryStatus();
        }
    }

    public Set<BlockPos> getFlowersInDimension(ResourceKey<Level> dimension) {
        return flowerPositions.getOrDefault(dimension, Collections.emptySet());
    }

    public FlowerLocation findTargetFlower(ResourceKey<Level> excludeDimension) {
        List<ResourceKey<Level>> availableDimensions = new ArrayList<>();

        for (ResourceKey<Level> dimension : flowerPositions.keySet()) {
            if (!dimension.equals(excludeDimension) && !flowerPositions.get(dimension).isEmpty()) {
                availableDimensions.add(dimension);
            }
        }

        if (availableDimensions.isEmpty()) {
            return null;
        }

        if (availableDimensions.size() == 1) {
            ResourceKey<Level> targetDim = availableDimensions.get(0);
            BlockPos pos = flowerPositions.get(targetDim).iterator().next();
            return new FlowerLocation(targetDim, pos);
        }

        return selectTargetByRotation(excludeDimension, availableDimensions);
    }

    private FlowerLocation selectTargetByRotation(ResourceKey<Level> sourceDimension, List<ResourceKey<Level>> availableDimensions) {
        availableDimensions.sort(Comparator.comparing(dim -> dim.location().toString()));

        int currentIndex = dimensionRotationIndex.getOrDefault(sourceDimension, 0);

        currentIndex = currentIndex % availableDimensions.size();

        ResourceKey<Level> targetDimension = availableDimensions.get(currentIndex);

        dimensionRotationIndex.put(sourceDimension, (currentIndex + 1) % availableDimensions.size());
        setDirty();

        Set<BlockPos> positions = flowerPositions.get(targetDimension);
        if (positions != null && !positions.isEmpty()) {
            BlockPos pos = positions.iterator().next();

            System.out.println("[REGISTRY] " + sourceDimension.location() + " -> " + targetDimension.location() +
                    " (rotation index: " + currentIndex + "/" + availableDimensions.size() + ")");

            return new FlowerLocation(targetDimension, pos);
        }

        return null;
    }

    public int getTotalFlowerCount() {
        return flowerPositions.values().stream().mapToInt(Set::size).sum();
    }

    public Map<ResourceKey<Level>, Set<BlockPos>> getAllFlowers() {
        return Collections.unmodifiableMap(flowerPositions);
    }

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