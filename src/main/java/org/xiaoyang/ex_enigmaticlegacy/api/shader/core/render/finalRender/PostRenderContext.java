package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.frameBuffer.FBO;

import java.util.Arrays;

public class PostRenderContext {
    public int boundFramebuffer = -1;
    public int[] drawAttachments = new int[0];
    public boolean depthTestEnabled = false;
    public boolean depthMaskEnabled = false;
    public int depthFunc = GL11.GL_LEQUAL;
    public int minecraftVao = 0;

    public void bindFrameBuffer(FBO fbo, boolean clearDepth, int... colorAttachmentsToClear) {
        if (fbo == null) return;
        fbo.bindFrameBuffer(clearDepth, colorAttachmentsToClear);
        boundFramebuffer = fbo.getFrameBuffer();
        drawAttachments = colorAttachmentsToClear == null ? new int[0] : Arrays.copyOf(colorAttachmentsToClear, colorAttachmentsToClear.length);
    }

    public void bindMinecraftFrameBuffer(FBO currentFbo, RenderTarget renderTarget) {
        if (currentFbo == null || renderTarget == null) return;
        currentFbo.unbindFrameBuffer(renderTarget.frameBufferId);
        boundFramebuffer = renderTarget.frameBufferId;
        drawAttachments = new int[0];
    }

    public void setDrawBuffer(FBO fbo, int attachment) {
        if (fbo == null) return;
        if (drawAttachments.length == 1 && drawAttachments[0] == attachment) return;
        fbo.setDrawBuffer(attachment);
        drawAttachments = new int[]{attachment};
    }

    public void setDrawBuffers(FBO fbo, int... attachments) {
        if (fbo == null || attachments == null) return;
        if (Arrays.equals(drawAttachments, attachments)) return;
        fbo.setDrawBuffers(attachments);
        drawAttachments = Arrays.copyOf(attachments, attachments.length);
    }

    public void clearColorAttachment(FBO fbo, int attachment, float red, float green, float blue, float alpha) {
        if (fbo == null) return;
        fbo.clearColorAttachment(attachment, red, green, blue, alpha);
        drawAttachments = new int[]{attachment};
    }

    public void setDepthState(boolean depthTest, boolean depthMask, int newDepthFunc) {
        if (depthTest) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        } else {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }
        GL11.glDepthMask(depthMask);
        GL11.glDepthFunc(newDepthFunc);
        depthTestEnabled = depthTest;
        depthMaskEnabled = depthMask;
        depthFunc = newDepthFunc;
    }

    public void beginFrame(int minecraftVao) {
        this.minecraftVao = minecraftVao;
    }

    public void prepareMinecraftBufferSource() {
        GL20.glUseProgram(0);
        if (minecraftVao != 0) {
            GL30.glBindVertexArray(minecraftVao);
        }
    }

    public void prepareRenderTypePhase(boolean depthTest, boolean depthMask, int newDepthFunc) {
        prepareMinecraftBufferSource();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        setDepthState(depthTest, depthMask, newDepthFunc);
    }
}
