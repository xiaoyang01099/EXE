package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.frameBuffer.FBO;

import java.util.Objects;

public class FrameBufferUtil {
    public static void copyFBO(RenderTarget sourceFBO, FBO targetFBO) {
        if (sourceFBO == null || targetFBO == null) return;

        int width = targetFBO.getWidth();
        int height = targetFBO.getHeight();
        int previousReadFbo = GL30.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int previousDrawFbo = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int previousReadBuffer = GL11.glGetInteger(GL11.GL_READ_BUFFER);
        int previousDrawBuffer = GL11.glGetInteger(GL11.GL_DRAW_BUFFER);

        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, sourceFBO.frameBufferId);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, targetFBO.getFrameBuffer());
        GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GL30.glBlitFramebuffer(0, 0, width, height,
                0, 0, width, height,
                GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

        if (canBlitDepth(sourceFBO, targetFBO)) {
            GL30.glBlitFramebuffer(0, 0, width, height,
                    0, 0, width, height,
                    GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
        }

        restoreBlitState(previousReadFbo, previousDrawFbo, previousReadBuffer, previousDrawBuffer);
    }

    public static void copyFBODepth(RenderTarget sourceFBO, FBO targetFBO) {
        if (sourceFBO == null || targetFBO == null) return;
        if (!canBlitDepth(sourceFBO, targetFBO)) return;

        int width = targetFBO.getWidth();
        int height = targetFBO.getHeight();
        int previousReadFbo = GL30.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int previousDrawFbo = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int previousReadBuffer = GL11.glGetInteger(GL11.GL_READ_BUFFER);
        int previousDrawBuffer = GL11.glGetInteger(GL11.GL_DRAW_BUFFER);

        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, sourceFBO.frameBufferId);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, targetFBO.getFrameBuffer());
        GL30.glBlitFramebuffer(0, 0, width, height,
                0, 0, width, height,
                GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);

        restoreBlitState(previousReadFbo, previousDrawFbo, previousReadBuffer, previousDrawBuffer);
    }

    public static void copyColorAttachments(FBO sourceFBO, FBO targetFBO) {
        int attachmentCount = Math.min(sourceFBO.getColorAttachmentCount(), targetFBO.getColorAttachmentCount());
        copyColorAttachments(sourceFBO.getFrameBuffer(), targetFBO.getFrameBuffer(),
                Minecraft.getInstance().getWindow().getWidth(),
                Minecraft.getInstance().getWindow().getHeight(),
                attachmentCount);
    }

    public static void copyColorAttachments(int sourceFramebuffer, int targetFramebuffer,
                                            int width, int height, int attachmentCount) {
        int previousReadFbo = GL30.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int previousDrawFbo = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int previousReadBuffer = GL11.glGetInteger(GL11.GL_READ_BUFFER);
        int previousDrawBuffer = GL11.glGetInteger(GL11.GL_DRAW_BUFFER);

        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, sourceFramebuffer);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, targetFramebuffer);

        // glBlitFramebuffer 只复制当前 read/draw attachment，MRT 必须逐个附件复制。
        for (int i = 0; i < attachmentCount; i++) {
            GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0 + i);
            GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0 + i);
            GL30.glBlitFramebuffer(0, 0, width, height,
                    0, 0, width, height,
                    GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        }

        restoreBlitState(previousReadFbo, previousDrawFbo, previousReadBuffer, previousDrawBuffer);
    }

    // 只比较主 RenderTarget 的 depth texture 格式和本模组 FBO 的已知深度格式，不查询 framebuffer attachment。
    public static boolean canBlitDepth(RenderTarget sourceFBO, FBO targetFBO) {
        if (sourceFBO == null || targetFBO == null) return false;
        int sourceDepthFormat = getRenderTargetDepthFormat(sourceFBO);
        int targetDepthFormat = targetFBO.getDepthInternalFormat();
        return isDepthFormatCompatible(sourceDepthFormat, targetDepthFormat);
    }

    // 直接读取 RenderTarget 的 depth texture 内部格式，避免触碰 framebuffer depth-stencil attachment。
    public static int getRenderTargetDepthFormat(RenderTarget sourceFBO) {
        if (sourceFBO == null || !sourceFBO.useDepth) return GL11.GL_NONE;
        int depthTextureId = sourceFBO.getDepthTextureId();
        if (depthTextureId <= 0) return GL11.GL_NONE;
        return getTextureInternalFormat(depthTextureId);
    }

    // 普通 depth-only 和 packed depth-stencil 分开判断，避免跨类型 depth blit。
    public static boolean isDepthFormatCompatible(int sourceDepthFormat, int targetDepthFormat) {
        if (sourceDepthFormat == GL11.GL_NONE || targetDepthFormat == GL11.GL_NONE) return false;
        if (sourceDepthFormat == targetDepthFormat) return true;
        return sourceDepthFormat == GL11.GL_DEPTH_COMPONENT && targetDepthFormat == GL14.GL_DEPTH_COMPONENT24;
    }

    // 根据 Minecraft 主 RenderTarget 当前深度纹理格式选择本模组 FBO 深度附件。
    public static FboDepthSpec chooseCompatibleDepthSpec(RenderTarget sourceFBO) {
        int sourceDepthFormat = getRenderTargetDepthFormat(sourceFBO);
        if (FBO.isDepthStencilInternalFormat(sourceDepthFormat)) {
            return new FboDepthSpec(FBO.DEPTH_STENCIL_TEXTURE, sourceDepthFormat);
        }
        return new FboDepthSpec(FBO.DEPTH_TEXTURE, GL14.GL_DEPTH_COMPONENT24);
    }

    // 查询 texture 的内部格式，并恢复调用前绑定。
    public static int getTextureInternalFormat(int textureId) {
        int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        int internalFormat = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_INTERNAL_FORMAT);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);
        return internalFormat;
    }

    // 统一恢复 blit 前的 read/draw framebuffer 和 buffer 状态。
    public static void restoreBlitState(int previousReadFbo, int previousDrawFbo, int previousReadBuffer, int previousDrawBuffer) {
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previousReadFbo);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousDrawFbo);
        GL11.glReadBuffer(previousReadBuffer);
        GL11.glDrawBuffer(previousDrawBuffer);
    }

    // 描述本模组 FBO 为匹配主 RenderTarget 所需的深度附件规格。
    public static class FboDepthSpec {
        public final int depthBufferType; // 本模组 FBO 应使用的深度附件类型。
        public final int depthInternalFormat; // 本模组 FBO 应使用的深度内部格式。

        public FboDepthSpec(int depthBufferType, int depthInternalFormat) {
            this.depthBufferType = depthBufferType;
            this.depthInternalFormat = depthInternalFormat;
        }

        // 判断现有 FBO 是否已经匹配当前主 RenderTarget 深度格式。
        public boolean matches(FBO fbo) {
            if (fbo == null) return false;
            return fbo.getDepthBufferType() == depthBufferType && fbo.getDepthInternalFormat() == depthInternalFormat;
        }

        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof FboDepthSpec other)) return false;
            return depthBufferType == other.depthBufferType && depthInternalFormat == other.depthInternalFormat;
        }

        public int hashCode() {
            return Objects.hash(depthBufferType, depthInternalFormat);
        }
    }
}
