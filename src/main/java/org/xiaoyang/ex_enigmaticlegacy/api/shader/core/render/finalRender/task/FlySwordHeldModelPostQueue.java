package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task;


import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.PostRenderPhase;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue.FlySwordHeldModelQueue;

public class FlySwordHeldModelPostQueue implements PostRenderTaskQueue<FlySwordHeldModelTask> {
    public final FlySwordHeldModelQueue queue;

    public FlySwordHeldModelPostQueue(FlySwordHeldModelQueue queue) {
        this.queue = queue;
    }

    public PostRenderQueueType queueType() {
        return PostRenderQueueType.FLY_SWORD_HELD_MODEL;
    }

    public PostRenderPhase phase() {
        return PostRenderPhase.DEPTH_TESTED_WORLD;
    }

    public void add(FlySwordHeldModelTask task) {
        if (task == null || queue == null) return;
        queue.submit(task.model, task.modelViewMatrix, task.plusSword, task.gameTime, task.flowParams);
    }

    public boolean hasActive() {
        return queue != null && queue.hasActive();
    }

    public void render(PostRenderTaskRenderContext context) {
        if (queue == null || context == null) return;
        queue.render(context.fboBuffer, context.partialTick,
                context.sceneColorTextureId, context.screenWidth, context.screenHeight);
    }

    public void clear() {
        if (queue == null) return;
        queue.clear();
    }
}
