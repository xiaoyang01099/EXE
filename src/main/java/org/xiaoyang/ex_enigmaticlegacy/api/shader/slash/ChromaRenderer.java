package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public final class ChromaRenderer {
    private static final int FULLSCREEN_BUFFER_SIZE = 256;
    private static final FullscreenBufferBuilder FULLSCREEN_BUFFER = new FullscreenBufferBuilder(FULLSCREEN_BUFFER_SIZE);
    private static ShaderInstance shader;
    private static Uniform texelSizeUniform;
    private static Uniform strengthUniform;
    private static Uniform offsetPixelsUniform;
    private static Uniform centerClearRadiusUniform;
    private static Uniform edgeFullRadiusUniform;
    private static TextureTarget sourceTarget;

    private ChromaRenderer() {
    }

    public static void shaderLoaded(ShaderInstance instance) {
        shader = instance;
        texelSizeUniform = instance.getUniform("TexelSize");
        strengthUniform = instance.getUniform("Strength");
        offsetPixelsUniform = instance.getUniform("OffsetPixels");
        centerClearRadiusUniform = instance.getUniform("CenterClearRadius");
        edgeFullRadiusUniform = instance.getUniform("EdgeFullRadius");
    }

    static void render(RenderTarget main, float strength) {
        float safeStrength = Mth.clamp(strength, 0.0f, 1.0f);
        if (!SlashCofig.ScreenBreak.TopChroma.ENABLED || shader == null
                || safeStrength <= 0.001f || main.width <= 0 || main.height <= 0) {
            return;
        }
        ensureSourceTarget(main.width, main.height);
        if (sourceTarget == null) return;
        if (!FramebufferBlitter.blitColor(main, sourceTarget)) return;

        main.bindWrite(false);
        RenderSystem.viewport(0, 0, main.width, main.height);
        RenderSystem.disableScissor();
        DriverBlendState.disable();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        withScreenProjection(main.width, main.height, () -> {
            RenderSystem.setShader(() -> shader);
            shader.setSampler("SceneSampler", sourceTarget.getColorTextureId());
            if (texelSizeUniform != null) texelSizeUniform.set(1.0f / main.width, 1.0f / main.height);
            if (strengthUniform != null) strengthUniform.set(safeStrength);
            if (offsetPixelsUniform != null) offsetPixelsUniform.set(Math.max(0.0f, SlashCofig.ScreenBreak.TopChroma.OFFSET_PIXELS));
            if (centerClearRadiusUniform != null) centerClearRadiusUniform.set(Math.max(0.0f, SlashCofig.ScreenBreak.TopChroma.CENTER_CLEAR_RADIUS));
            if (edgeFullRadiusUniform != null) edgeFullRadiusUniform.set(Math.max(0.001f, SlashCofig.ScreenBreak.TopChroma.EDGE_FULL_RADIUS));

            BufferBuilder builder = FULLSCREEN_BUFFER.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(0.0f, (float) main.height, 0.0f).uv(0.0f, 0.0f).endVertex();
            builder.vertex((float) main.width, (float) main.height, 0.0f).uv(1.0f, 0.0f).endVertex();
            builder.vertex((float) main.width, 0.0f, 0.0f).uv(1.0f, 1.0f).endVertex();
            builder.vertex(0.0f, 0.0f, 0.0f).uv(0.0f, 1.0f).endVertex();
            BufferUploader.drawWithShader(builder.end());
        });

        main.bindWrite(false);
        RenderSystem.viewport(0, 0, main.width, main.height);
    }

    static void clear() {
        if (sourceTarget != null) {
            sourceTarget.destroyBuffers();
            sourceTarget = null;
        }
    }

    private static void ensureSourceTarget(int width, int height) {
        if (sourceTarget == null) {
            sourceTarget = new TextureTarget(width, height, false, Minecraft.ON_OSX);
            sourceTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        } else if (sourceTarget.width != width || sourceTarget.height != height) {
            sourceTarget.resize(width, height, Minecraft.ON_OSX);
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
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

}
