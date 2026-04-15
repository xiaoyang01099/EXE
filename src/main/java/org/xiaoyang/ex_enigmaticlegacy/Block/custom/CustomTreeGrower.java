package org.xiaoyang.ex_enigmaticlegacy.Block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

import javax.annotation.Nullable;
import java.util.Random;

public class CustomTreeGrower extends AbstractTreeGrower {
    @Override
    protected @Nullable ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource p_222910_, boolean p_222911_) {
        return null;
    }

    @Override
    public boolean growTree(ServerLevel level, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, RandomSource source) {
        BlockState belowState = level.getBlockState(pos.below());

        if (!hasGrowthSpace(level, pos)) {
            return false;
        }

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 4);
        if (CustomTreeGenerator.generateTree(level, pos, belowState.getBlock())) {
            return true;
        } else {
            level.setBlock(pos, state, 4);
            return false;
        }
    }

    private boolean hasGrowthSpace(ServerLevel level, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                BlockPos checkPos = pos.offset(x, 0, z);
                BlockState checkState = level.getBlockState(checkPos);
                if (checkState.getBlock() instanceof SaplingBlock) {
                    return false;
                }
            }
        }

        for (int y = 1; y <= 6; y++) {
            BlockPos checkPos = pos.above(y);
            BlockState checkState = level.getBlockState(checkPos);
            if (!checkState.isAir() && !checkState.canBeReplaced()) {
                return false;
            }
        }

        return true;
    }
}