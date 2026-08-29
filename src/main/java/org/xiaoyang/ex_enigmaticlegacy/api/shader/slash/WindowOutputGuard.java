package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.nio.ByteBuffer;

final class WindowOutputGuard {
    private static final int PROBE_SIZE = 4;
    private static final int PROBE_BYTES = PROBE_SIZE * PROBE_SIZE * 4;
    private static final ByteBuffer PIXELS = BufferUtils.createByteBuffer(PROBE_BYTES);
    private static TextureTarget mainProbe;
    private static TextureTarget windowProbe;
    private static boolean probeLogged;
    private static boolean recoveryLogged;

    private WindowOutputGuard() {
    }

    static void validateAndRecover(RenderTarget main, int windowWidth, int windowHeight) {
        RenderSystem.assertOnRenderThread();
        if (main == null || main.width <= 0 || main.height <= 0
                || windowWidth <= 0 || windowHeight <= 0) {
            return;
        }

        ensureTargets();
        if (!FramebufferBlitter.blitColor(main, mainProbe)
                || !FramebufferBlitter.blitDefaultColor(windowProbe, windowWidth, windowHeight)) {
            return;
        }

        Probe source = sample(mainProbe);
        Probe window = sample(windowProbe);
        boolean collapsed = ScreenOutputGuard.isCollapsed(
                source.averageRgb(), source.maxChannel(),
                window.averageRgb(), window.maxChannel()
        );
        boolean recovered = false;
        if (collapsed) {
            recovered = FramebufferBlitter.blitColorToDefault(main, windowWidth, windowHeight);
            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
            RenderSystem.viewport(0, 0, windowWidth, windowHeight);
        }

        if (!probeLogged) {
            probeLogged = true;
            Exe.LOGGER.info(
                    "[DimensionalSlash] Final no-pack window probe: main(avg={}, max={}), window(avg={}, max={}), recovered={}",
                    source.averageRgb(), source.maxChannel(), window.averageRgb(), window.maxChannel(), recovered
            );
        }
        if (collapsed && !recoveryLogged) {
            recoveryLogged = true;
            Exe.LOGGER.error(
                    "[DimensionalSlash] Recovered collapsed final window output with a direct framebuffer blit"
            );
        }
    }

    static void onPipelineSwitch() {
        probeLogged = false;
        recoveryLogged = false;
    }

    static void clear() {
        probeLogged = false;
        recoveryLogged = false;
        destroy(mainProbe);
        destroy(windowProbe);
        mainProbe = null;
        windowProbe = null;
    }

    private static void ensureTargets() {
        if (mainProbe == null) {
            mainProbe = createTarget();
        }
        if (windowProbe == null) {
            windowProbe = createTarget();
        }
    }

    private static TextureTarget createTarget() {
        TextureTarget target = new TextureTarget(PROBE_SIZE, PROBE_SIZE, false, Minecraft.ON_OSX);
        target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        return target;
    }

    private static Probe sample(RenderTarget target) {
        int previousReadFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int previousReadBuffer = GL11.glGetInteger(GL11.GL_READ_BUFFER);
        try {
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, target.frameBufferId);
            GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
            PIXELS.clear();
            GL11.glReadPixels(0, 0, PROBE_SIZE, PROBE_SIZE, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, PIXELS);

            int rgbTotal = 0;
            int maxChannel = 0;
            for (int index = 0; index < PROBE_SIZE * PROBE_SIZE; index++) {
                int offset = index * 4;
                int red = PIXELS.get(offset) & 0xFF;
                int green = PIXELS.get(offset + 1) & 0xFF;
                int blue = PIXELS.get(offset + 2) & 0xFF;
                rgbTotal += red + green + blue;
                maxChannel = Math.max(maxChannel, Math.max(red, Math.max(green, blue)));
            }
            return new Probe(rgbTotal / (float) (PROBE_SIZE * PROBE_SIZE * 3), maxChannel);
        } finally {
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previousReadFramebuffer);
            GL11.glReadBuffer(previousReadBuffer);
        }
    }

    private static void destroy(TextureTarget target) {
        if (target != null) {
            target.destroyBuffers();
        }
    }

    private record Probe(float averageRgb, int maxChannel) {
    }
}
