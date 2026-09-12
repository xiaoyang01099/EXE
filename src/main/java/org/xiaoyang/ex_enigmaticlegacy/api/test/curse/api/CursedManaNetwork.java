package org.xiaoyang.ex_enigmaticlegacy.api.test.curse.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class CursedManaNetwork implements ICursedManaNetwork {
    private static final CursedManaNetwork INSTANCE = new CursedManaNetwork();
    public static CursedManaNetwork getInstance() {
        return INSTANCE;
    }
    private final Set<ICursedManaCollector> collectors = Collections.newSetFromMap(new WeakHashMap<>());
    private final Set<ICursedManaPool> pools = Collections.newSetFromMap(new WeakHashMap<>());

    private CursedManaNetwork() {}

    public boolean isCollectorIn(Level level, ICursedManaCollector collector) {
        return collectors.stream()
                .anyMatch(c -> c == collector && c.getCursedManaReceiverLevel() == level);
    }

    public boolean isPoolIn(Level level, ICursedManaPool pool) {
        return pools.stream()
                .anyMatch(p -> p == pool && p.getCursedManaReceiverLevel() == level);
    }

    @Override
    public void clear() {
        collectors.clear();
        pools.clear();
    }

    @Override
    @Nullable
    public ICursedManaCollector getClosestCursedCollector(BlockPos pos, Level world, int limit) {
        if (pos == null || world == null || limit < 0) return null;
        return collectors.stream()
                .filter(c -> c.getCursedManaReceiverLevel() == world)
                .filter(c -> {
                    BlockPos collectorPos = c.getCursedManaReceiverPos();
                    return collectorPos.closerThan(pos, limit);
                })
                .min(Comparator.comparingDouble(c ->
                        c.getCursedManaReceiverPos().distSqr(pos)))
                .orElse(null);
    }

    @Override
    @Nullable
    public ICursedManaPool getClosestCursedPool(BlockPos pos, Level world, int limit) {
        if (pos == null || world == null || limit < 0) return null;
        return pools.stream()
                .filter(p -> p.getCursedManaReceiverLevel() == world)
                .filter(p -> {
                    BlockPos poolPos = p.getCursedManaReceiverPos();
                    return poolPos.closerThan(pos, limit);
                })
                .min(Comparator.comparingDouble(p ->
                        p.getCursedManaReceiverPos().distSqr(pos)))
                .orElse(null);
    }

    @Override
    public Set<ICursedManaCollector> getAllCursedCollectorsInWorld(Level world) {
        if (world == null) return Collections.emptySet();
        return collectors.stream()
                .filter(c -> c.getCursedManaReceiverLevel() == world)
                .collect(Collectors.toSet());
    }
    @Override
    public Set<ICursedManaPool> getAllCursedPoolsInWorld(Level world) {
        if (world == null) return Collections.emptySet();
        return pools.stream()
                .filter(p -> p.getCursedManaReceiverLevel() == world)
                .collect(Collectors.toSet());
    }

    @Override
    public void fireCursedManaNetworkEvent(ICursedManaReceiver thing, CursedManaBlockType type, CursedManaNetworkAction action) {
        switch (type) {
            case COLLECTOR:
                if (thing instanceof ICursedManaCollector collector) {
                    if (action == CursedManaNetworkAction.ADD) {
                        collectors.add(collector);
                    } else {
                        collectors.remove(collector);
                    }
                }
                break;
            case POOL:
                if (thing instanceof ICursedManaPool pool) {
                    if (action == CursedManaNetworkAction.ADD) {
                        pools.add(pool);
                    } else {
                        pools.remove(pool);
                    }
                }
                break;
        }
        MinecraftForge.EVENT_BUS.post(new CursedManaNetworkEvent(thing, type, action));
    }

    public void clearWorld(Level world) {
        collectors.removeIf(c -> c.getCursedManaReceiverLevel() == world);
        pools.removeIf(p -> p.getCursedManaReceiverLevel() == world);
    }
}
