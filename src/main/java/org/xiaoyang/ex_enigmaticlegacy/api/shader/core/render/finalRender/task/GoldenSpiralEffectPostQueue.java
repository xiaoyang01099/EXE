package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task;


import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.PostRenderPhase;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue.GoldenSpiralEffectQueue;

public class GoldenSpiralEffectPostQueue implements PostRenderTaskQueue<GoldenSpiralEffectTask> {
    public final GoldenSpiralEffectQueue queue;

    public GoldenSpiralEffectPostQueue(GoldenSpiralEffectQueue queue) {
        this.queue = queue;
    }

    public PostRenderQueueType queueType() {
        return PostRenderQueueType.GOLDEN_SPIRAL_EFFECT;
    }

    public PostRenderPhase phase() {
        return PostRenderPhase.DEPTH_TESTED_WORLD;
    }

    public void add(GoldenSpiralEffectTask task) {
        if (task == null || queue == null) return;
        queue.add(task.center, task.seed);
    }

    public boolean hasActive() {
        return queue != null && queue.hasActive();
    }

    public void render(PostRenderTaskRenderContext context) {
        if (queue == null || context == null) return;
        queue.render(context.fboBuffer, context.camera, context.partialTick, context.viewMatrix);
    }

    public void clear() {
        if (queue == null) return;
        queue.clear();
    }
}
