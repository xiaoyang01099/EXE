package net.xiaoyang010.ex_enigmaticlegacy.api.test.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 诅咒魔力网络实现
 */
public class CursedManaNetwork implements ICursedManaNetwork {
    private static final CursedManaNetwork INSTANCE = new CursedManaNetwork();

    public static CursedManaNetwork getInstance() {
        return INSTANCE;
    }

    private final Set<WeakReference<ICursedManaCollector>> collectors =
            Collections.newSetFromMap(new WeakHashMap<>());
    private final Set<WeakReference<ICursedManaPool>> pools =
            Collections.newSetFromMap(new WeakHashMap<>());

    private CursedManaNetwork() {}

    public boolean isCollectorIn(Level level, ICursedManaCollector collector) {
        return collectors.stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
                .anyMatch(c -> c == collector && c.getCursedManaReceiverLevel() == level);
    }

    public boolean isPoolIn(Level level, ICursedManaPool pool) {
        return pools.stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
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
        return collectors.stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
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
        return pools.stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
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
        return collectors.stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
                .filter(c -> c.getCursedManaReceiverLevel() == world)
                .collect(Collectors.toSet());
    }
    @Override
    public Set<ICursedManaPool> getAllCursedPoolsInWorld(Level world) {
        return pools.stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
                .filter(p -> p.getCursedManaReceiverLevel() == world)
                .collect(Collectors.toSet());
    }

    @Override
    public void fireCursedManaNetworkEvent(ICursedManaReceiver thing, CursedManaBlockType type, CursedManaNetworkAction action) {
        switch (type) {
            case COLLECTOR:
                if (thing instanceof ICursedManaCollector collector) {
                    if (action == CursedManaNetworkAction.ADD) {
                        collectors.add(new WeakReference<>(collector));
                    } else {
                        collectors.removeIf(ref -> {
                            ICursedManaCollector c = ref.get();
                            return c == null || c == collector;
                        });
                    }
                }
                break;
            case POOL:
                if (thing instanceof ICursedManaPool pool) {
                    if (action == CursedManaNetworkAction.ADD) {
                        pools.add(new WeakReference<>(pool));
                    } else {
                        pools.removeIf(ref -> {
                            ICursedManaPool p = ref.get();
                            return p == null || p == pool;
                        });
                    }
                }
                break;
        }
        MinecraftForge.EVENT_BUS.post(new CursedManaNetworkEvent(thing, type, action));
    }

    public void clearWorld(Level world) {
        collectors.removeIf(ref -> {
            ICursedManaCollector c = ref.get();
            return c == null || c.getCursedManaReceiverLevel() == world;
        });
        pools.removeIf(ref -> {
            ICursedManaPool p = ref.get();
            return p == null || p.getCursedManaReceiverLevel() == world;
        });
    }
}
