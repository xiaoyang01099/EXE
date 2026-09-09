package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task;


import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.PostRenderPhase;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue.ExcaliburSpiralQueue;

public class ExcaliburSpiralPostQueue implements PostRenderTaskQueue<ExcaliburSpiralTask> {
    public final ExcaliburSpiralQueue queue;

    public ExcaliburSpiralPostQueue(ExcaliburSpiralQueue queue) {
        this.queue = queue;
    }

    public PostRenderQueueType queueType() {
        return PostRenderQueueType.EXCALIBUR_SPIRAL;
    }

    public PostRenderPhase phase() {
        return PostRenderPhase.ALWAYS_VISIBLE_WORLD;
    }

    public void add(ExcaliburSpiralTask task) {
        if (task == null || queue == null) return;
        queue.add(task);
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
