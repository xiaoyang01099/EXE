package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.frameBuffer;

import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class FBO {
    public static final int NONE = 0;
    public static final int DEPTH_TEXTURE = 1;
    public static final int DEPTH_RENDER_BUFFER = 2;
    public static final int DEPTH_STENCIL_TEXTURE = 3;
    public static final int DEPTH_STENCIL_RENDER_BUFFER = 4;
    private int width;
    private int height;
    private int w,h;
    private int frameBuffer;
    private int depthBuffer;
    private int[] colourTextures;
    private int colorAttachmentCount;
    private int depthTexture;
    private int depthBufferType;
    private int depthInternalFormat;

    public FBO(int width, int height, int depthBufferType) {
        this(width, height, depthBufferType, 1);
    }

    public FBO(int width, int height, int depthBufferType, int colorAttachmentCount) {
        this(width, height, depthBufferType, colorAttachmentCount, defaultInternalFormatForDepthBufferType(depthBufferType));
    }

    public FBO(int width, int height, int depthBufferType, int colorAttachmentCount, int depthInternalFormat) {
        this.width = width;
        this.height = height;
        this.depthBufferType = depthBufferType;
        this.depthInternalFormat = depthInternalFormat;
        this.colorAttachmentCount = Math.max(1, colorAttachmentCount);
        initialiseFrameBuffer(depthBufferType);
        w = Minecraft.getInstance().getWindow().getWidth();
        h = Minecraft.getInstance().getWindow().getHeight();
    }

    public void resize(int newWidth, int newHeight) {
        if (newWidth <= 0 || newHeight <= 0) return;
        resize(newWidth, newHeight, depthBufferType, depthInternalFormat);
    }

    public void resize(int newWidth, int newHeight, int newDepthBufferType, int newDepthInternalFormat) {
        if (newWidth <= 0 || newHeight <= 0) return;
        cleanUp();
        this.width = newWidth;
        this.height = newHeight;
        this.depthBufferType = newDepthBufferType;
        this.depthInternalFormat = newDepthInternalFormat;
        initialiseFrameBuffer(depthBufferType);
        this.w = Minecraft.getInstance().getWindow().getWidth();
        this.h = Minecraft.getInstance().getWindow().getHeight();
    }

    public void bindFrameBuffer(boolean clearDepth, int... colorAttachmentsToClear) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
        GL11.glViewport(0, 0, width, height);
        GL11.glClearColor(0f, 0f, 0f, 0f);

        if (colorAttachmentsToClear != null) {
            for (int attachmentIndex : colorAttachmentsToClear) {
                clearColorAttachment(attachmentIndex, 0f, 0f, 0f, 0f);
            }
        }

        if (clearDepth) {
            GL11.glClearDepth(1.0D);
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        }

        if (colorAttachmentsToClear != null && colorAttachmentsToClear.length > 0) {
            setDrawBuffers(colorAttachmentsToClear);
        }
    }

    public void unbindFrameBuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(0, 0, w, h);
    }

    public void unbindFrameBuffer(int fbo) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
        GL11.glViewport(0, 0, w, h);
    }

    public void initialiseFrameBuffer(int type) {
        createFrameBuffer();
        createTextureAttachment();

        if (type == DEPTH_RENDER_BUFFER) {
            createDepthBufferAttachment();
        } else if (type == DEPTH_TEXTURE) {
            createDepthTextureAttachment();
        } else if (type == DEPTH_STENCIL_TEXTURE) {
            createDepthStencilTextureAttachment();
        } else if (type == DEPTH_STENCIL_RENDER_BUFFER) {
            createDepthStencilBufferAttachment();
        }
        checkFrameBufferComplete();
        unbindFrameBuffer();
    }

    private void createFrameBuffer() {
        frameBuffer = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
    }

    private void createTextureAttachment() {
        colourTextures = new int[colorAttachmentCount];

        for (int i = 0; i < colorAttachmentCount; i++) {
            int colourTexture = GL11.glGenTextures();
            colourTextures[i] = colourTexture;

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, colourTexture);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
                    (ByteBuffer) null);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + i, GL11.GL_TEXTURE_2D,
                    colourTexture, 0);
        }

        configureDrawBuffers();
    }

    public void configureDrawBuffers() {
        IntBuffer buffers = BufferUtils.createIntBuffer(colorAttachmentCount);
        for (int i = 0; i < colorAttachmentCount; i++) {
            buffers.put(GL30.GL_COLOR_ATTACHMENT0 + i);
        }
        buffers.flip();
        GL20.glDrawBuffers(buffers);
    }

    public void setDrawBuffer(int attachmentIndex) {
        validateColorAttachmentIndex(attachmentIndex);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0 + attachmentIndex);
    }

    public void setDrawBuffers(int... attachmentIndices) {
        if (attachmentIndices == null || attachmentIndices.length == 0) {
            configureDrawBuffers();
            return;
        }

        IntBuffer buffers = BufferUtils.createIntBuffer(attachmentIndices.length);
        for (int attachmentIndex : attachmentIndices) {
            validateColorAttachmentIndex(attachmentIndex);
            buffers.put(GL30.GL_COLOR_ATTACHMENT0 + attachmentIndex);
        }
        buffers.flip();
        GL20.glDrawBuffers(buffers);
    }

    public void clearColorAttachment(int attachmentIndex, float red, float green, float blue, float alpha) {
        validateColorAttachmentIndex(attachmentIndex);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0 + attachmentIndex);
        GL11.glClearColor(red, green, blue, alpha);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }

    public void validateColorAttachmentIndex(int attachmentIndex) {
        if (attachmentIndex < 0 || attachmentIndex >= colorAttachmentCount) {
            throw new IndexOutOfBoundsException("Color attachment index out of range: " + attachmentIndex);
        }
    }

    private void createDepthTextureAttachment() {
        depthTexture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, depthInternalFormat, width, height, 0, GL11.GL_DEPTH_COMPONENT,
                GL11.GL_FLOAT, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depthTexture, 0);
    }

    private void createDepthBufferAttachment() {
        depthBuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, depthInternalFormat, width, height);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER,
                depthBuffer);
    }

    public void createDepthStencilTextureAttachment() {
        depthTexture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, depthInternalFormat, width, height, 0, GL30.GL_DEPTH_STENCIL,
                getDepthStencilPixelType(depthInternalFormat), (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL11.GL_TEXTURE_2D, depthTexture, 0);
    }

    public void createDepthStencilBufferAttachment() {
        depthBuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, depthInternalFormat, width, height);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER,
                depthBuffer);
    }

    public void cleanUp() {
        GL30.glDeleteFramebuffers(frameBuffer);
        if (colourTextures != null) {
            for (int colourTexture : colourTextures) {
                GL11.glDeleteTextures(colourTexture);
            }
        }
        if (depthTexture > 0) {
            GL11.glDeleteTextures(depthTexture);
            depthTexture = 0;
        }
        if (depthBuffer > 0) {
            GL30.glDeleteRenderbuffers(depthBuffer);
            depthBuffer = 0;
        }
    }

    private void checkFrameBufferComplete() {
        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("FBO incomplete: " + status);
        }
    }

    public int getFrameBuffer() {
        return frameBuffer;
    }

    public int getColourTexture() {
        return getColourTexture(0);
    }

    public int getColourTexture(int attachmentIndex) {
        validateColorAttachmentIndex(attachmentIndex);
        return colourTextures[attachmentIndex];
    }

    public int getColorAttachmentCount() {
        return colorAttachmentCount;
    }

    public int getDepthTexture() {
        return depthTexture;
    }

    public int getDepthBufferType() {
        return depthBufferType;
    }

    public int getDepthInternalFormat() {
        if (depthBufferType == NONE) return GL11.GL_NONE;
        return depthInternalFormat;
    }

    public static int defaultInternalFormatForDepthBufferType(int type) {
        if (type == DEPTH_TEXTURE || type == DEPTH_RENDER_BUFFER) return GL14.GL_DEPTH_COMPONENT24;
        if (type == DEPTH_STENCIL_TEXTURE || type == DEPTH_STENCIL_RENDER_BUFFER) return GL30.GL_DEPTH32F_STENCIL8;
        return GL11.GL_NONE;
    }

    public static boolean isDepthStencilInternalFormat(int internalFormat) {
        return internalFormat == GL30.GL_DEPTH24_STENCIL8 || internalFormat == GL30.GL_DEPTH32F_STENCIL8;
    }

    public static int getDepthStencilPixelType(int internalFormat) {
        if (internalFormat == GL30.GL_DEPTH32F_STENCIL8) return GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV;
        return GL30.GL_UNSIGNED_INT_24_8;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
