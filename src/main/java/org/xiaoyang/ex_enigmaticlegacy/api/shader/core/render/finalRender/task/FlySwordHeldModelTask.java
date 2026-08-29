package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task;

import net.minecraft.client.resources.model.BakedModel;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Item.FlySwordHeldItemRenderer;

public class FlySwordHeldModelTask implements PostRenderTask {
    public final BakedModel model;
    public final Matrix4f modelViewMatrix;
    public final boolean plusSword;
    public final long gameTime;
    public final FlySwordHeldItemRenderer.FlySwordFlowParams flowParams;

    public FlySwordHeldModelTask(BakedModel model, Matrix4f modelViewMatrix,
                                 boolean plusSword, long gameTime, FlySwordHeldItemRenderer.FlySwordFlowParams flowParams) {
        this.model = model;
        this.modelViewMatrix = modelViewMatrix;
        this.plusSword = plusSword;
        this.gameTime = gameTime;
        this.flowParams = flowParams;
    }

    public PostRenderQueueType queueType() {
        return PostRenderQueueType.FLY_SWORD_HELD_MODEL;
    }
}
