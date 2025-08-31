package net.xiaoyang010.ex_enigmaticlegacy.Util;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;


public class CommonHelper {

    public CommonHelper() {
    }

    public static void fertilizer(Level world, Block block, int x, int y, int z, int count, Player player) {
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
                for (int i = 0; i < count; i++) {
                    if (growable.isBonemealSuccess(world, world.random, pos, state)) {
                        if (world instanceof ServerLevel serverLevel) {
                            growable.performBonemeal(serverLevel, world.random, pos, state);
                        }
                        break;
                    }
                }
            }
        }
    }

    public static boolean setBlock(Level world, Block block, int meta, int x, int y, int z, Player player, boolean checkAir) {
        if (world.isClientSide) return false;

        BlockPos pos = new BlockPos(x, y, z);
        BlockState currentState = world.getBlockState(pos);

        if (block.defaultBlockState().getMaterial() == Material.AIR) {
            return false;
        }

        if (checkAir && !currentState.isAir()) {
            return false;
        }

        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, currentState, player);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            return false;
        }

        return setBlockWithMeta(world, block, x, y, z, meta);
    }

    public static boolean setBlockWithMeta(Level world, Block block, int x, int y, int z, int meta) {
        if (world.isClientSide) return false;

        BlockPos pos = new BlockPos(x, y, z);
        BlockState newState = block.defaultBlockState();

        return world.setBlock(pos, newState, 3);
    }
}