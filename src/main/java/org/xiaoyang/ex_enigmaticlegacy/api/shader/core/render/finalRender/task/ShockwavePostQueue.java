package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task;

import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.PostRenderPhase;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue.ShockwaveQueue;


public class ShockwavePostQueue implements PostRenderTaskQueue<ShockwaveTask> {
    public final ShockwaveQueue queue;

    public ShockwavePostQueue(ShockwaveQueue queue) {
        this.queue = queue;
    }

    public PostRenderQueueType queueType() {
        return PostRenderQueueType.SHOCKWAVE;
    }

    public PostRenderPhase phase() {
        return PostRenderPhase.DEPTH_TESTED_WORLD;
    }

    public void add(ShockwaveTask task) {
        if (task == null || queue == null) return;
        queue.add(task.center, task.normal, task.startRadius, task.endRadius,
                task.growTime, task.holdTime, task.fadeTime, task.width, task.seed, task.alpha);
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
