package net.xiaoyang010.ex_enigmaticlegacy.Block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CustomTreeGenerator {

    private static class TreeType {
        String name;
        TreeGenerator generator;
        int blockCount;
        double weight;

        TreeType(String name, TreeGenerator generator, int blockCount) {
            this.name = name;
            this.generator = generator;
            this.blockCount = blockCount;
        }
    }

    @FunctionalInterface
    private interface TreeGenerator {
        boolean generate(LevelAccessor level, BlockPos pos, Block material);
    }

    public static boolean generateTree(LevelAccessor level, BlockPos pos, Block material) {
        if (material == Blocks.AIR || material == Blocks.WATER || material == Blocks.LAVA || material == Blocks.FIRE) {
            return false;
        }

        TreeType[] treeTypes = new TreeType[] {
                new TreeType("Default tree", CustomTreeGenerator::generateDefaultTree, 32),
                new TreeType("Birch like", CustomTreeGenerator::generateBirchTree, 38),
                new TreeType("Acacia like", CustomTreeGenerator::generateAcaciaTree, 35),
                new TreeType("Mega Spruce", CustomTreeGenerator::generateMegaSpruceTree, 85),
                new TreeType("Mega Jungle", CustomTreeGenerator::generateMegaJungleTree, 95)
        };

        double totalWeight = 0;
        for (TreeType type : treeTypes) {
            if (type.name.equals("Mega Jungle")) {
                type.weight = 0.08;
            } else if (type.name.equals("Mega Spruce")) {
                type.weight = 0.02;
            } else {
                type.weight = 0.9 * ((double)(95 - type.blockCount) / 95);
            }
            totalWeight += type.weight;
        }

        for (TreeType type : treeTypes) {
            type.weight = type.weight / totalWeight;
        }

        double randomValue = Math.random();
        double currentWeight = 0;

        for (TreeType type : treeTypes) {
            currentWeight += type.weight;
            if (randomValue <= currentWeight) {
                return type.generator.generate(level, pos, material);
            }
        }

        return generateDefaultTree(level, pos, material);
    }

    private static boolean generateDefaultTree(LevelAccessor level, BlockPos pos, Block material) {
        BlockState blockState = material.defaultBlockState();

        for (int y = 0; y < 5; y++) {
            if (!canPlace(level, pos.above(y))) return false;
            placeBlock(level, pos.above(y), blockState);
        }

        boolean placedAny = false;
        for (int y = 3; y <= 5; y++) {
            int radius = y == 5 ? 1 : 2;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (y == 5 && (Math.abs(x) == 2 || Math.abs(z) == 2)) continue;
                    if (Math.abs(x) == radius && Math.abs(z) == radius) continue;

                    BlockPos leafPos = pos.offset(x, y, z);
                    if (canPlace(level, leafPos)) {
                        placeBlock(level, leafPos, blockState);
                        placedAny = true;
                    }
                }
            }
        }

        if (canPlace(level, pos.above(6))) {
            placeBlock(level, pos.above(6), blockState);
            placedAny = true;
        }

        return placedAny;
    }

    private static boolean generateBirchTree(LevelAccessor level, BlockPos pos, Block material) {
        BlockState blockState = material.defaultBlockState();
        int height = 7;

        for (int y = 0; y < height; y++) {
            if (!canPlace(level, pos.above(y))) return false;
            placeBlock(level, pos.above(y), blockState);
        }

        boolean placedAny = false;
        for (int y = height - 3; y <= height; y++) {
            int radius = (y == height) ? 1 : 2;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) == 2 && Math.abs(z) == 2) {
                        if (Math.random() < 0.5) continue;
                    }
                    BlockPos leafPos = pos.offset(x, y, z);
                    if (canPlace(level, leafPos)) {
                        placeBlock(level, leafPos, blockState);
                        placedAny = true;
                    }
                }
            }
        }
        return placedAny;
    }

    private static boolean generateAcaciaTree(LevelAccessor level, BlockPos pos, Block material) {
        BlockState blockState = material.defaultBlockState();
        int height = 5;

        for (int y = 0; y < height - 2; y++) {
            if (!canPlace(level, pos.above(y))) return false;
            placeBlock(level, pos.above(y), blockState);
        }

        boolean placedAny = false;
        int xOffset = Math.random() < 0.5 ? 1 : -1;
        int zOffset = Math.random() < 0.5 ? 1 : -1;

        for (int y = height - 2; y <= height; y++) {
            BlockPos branchPos = pos.offset(xOffset * (y - (height - 2)), y, zOffset * (y - (height - 2)));
            if (!canPlace(level, branchPos)) continue;
            placeBlock(level, branchPos, blockState);
            placedAny = true;
        }

        xOffset *= -1;
        for (int y = height - 2; y <= height; y++) {
            BlockPos branchPos = pos.offset(xOffset * (y - (height - 2)), y, zOffset * (y - (height - 2)));
            if (!canPlace(level, branchPos)) continue;
            placeBlock(level, branchPos, blockState);
            placedAny = true;
        }

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) + Math.abs(z) > 3) continue;

                BlockPos leaf1 = pos.offset(x + xOffset, height, z + zOffset);
                BlockPos leaf2 = pos.offset(x - xOffset, height, z + zOffset);

                if (canPlace(level, leaf1)) {
                    placeBlock(level, leaf1, blockState);
                    placedAny = true;
                }
                if (canPlace(level, leaf2)) {
                    placeBlock(level, leaf2, blockState);
                    placedAny = true;
                }
            }
        }

        return placedAny;
    }

    private static boolean generateMegaSpruceTree(LevelAccessor level, BlockPos pos, Block material) {
        BlockState blockState = material.defaultBlockState();
        int height = 20 + (int)(Math.random() * 5);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < 2; x++) {
                for (int z = 0; z < 2; z++) {
                    if (!canPlace(level, pos.offset(x, y, z))) return false;
                    placeBlock(level, pos.offset(x, y, z), blockState);
                }
            }
        }

        boolean placedAny = false;

        for (int y = height/2; y < height; y++) {
            int radius = (height - y) / 2 + 1;
            for (int x = -radius; x <= radius + 1; x++) {
                for (int z = -radius; z <= radius + 1; z++) {
                    if (Math.abs(x) == radius && Math.abs(z) == radius) {
                        if (Math.random() < 0.5) continue;
                    }
                    BlockPos leafPos = pos.offset(x, y, z);
                    if (canPlace(level, leafPos)) {
                        placeBlock(level, leafPos, blockState);
                        placedAny = true;
                    }
                }
            }
        }
        return placedAny;
    }

    private static boolean generateMegaJungleTree(LevelAccessor level, BlockPos pos, Block material) {
        BlockState blockState = material.defaultBlockState();
        int height = 25 + (int)(Math.random() * 5);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < 2; x++) {
                for (int z = 0; z < 2; z++) {
                    if (!canPlace(level, pos.offset(x, y, z))) return false;
                    placeBlock(level, pos.offset(x, y, z), blockState);
                }
            }
        }

        boolean placedAny = false;

        for (int y = 5; y < height - 5; y += 3) {
            if (Math.random() < 0.3) {
                int xOffset = Math.random() < 0.5 ? 2 : -2;
                int zOffset = Math.random() < 0.5 ? 2 : -2;

                for (int i = 0; i < 3; i++) {
                    BlockPos branchPos = pos.offset(xOffset, y + i, zOffset);
                    if (canPlace(level, branchPos)) {
                        placeBlock(level, branchPos, blockState);
                        placedAny = true;
                    }
                }
            }
        }

        for (int y = height - 5; y <= height; y++) {
            int radius = 4;
            for (int x = -radius; x <= radius + 1; x++) {
                for (int z = -radius; z <= radius + 1; z++) {
                    if (x * x + z * z > radius * radius + 2) continue;
                    BlockPos leafPos = pos.offset(x, y, z);
                    if (canPlace(level, leafPos)) {
                        placeBlock(level, leafPos, blockState);
                        placedAny = true;
                    }
                }
            }
        }

        return placedAny;
    }

    private static boolean canPlace(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos).isAir() || level.getBlockState(pos).getMaterial().isReplaceable();
    }

    private static void placeBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 3);
    }
}