package org.xiaoyang.ex_enigmaticlegacy.api.test.curse.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Set;

public interface ICursedManaNetwork {

    void clear();

    @Nullable
    ICursedManaCollector getClosestCursedCollector(BlockPos pos, Level world, int limit);

    @Nullable
    ICursedManaPool getClosestCursedPool(BlockPos pos, Level world, int limit);

    Set<ICursedManaCollector> getAllCursedCollectorsInWorld(Level world);

    Set<ICursedManaPool> getAllCursedPoolsInWorld(Level world);

    void fireCursedManaNetworkEvent(ICursedManaReceiver thing, CursedManaBlockType type, CursedManaNetworkAction action);
}
