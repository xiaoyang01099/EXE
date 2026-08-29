package org.xiaoyang.ex_enigmaticlegacy.Util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.level.BlockEvent;

public class BlockUtil {

    public static Boolean isPlaceBlock(Level world, Entity player, BlockPos blockPos){

        BlockSnapshot blockSnapshot = BlockSnapshot.create(world.dimension(), world, blockPos);
        BlockState blockState = world.getBlockState(blockPos);
        BlockEvent.EntityPlaceEvent entityPlaceEvent = new BlockEvent.EntityPlaceEvent(blockSnapshot, blockState, player);
        MinecraftForge.EVENT_BUS.post(entityPlaceEvent);
        return !entityPlaceEvent.isCanceled();
    }
    public static Boolean isPlaceBlock(Level world, Entity player, BlockPos blockPos, BlockState blockState){

        BlockSnapshot blockSnapshot = BlockSnapshot.create(world.dimension(), world, blockPos);
        BlockEvent.EntityPlaceEvent entityPlaceEvent = new BlockEvent.EntityPlaceEvent(blockSnapshot, blockState, player);
        MinecraftForge.EVENT_BUS.post(entityPlaceEvent);
        return !entityPlaceEvent.isCanceled();
    }

    public static Boolean isPlaceBlock(Level world, Entity player, BlockPos pos1, BlockPos pos2, BlockState blockState){
        int maxY = Math.max(pos1.getY(), pos2.getY());
//        int minY = Math.min(pos1.getY(), pos2.getY());
//        int y = maxY/2;
        int y = maxY;

        int maxX =  Math.max(pos1.getX(), pos2.getX());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());

        for (int x = minX; x <=maxX; x+=15) {
            for (int z = minZ; z <=maxZ; z+=15)  {
                if(!isPlaceBlock(world, player, new BlockPos(x, y, z), blockState))return false;
            }
        }

        for (int z = minZ; z <=maxZ; z+=15)  {
            if(!isPlaceBlock(world, player, new BlockPos(maxX, y, z), blockState))return false;
        }

        for (int x = minX; x <=maxX; x+=15) {
            if(!isPlaceBlock(world, player, new BlockPos(x, y, maxZ), blockState))return false;
        }
        if(!isPlaceBlock(world, player, new BlockPos(maxX, y, maxZ), blockState))return false;
        return true;
    }
}
