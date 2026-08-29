package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

public final class ParticleOwnership {
    public static final long NO_OWNER = Long.MIN_VALUE;
    private static final ThreadLocal<Long> SPAWN_OWNER = ThreadLocal.withInitial(() -> NO_OWNER);

    private ParticleOwnership() {
    }

    static void spawnOwned(long roundId, Runnable spawner) {
        long previousOwner = SPAWN_OWNER.get();
        SPAWN_OWNER.set(roundId);
        try {
            spawner.run();
        } finally {
            SPAWN_OWNER.set(previousOwner);
        }
    }

    public static long claimSpawnOwner() {
        return SPAWN_OWNER.get();
    }

    public static float visibility(long roundId, float partialTick) {
        if (roundId == NO_OWNER) return 1.0f;
        return ClientEffects.worldVisibility(roundId, partialTick);
    }

    public static boolean isRenderFrozen(long roundId) {
        return roundId != NO_OWNER && PauseRenderState.isFrozen();
    }

    public static float partialTick(long roundId, float partialTick) {
        return roundId == NO_OWNER ? partialTick : PauseRenderState.partialTick(partialTick);
    }
}
