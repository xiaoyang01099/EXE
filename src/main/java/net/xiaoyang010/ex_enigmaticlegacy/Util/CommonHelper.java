package net.xiaoyang010.ex_enigmaticlegacy.Util;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

import java.util.Random;

public class CommonHelper {

    public CommonHelper() {
    }

    public void fertilizer(Level world, Block block, int x, int y, int z, int count, Player player) {
        if (world.isClientSide) return;

        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = world.getBlockState(pos);

        if (block instanceof BonemealableBlock growable) {
            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, player);
            MinecraftForge.EVENT_BUS.post(event);

            if (event.isCanceled()) {
                return;
            }

            if (growable.isValidBonemealTarget(world, pos, state, world.isClientSide)) {
                if (growable.isBonemealSuccess(world, world.random, pos, state)) {
                    for (int i = 0; i < count; i++) {
                        if (growable.isBonemealSuccess(world, world.random, pos, state)) {
                            growable.performBonemeal((ServerLevel) world, world.random, pos, state);
                            break;
                        }
                    }
                }
            }
        }
    }
}