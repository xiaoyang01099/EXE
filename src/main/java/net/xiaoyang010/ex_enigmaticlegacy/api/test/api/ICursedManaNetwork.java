package net.xiaoyang010.ex_enigmaticlegacy.api.test.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * 诅咒魔力网络接口
 * 管理所有诅咒魔力相关的方块
 */
public interface ICursedManaNetwork {

    /**
     * 清空整个网络
     */
    void clear();

    /**
     * 获取最近的诅咒魔力收集器
     */
    @Nullable
    ICursedManaCollector getClosestCursedCollector(BlockPos pos, Level world, int limit);

    /**
     * 获取最近的诅咒魔力池
     */
    @Nullable
    ICursedManaPool getClosestCursedPool(BlockPos pos, Level world, int limit);

    /**
     * 获取指定世界的所有诅咒魔力收集器
     */
    Set<ICursedManaCollector> getAllCursedCollectorsInWorld(Level world);

    /**
     * 获取指定世界的所有诅咒魔力池
     */
    Set<ICursedManaPool> getAllCursedPoolsInWorld(Level world);

    /**
     * 触发网络事件（添加/移除方块）
     */
    void fireCursedManaNetworkEvent(ICursedManaReceiver thing, CursedManaBlockType type, CursedManaNetworkAction action);
}
