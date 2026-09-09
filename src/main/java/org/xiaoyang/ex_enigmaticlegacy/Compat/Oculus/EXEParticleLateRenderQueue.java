package org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.xiaoyang.ex_enigmaticlegacy.Client.help.EXECoreShaders;

import java.util.*;

public final class EXEParticleLateRenderQueue {
    private static final List<GlowQuad> GLOW_QUADS = new ArrayList<>();
    private static final List<StarQuad> STAR_QUADS = new ArrayList<>();
    private static final Set<Object> SEEN_GLOW_PARTICLES = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Set<Object> SEEN_STAR_PARTICLES = Collections.newSetFromMap(new IdentityHashMap<>());
    private static boolean deferThisFrame;
    private static Matrix4f particleModelView;
    private static Matrix4f particleProjection;

    public static void beginFrame(EXERenderFrameState.Snapshot frameState) {
        clearQueuedGeometry();
        deferThisFrame = frameState.shaderPackActive();
    }

    public static boolean shouldDefer() {
        return deferThisFrame;
    }

    public static void enqueueGlow(Object particle, Vector3f[] corners, float red, float green, float blue, float alpha, int light) {
        if (!deferThisFrame || particle == null || corners.length < 4 || !SEEN_GLOW_PARTICLES.add(particle)) {
            return;
        }
        captureParticleMatrices();
        GLOW_QUADS.add(new GlowQuad(copy(corners[0]), copy(corners[1]), copy(corners[2]), copy(corners[3]), red, green, blue, alpha, light));
    }

    public static void enqueueStar(Object particle, Vector3f[] corners, float alpha, float coreAlpha, int light) {
        if (!deferThisFrame || particle == null || corners.length < 4 || !SEEN_STAR_PARTICLES.add(particle)) {
            return;
        }
        captureParticleMatrices();
        STAR_QUADS.add(new StarQuad(copy(corners[0]), copy(corners[1]), copy(corners[2]), copy(corners[3]), alpha, coreAlpha, light));
    }

    public static void renderAfterLevel() {
        if (!deferThisFrame || (GLOW_QUADS.isEmpty() && STAR_QUADS.isEmpty())) {
            clearQueuedGeometry();
            return;
        }

        ShaderInstance blackShader = EXECoreShaders.getTrueDemonParticleShader();
        ShaderInstance whiteShader = EXECoreShaders.getTrueDemonParticleWhiteShader();
        ShaderInstance magentaShader = EXECoreShaders.getTrueDemonParticleMagentaShader();
        ShaderInstance starShader = EXECoreShaders.getTrueDemonStarParticleShader();
        if (Minecraft.getInstance().level == null || particleModelView == null || particleProjection == null
                || blackShader == null || whiteShader == null || magentaShader == null || starShader == null) {
            clearQueuedGeometry();
            return;
        }

        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.last().pose().set(particleModelView);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f(particleProjection), VertexSorting.DISTANCE_TO_ORIGIN);

        try {
            EXELatePassState.prepare();
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.depthMask(false);

            drawGlowLayer(blackShader, 0);
            drawGlowLayer(whiteShader, 1);
            drawGlowLayer(magentaShader, 2);
            drawStars(starShader);
        } finally {
            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.setProjectionMatrix(previousProjection, VertexSorting.DISTANCE_TO_ORIGIN);
            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();
            EXELatePassState.finish();
            clearQueuedGeometry();
        }
    }

    public static void endFrame() {
        clearQueuedGeometry();
        deferThisFrame = false;
    }

    public static void clearAll() {
        endFrame();
    }

    private static void drawGlowLayer(ShaderInstance shader, int layer) {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        for (GlowQuad quad : GLOW_QUADS) {
            quad.write(builder);
        }
        BufferBuilder.RenderedBuffer rendered = builder.endOrDiscardIfEmpty();
        if (rendered == null) return;

        Uniform layerUniform = shader.getUniform("Layer");
        if (layerUniform != null) layerUniform.set(layer);
        RenderSystem.setShader(() -> shader);
        BufferUploader.drawWithShader(rendered);
    }

    private static void drawStars(ShaderInstance shader) {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        for (StarQuad quad : STAR_QUADS) {
            quad.write(builder);
        }
        BufferBuilder.RenderedBuffer rendered = builder.endOrDiscardIfEmpty();
        if (rendered == null) return;

        RenderSystem.setShader(() -> shader);
        BufferUploader.drawWithShader(rendered);
    }

    private static void captureParticleMatrices() {
        if (particleModelView == null) {
            particleModelView = new Matrix4f(RenderSystem.getModelViewMatrix());
            particleProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        }
    }

    private static Vector3f copy(Vector3f vector) {
        return new Vector3f(vector);
    }

    private static void clearQueuedGeometry() {
        GLOW_QUADS.clear();
        STAR_QUADS.clear();
        SEEN_GLOW_PARTICLES.clear();
        SEEN_STAR_PARTICLES.clear();
        particleModelView = null;
        particleProjection = null;
    }

    private record GlowQuad(Vector3f corner0, Vector3f corner1, Vector3f corner2, Vector3f corner3,
                            float red, float green, float blue, float alpha, int light) {
        private void write(BufferBuilder builder) {
            writeVertex(builder, corner0, 0.0F, 1.0F, red, green, blue, alpha, light);
            writeVertex(builder, corner1, 0.0F, 0.0F, red, green, blue, alpha, light);
            writeVertex(builder, corner2, 1.0F, 0.0F, red, green, blue, alpha, light);
            writeVertex(builder, corner3, 1.0F, 1.0F, red, green, blue, alpha, light);
        }
    }

    private record StarQuad(Vector3f corner0, Vector3f corner1, Vector3f corner2, Vector3f corner3,
                            float alpha, float coreAlpha, int light) {
        private void write(BufferBuilder builder) {
            writeVertex(builder, corner0, 0.0F, 1.0F, coreAlpha, 1.0F, 1.0F, alpha, light);
            writeVertex(builder, corner1, 0.0F, 0.0F, coreAlpha, 1.0F, 1.0F, alpha, light);
            writeVertex(builder, corner2, 1.0F, 0.0F, coreAlpha, 1.0F, 1.0F, alpha, light);
            writeVertex(builder, corner3, 1.0F, 1.0F, coreAlpha, 1.0F, 1.0F, alpha, light);
        }
    }

    private static void writeVertex(BufferBuilder builder, Vector3f corner, float u, float v,
                                    float red, float green, float blue, float alpha, int light) {
        builder.vertex(corner.x(), corner.y(), corner.z()).uv(u, v).color(red, green, blue, alpha).uv2(light).endVertex();
    }

    private EXEParticleLateRenderQueue() {}
}
