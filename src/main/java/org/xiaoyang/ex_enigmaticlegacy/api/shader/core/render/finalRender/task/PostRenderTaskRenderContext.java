package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.PostRenderContext;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.PostRenderPhase;

public class PostRenderTaskRenderContext {
    public final PostRenderContext postRenderContext; // 后处理 GL 状态入口。
    public final MultiBufferSource.BufferSource fboBuffer; // MRT RenderType 批处理缓冲。
    public final Camera camera; // 当前渲染相机。
    public final float partialTick; // 当前帧插值。
    public final Matrix4f viewMatrix; // 当前相机 view 矩阵。
    public final float frameDeltaSeconds; // 当前帧后处理时间步长。
    public final int sceneColorTextureId; // 进入后处理前的主场景颜色纹理。
    public final int sceneDepthTextureId; // 进入后处理前的主场景深度纹理。
    public final int screenWidth; // 当前 FBO 宽度。
    public final int screenHeight; // 当前 FBO 高度。

    public PostRenderTaskRenderContext(PostRenderContext postRenderContext, MultiBufferSource.BufferSource fboBuffer,
                                       Camera camera, float partialTick, Matrix4f viewMatrix, float frameDeltaSeconds,
                                       int sceneColorTextureId, int sceneDepthTextureId, int screenWidth, int screenHeight) {
        this.postRenderContext = postRenderContext;
        this.fboBuffer = fboBuffer;
        this.camera = camera;
        this.partialTick = partialTick;
        this.viewMatrix = viewMatrix;
        this.frameDeltaSeconds = frameDeltaSeconds;
        this.sceneColorTextureId = sceneColorTextureId;
        this.sceneDepthTextureId = sceneDepthTextureId;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void prepareRenderTypePhase(PostRenderPhase phase) {
        if (postRenderContext == null) return;
        if (phase == PostRenderPhase.ALWAYS_VISIBLE_WORLD) {
            postRenderContext.prepareRenderTypePhase(false, false, GL11.GL_ALWAYS);
            return;
        }
        postRenderContext.prepareRenderTypePhase(true, false, GL11.GL_LEQUAL);
    }
}
