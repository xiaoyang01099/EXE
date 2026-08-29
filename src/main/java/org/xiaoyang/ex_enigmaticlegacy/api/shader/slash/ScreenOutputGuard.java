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

final class ScreenOutputGuard {
    private static final int PROBE_SIZE = 4;
    private static final int PROBE_BYTES = PROBE_SIZE * PROBE_SIZE * 4;
    private static final ByteBuffer PIXELS = BufferUtils.createByteBuffer(PROBE_BYTES);
    private static TextureTarget inputSnapshot;
    private static TextureTarget inputProbe;
    private static TextureTarget outputProbe;
    private static boolean captured;
    private static boolean inputSnapshotValid;
    private static boolean recoveryLogged;

    private ScreenOutputGuard() {
    }

    static void capture(RenderTarget main, boolean needed) {
        captured = false;
        if (!needed || main == null || main.width <= 0 || main.height <= 0) {
            return;
        }

        ensureTargets(main.width, main.height);
        captured = inputSnapshot != null && FramebufferBlitter.blitColor(main, inputSnapshot);
        inputSnapshotValid = captured;
        main.bindWrite(false);
        RenderSystem.viewport(0, 0, main.width, main.height);
    }

    static void validateCustomOutputAndCommit(RenderTarget main) {
        if (!captured || main == null || inputSnapshot == null || inputProbe == null || outputProbe == null) {
            captured = false;
            return;
        }

        restoreIfCollapsed(inputSnapshot, main, "custom fullscreen stack");
        if (!FramebufferBlitter.blitColor(main, inputSnapshot)) {
            captured = false;
            inputSnapshotValid = false;
        } else {
            inputSnapshotValid = true;
        }
        main.bindWrite(false);
        RenderSystem.viewport(0, 0, main.width, main.height);
    }

    static void finishBeforeHotbar(RenderTarget main) {
        if (!captured || main == null || inputSnapshot == null || inputProbe == null || outputProbe == null) {
            captured = false;
            return;
        }
        restoreIfCollapsed(inputSnapshot, main, "pre-hotbar GUI boundary");
        captured = false;
    }

    private static void restoreIfCollapsed(RenderTarget reference, RenderTarget main, String stage) {

        if (reference == null || !FramebufferBlitter.blitColor(reference, inputProbe) || !FramebufferBlitter.blitColor(main, outputProbe)) {
            return;
        }

        Probe input = sample(inputProbe);
        Probe output = sample(outputProbe);
        boolean collapsed = isCollapsed(input.averageRgb(), input.maxChannel(), output.averageRgb(), output.maxChannel());
        if (!collapsed) {
            return;
        }

        FramebufferBlitter.blitColor(reference, main);
        main.bindWrite(false);
        RenderSystem.viewport(0, 0, main.width, main.height);
        if (!recoveryLogged) {
            recoveryLogged = true;
            Exe.LOGGER.error("[DimensionalSlash] Rejected collapsed black output at {}: input(avg={}, max={}), output(avg={}, max={})", stage, input.averageRgb(), input.maxChannel(), output.averageRgb(), output.maxChannel());
        }
    }

    static boolean isCollapsed(float inputAverage, int inputMax, float outputAverage, int outputMax) {
        boolean outputIsBlack = outputMax <= 3 && outputAverage <= 1.5f;
        boolean inputHadVisibleSignal = inputMax >= outputMax + 4 || inputAverage >= outputAverage + 0.75f;
        return outputIsBlack && inputHadVisibleSignal;
    }

    static void clear() {
        captured = false;
        inputSnapshotValid = false;
        recoveryLogged = false;
        destroy(inputSnapshot);
        destroy(inputProbe);
        destroy(outputProbe);
        inputSnapshot = null;
        inputProbe = null;
        outputProbe = null;
    }

    private static void ensureTargets(int width, int height) {
        if (inputSnapshot == null) {
            inputSnapshot = createTarget(width, height);
        } else if (inputSnapshot.width != width || inputSnapshot.height != height) {
            inputSnapshotValid = false;
            inputSnapshot.resize(width, height, Minecraft.ON_OSX);
        }
        if (inputProbe == null) {
            inputProbe = createTarget(PROBE_SIZE, PROBE_SIZE);
        }
        if (outputProbe == null) {
            outputProbe = createTarget(PROBE_SIZE, PROBE_SIZE);
        }
    }

    private static TextureTarget createTarget(int width, int height) {
        TextureTarget target = new TextureTarget(width, height, false, Minecraft.ON_OSX);
        target.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
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
