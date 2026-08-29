package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task;

import net.minecraft.world.phys.Vec3;

public class GoldenSpiralEffectTask implements PostRenderTask {
    public final Vec3 center;
    public final long seed;

    public GoldenSpiralEffectTask(Vec3 center, long seed) {
        this.center = center;
        this.seed = seed;
    }

    public PostRenderQueueType queueType() {
        return PostRenderQueueType.GOLDEN_SPIRAL_EFFECT;
    }

    public static GoldenSpiralEffectTask create(Vec3 center, long seed) {
        return new GoldenSpiralEffectTask(center, seed);
    }
}
