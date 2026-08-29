package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Objects;

/**
 * Copies render-target attachments without depending on framebuffer state left by
 * Minecraft, Oculus, or another post-processing pass.
 */
public final class FramebufferBlitter {
    private FramebufferBlitter() {
    }

    public static boolean blitColor(RenderTarget source, RenderTarget destination) {
        return blit(source, destination, GL30.GL_COLOR_BUFFER_BIT, Region.full(source, destination));
    }

    public static boolean blitColor(RenderTarget source, RenderTarget destination, Region... regions) {
        return blit(source, destination, GL30.GL_COLOR_BUFFER_BIT, regions);
    }

    public static boolean blitDepth(RenderTarget source, RenderTarget destination) {
        return blit(source, destination, GL30.GL_DEPTH_BUFFER_BIT, Region.full(source, destination));
    }

    public static boolean blitColorAndDepth(RenderTarget source, RenderTarget destination) {
        return blit(source, destination, GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT,
                Region.full(source, destination));
    }

    /** Copies the window back buffer into an ordinary texture target. */
    public static boolean blitDefaultColor(RenderTarget destination, int sourceWidth, int sourceHeight) {
        Objects.requireNonNull(destination, "destination");
        return blit(
                0, sourceWidth, sourceHeight,
                destination.frameBufferId, destination.width, destination.height,
                GL30.GL_COLOR_BUFFER_BIT,
                new Region(0, 0, sourceWidth, sourceHeight, 0, 0, destination.width, destination.height)
        );
    }

    /**
     * Presents a texture target without ShaderInstance, BufferUploader or a
     * fullscreen draw.  This is deliberately reserved for recovery after the
     * normal final window blit has produced a verified black back buffer.
     */
    public static boolean blitColorToDefault(RenderTarget source, int destinationWidth, int destinationHeight) {
        Objects.requireNonNull(source, "source");
        return blit(
                source.frameBufferId, source.width, source.height,
                0, destinationWidth, destinationHeight,
                GL30.GL_COLOR_BUFFER_BIT,
                new Region(0, 0, source.width, source.height, 0, 0, destinationWidth, destinationHeight)
        );
    }

    private static boolean blit(RenderTarget source, RenderTarget destination, int mask, Region... regions) {
        RenderSystem.assertOnRenderThread();
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(destination, "destination");
        return blit(
                source.frameBufferId, source.width, source.height,
                destination.frameBufferId, destination.width, destination.height,
                mask, regions
        );
    }

    private static boolean blit(
            int sourceFramebuffer,
            int sourceWidth,
            int sourceHeight,
            int destinationFramebuffer,
            int destinationWidth,
            int destinationHeight,
            int mask,
            Region... regions
    ) {
        RenderSystem.assertOnRenderThread();
        Objects.requireNonNull(regions, "regions");
        if (sourceWidth <= 0 || sourceHeight <= 0 || destinationWidth <= 0 || destinationHeight <= 0
                || regions.length == 0) {
            return false;
        }

        int previousReadFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int previousDrawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        boolean scissorEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer scissorBox = stack.mallocInt(4);
            GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, scissorBox);

            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, sourceFramebuffer);
            int previousSourceReadBuffer = GL11.glGetInteger(GL11.GL_READ_BUFFER);

            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, destinationFramebuffer);
            int drawBufferCount = destinationFramebuffer == 0
                    ? 1
                    : Math.max(1, GL11.glGetInteger(GL20.GL_MAX_DRAW_BUFFERS));
            IntBuffer previousDestinationDrawBuffers = stack.mallocInt(drawBufferCount);
            for (int index = 0; index < drawBufferCount; index++) {
                previousDestinationDrawBuffers.put(index, GL11.glGetInteger(GL20.GL_DRAW_BUFFER0 + index));
            }

            try {
                RenderSystem.disableScissor();
                GL11.glReadBuffer(colorBufferFor(sourceFramebuffer));
                GL11.glDrawBuffer(colorBufferFor(destinationFramebuffer));

                if (GL30.glCheckFramebufferStatus(GL30.GL_READ_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE
                        || GL30.glCheckFramebufferStatus(GL30.GL_DRAW_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
                    return false;
                }

                boolean copied = false;
                for (Region region : regions) {
                    if (region == null || !region.hasArea()) continue;
                    GL30.glBlitFramebuffer(
                            region.sourceX0, region.sourceY0, region.sourceX1, region.sourceY1,
                            region.destinationX0, region.destinationY0, region.destinationX1, region.destinationY1,
                            mask, GL30.GL_NEAREST
                    );
                    copied = true;
                }
                return copied;
            } finally {
                GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, sourceFramebuffer);
                GL11.glReadBuffer(previousSourceReadBuffer);
                GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, destinationFramebuffer);
                if (destinationFramebuffer == 0) {
                    // glDrawBuffers does not accept the aggregate GL_BACK enum
                    // on every 3.2 driver; the single-buffer API does.
                    GL11.glDrawBuffer(previousDestinationDrawBuffers.get(0));
                } else {
                    previousDestinationDrawBuffers.position(0);
                    GL20.glDrawBuffers(previousDestinationDrawBuffers);
                }

                GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previousReadFramebuffer);
                GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousDrawFramebuffer);
                if (scissorEnabled) {
                    RenderSystem.enableScissor(
                            scissorBox.get(0), scissorBox.get(1), scissorBox.get(2), scissorBox.get(3)
                    );
                } else {
                    RenderSystem.disableScissor();
                }
            }
        }
    }

    private static int colorBufferFor(int framebufferId) {
        return framebufferId == 0 ? GL11.GL_BACK : GL30.GL_COLOR_ATTACHMENT0;
    }

    public record Region(
            int sourceX0,
            int sourceY0,
            int sourceX1,
            int sourceY1,
            int destinationX0,
            int destinationY0,
            int destinationX1,
            int destinationY1
    ) {
        public static Region full(RenderTarget source, RenderTarget destination) {
            Objects.requireNonNull(source, "source");
            Objects.requireNonNull(destination, "destination");
            return new Region(
                    0, 0, source.width, source.height,
                    0, 0, destination.width, destination.height
            );
        }

        private boolean hasArea() {
            return sourceX0 != sourceX1 && sourceY0 != sourceY1
                    && destinationX0 != destinationX1 && destinationY0 != destinationY1;
        }
    }
}
