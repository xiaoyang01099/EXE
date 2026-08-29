package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue;

import net.minecraft.client.resources.model.BakedModel;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Item.FlySwordHeldItemRenderer;

public class FlySwordHeldModelState {
    public final BakedModel model;
    public final Matrix4f modelViewMatrix;
    public final boolean plusSword;
    public final long submitGameTime;
    public final FlySwordHeldItemRenderer.FlySwordFlowParams flowParams;

    public FlySwordHeldModelState(BakedModel model, Matrix4f modelViewMatrix,
                                  boolean plusSword, long submitGameTime, FlySwordHeldItemRenderer.FlySwordFlowParams flowParams) {
        this.model = model;
        this.modelViewMatrix = new Matrix4f(modelViewMatrix);
        this.plusSword = plusSword;
        this.submitGameTime = submitGameTime;
        this.flowParams = flowParams;
    }

    public boolean isExpired(long gameTime, long maxAgeTicks) {
        return gameTime - submitGameTime > maxAgeTicks;
    }
}
