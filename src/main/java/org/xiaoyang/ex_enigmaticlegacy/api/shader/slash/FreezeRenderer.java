package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

public final class FreezeRenderer {
    private static final int FULLSCREEN_BUFFER_SIZE = 256;
    private static final FullscreenBufferBuilder FULLSCREEN_BUFFER = new FullscreenBufferBuilder(FULLSCREEN_BUFFER_SIZE);
    private static ShaderInstance shader;
    private static Uniform desaturationUniform;
    private static Uniform contrastUniform;
    private static Uniform brightnessUniform;
    private static Uniform vignetteUniform;
    private static TextureTarget sourceTarget;
    private static boolean pendingCapture;
    private static boolean active;
    private static boolean firstDesaturationFramePending;

    private FreezeRenderer() {
    }

    public static void shaderLoaded(ShaderInstance instance) {
        shader = instance;
        desaturationUniform = instance.getUniform("Desaturation");
        contrastUniform = instance.getUniform("Contrast");
        brightnessUniform = instance.getUniform("Brightness");
        vignetteUniform = instance.getUniform("VignetteStrength");
    }

    static boolean requestStart() {
        return requestStartInternal();
    }

    static boolean requestFinalBreakStart() {
        return requestStartInternal();
    }

    private static boolean requestStartInternal() {
        if (!SlashCofig.ScreenBreak.SCREEN_FREEZE_ENABLED || shader == null) {
            clear();
            return false;
        }
        if (active || pendingCapture) {
            return false;
        }
        pendingCapture = true;
        return true;
    }

    static boolean render(RenderTarget main, float presentationAlpha) {
        if (!SlashCofig.ScreenBreak.SCREEN_FREEZE_ENABLED || shader == null || main.width <= 0 || main.height <= 0) {
            clear();
            return false;
        }
        if (pendingCapture) {
            pendingCapture = false;
            active = true;
            firstDesaturationFramePending = true;
        }

        if (!active) {
            return false;
        }
        float safePresentationAlpha = Math.max(0.0f, Math.min(1.0f, presentationAlpha));
        if (safePresentationAlpha <= 0.001f) return false;

        ensureSourceTarget(main.width, main.height);
        if (sourceTarget == null) {
            return false;
        }

        if (!FramebufferBlitter.blitColor(main, sourceTarget)) {
            return false;
        }
        if (sourceTarget.width != main.width || sourceTarget.height != main.height) {
            return false;
        }

        main.bindWrite(false);
        RenderSystem.viewport(0, 0, main.width, main.height);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        DriverBlendState.disable();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        withScreenProjection(main.width, main.height, () -> {
            RenderSystem.setShader(() -> shader);
            shader.setSampler("DiffuseSampler", sourceTarget.getColorTextureId());
            if (desaturationUniform != null) desaturationUniform.set(SlashCofig.ScreenBreak.SCREEN_FREEZE_DESATURATION * safePresentationAlpha);
            if (contrastUniform != null) contrastUniform.set(1.0f + (SlashCofig.ScreenBreak.SCREEN_FREEZE_CONTRAST - 1.0f) * safePresentationAlpha);
            if (brightnessUniform != null) brightnessUniform.set(1.0f + (SlashCofig.ScreenBreak.SCREEN_FREEZE_BRIGHTNESS - 1.0f) * safePresentationAlpha);
            if (vignetteUniform != null) vignetteUniform.set(SlashCofig.ScreenBreak.SCREEN_FREEZE_VIGNETTE_STRENGTH * safePresentationAlpha);

            BufferBuilder builder = FULLSCREEN_BUFFER.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(0.0f, (float) main.height, 0.0f).uv(0.0f, 0.0f).endVertex();
            builder.vertex((float) main.width, (float) main.height, 0.0f).uv(1.0f, 0.0f).endVertex();
            builder.vertex((float) main.width, 0.0f, 0.0f).uv(1.0f, 1.0f).endVertex();
            builder.vertex(0.0f, 0.0f, 0.0f).uv(0.0f, 1.0f).endVertex();
            BufferUploader.drawWithShader(builder.end());
        });

        main.bindWrite(false);
        RenderSystem.viewport(0, 0, main.width, main.height);
        boolean firstDesaturationFrame = firstDesaturationFramePending;
        firstDesaturationFramePending = false;
        return firstDesaturationFrame;
    }

    static void release() {
        pendingCapture = false;
        active = false;
        firstDesaturationFramePending = false;
        disposeSourceTarget();
    }

    static void clear() {
        pendingCapture = false;
        active = false;
        firstDesaturationFramePending = false;
        disposeSourceTarget();
    }

    static void clearAll() {
        clear();
    }

    private static void ensureSourceTarget(int width, int height) {
        if (sourceTarget == null) {
            sourceTarget = new TextureTarget(width, height, false, Minecraft.ON_OSX);
            sourceTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        } else if (sourceTarget.width != width || sourceTarget.height != height) {
            sourceTarget.resize(width, height, Minecraft.ON_OSX);
        }
    }

    private static void disposeSourceTarget() {
        if (sourceTarget != null) {
            sourceTarget.destroyBuffers();
            sourceTarget = null;
        }
    }

    private static void withScreenProjection(int width, int height, Runnable draw) {
        RenderSystem.backupProjectionMatrix();
        Matrix4f projection = new Matrix4f().setOrtho(0.0f, (float) width, (float) height, 0.0f, 1000.0f, 3000.0f);
        RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);

        var modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.setIdentity();
        modelView.translate(0.0f, 0.0f, -2000.0f);
        RenderSystem.applyModelViewMatrix();

        try {
            draw.run();
        } finally {
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            DriverBlendState.enableDefault();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

}
