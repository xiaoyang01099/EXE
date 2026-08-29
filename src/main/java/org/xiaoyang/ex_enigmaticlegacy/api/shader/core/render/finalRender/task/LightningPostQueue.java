package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task;


import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.PostRenderPhase;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue.CoinLightningQueue;

public class LightningPostQueue implements PostRenderTaskQueue<LightningTask> {
    public final CoinLightningQueue queue;

    public LightningPostQueue(CoinLightningQueue queue) {
        this.queue = queue;
    }

    public PostRenderQueueType queueType() {
        return PostRenderQueueType.LIGHTNING;
    }

    public PostRenderPhase phase() {
        return PostRenderPhase.DEPTH_TESTED_WORLD;
    }

    public void add(LightningTask task) {
        if (task == null || queue == null) return;
        if (task.kind == LightningTask.Kind.CHARGING) {
            queue.addChargingLightning(task.player, task.chargeProgress, task.chargingPartialTick, task.colorful);
            return;
        }
        queue.addLightning(task.lightning);
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
