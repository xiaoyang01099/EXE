package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task;

import net.minecraft.world.phys.Vec3;

public class ExcaliburSpiralTask implements PostRenderTask {
    public final Vec3 anchor; // 螺旋中心锚在玩家身体中心。
    public final float ageTicks; // 蓄力年龄 tick，含 partialTick。
    public final int fullChargeTicks; // 满蓄力 tick。
    public final boolean released; // 是否释放阶段。
    public final float releaseAgeTicks; // 释放阶段年龄 tick。
    public final long seed; // 视觉随机种子。

    public ExcaliburSpiralTask(Vec3 anchor, float ageTicks, int fullChargeTicks,
                               boolean released, float releaseAgeTicks, long seed) {
        this.anchor = anchor;
        this.ageTicks = ageTicks;
        this.fullChargeTicks = Math.max(1, fullChargeTicks);
        this.released = released;
        this.releaseAgeTicks = Math.max(0.0F, releaseAgeTicks);
        this.seed = seed;
    }

    public PostRenderQueueType queueType() {
        return PostRenderQueueType.EXCALIBUR_SPIRAL;
    }

    // 创建咖喱棒玩家中心螺旋任务。
    public static ExcaliburSpiralTask create(Vec3 anchor, float ageTicks, int fullChargeTicks,
                                             boolean released, float releaseAgeTicks, long seed) {
        return new ExcaliburSpiralTask(anchor, ageTicks, fullChargeTicks, released, releaseAgeTicks, seed);
    }
}
