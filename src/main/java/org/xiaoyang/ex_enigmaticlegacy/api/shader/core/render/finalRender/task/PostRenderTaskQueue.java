package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task;

import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.PostRenderPhase;

public interface PostRenderTaskQueue<T extends PostRenderTask> {
    PostRenderQueueType queueType();
    PostRenderPhase phase();
    void add(T task);
    boolean hasActive();
    void render(PostRenderTaskRenderContext context);
    void clear();
}
