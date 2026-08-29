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

import java.util.ArrayList;
import java.util.List;

public final class DistortionRenderer {
    private static final int FULLSCREEN_BUFFER_SIZE = 256;
    private static final FullscreenBufferBuilder FULLSCREEN_BUFFER = new FullscreenBufferBuilder(FULLSCREEN_BUFFER_SIZE);
    private static ShaderInstance shader;
    private static Uniform texelSizeUniform;
    private static Uniform sourceTexelSizeUniform;
    private static Uniform sourcePaddingUniform;
    private static Uniform lineStartUniform;
    private static Uniform lineEndUniform;
    private static Uniform lineWidthUniform;
    private static Uniform lineStrengthUniform;
    private static Uniform lineAlphaUniform;
    private static Uniform directionFlipUniform;
    private static Uniform centerFadeInnerUniform;
    private static Uniform centerFadeOuterUniform;
    private static Uniform centerFadeMinStrengthUniform;
    private static Uniform voronoiStrengthUniform;
    private static Uniform voronoiAgeUniform;
    private static Uniform voronoiSeedUniform;
    private static Uniform voronoiCellScaleUniform;
    private static Uniform voronoiWarpPixelsUniform;
    private static Uniform voronoiCrackWidthUniform;
    private static Uniform voronoiCrackAlphaUniform;
    private static Uniform voronoiCrackColorUniform;
    private static Uniform voronoiCrackRevealThresholdUniform;
    private static Uniform voronoiCenterInnerUniform;
    private static Uniform voronoiCenterOuterUniform;
    private static Uniform voronoiCenterFalloffUniform;
    private static Uniform voronoiSpreadStartUniform;
    private static Uniform voronoiSpreadEndUniform;
    private static Uniform voronoiSpreadTicksUniform;
    private static Uniform voronoiSpreadFeatherUniform;
    private static TextureTarget sourceTarget;
    private static int sourcePaddingPixels;

    private DistortionRenderer() {
    }

    public static void shaderLoaded(ShaderInstance instance) {
        shader = instance;
        texelSizeUniform = instance.getUniform("TexelSize");
        sourceTexelSizeUniform = instance.getUniform("SourceTexelSize");
        sourcePaddingUniform = instance.getUniform("SourcePaddingPixels");
        lineStartUniform = instance.getUniform("LineStart");
        lineEndUniform = instance.getUniform("LineEnd");
        lineWidthUniform = instance.getUniform("LineWidthPixels");
        lineStrengthUniform = instance.getUniform("LineStrengthPixels");
        lineAlphaUniform = instance.getUniform("LineAlpha");
        directionFlipUniform = instance.getUniform("DirectionFlip");
        centerFadeInnerUniform = instance.getUniform("CenterFadeInnerRadius");
        centerFadeOuterUniform = instance.getUniform("CenterFadeOuterRadius");
        centerFadeMinStrengthUniform = instance.getUniform("CenterFadeMinStrength");
        voronoiStrengthUniform = instance.getUniform("VoronoiStrength");
        voronoiAgeUniform = instance.getUniform("VoronoiAge");
        voronoiSeedUniform = instance.getUniform("VoronoiSeed");
        voronoiCellScaleUniform = instance.getUniform("VoronoiCellScale");
        voronoiWarpPixelsUniform = instance.getUniform("VoronoiWarpPixels");
        voronoiCrackWidthUniform = instance.getUniform("VoronoiCrackWidthPixels");
        voronoiCrackAlphaUniform = instance.getUniform("VoronoiCrackAlpha");
        voronoiCrackColorUniform = instance.getUniform("VoronoiCrackColor");
        voronoiCrackRevealThresholdUniform = instance.getUniform("VoronoiCrackRevealThreshold");
        voronoiCenterInnerUniform = instance.getUniform("VoronoiCenterInnerRadius");
        voronoiCenterOuterUniform = instance.getUniform("VoronoiCenterOuterRadius");
        voronoiCenterFalloffUniform = instance.getUniform("VoronoiCenterFalloffPower");
        voronoiSpreadStartUniform = instance.getUniform("VoronoiSpreadStartRadius");
        voronoiSpreadEndUniform = instance.getUniform("VoronoiSpreadEndRadius");
        voronoiSpreadTicksUniform = instance.getUniform("VoronoiSpreadTicks");
        voronoiSpreadFeatherUniform = instance.getUniform("VoronoiSpreadFeatherRadius");
    }

    static void render(RenderTarget main, List<ScreenLine> lines, VoronoiState voronoiState) {
        if (shader == null || lines == null || lines.isEmpty() || main.width <= 0 || main.height <= 0) {
            return;
        }
        List<ScreenLine> selectedLines = selectRealCrackPathLines(lines, main.width, main.height);
        if (selectedLines.isEmpty()) {
            return;
        }

        ensureSourceTarget(main.width, main.height);
        if (sourceTarget == null) {
            return;
        }

        VoronoiState safeVoronoiState = voronoiState == null ? VoronoiState.NONE : voronoiState;
        int lastLineIndex = selectedLines.size() - 1;
        for (int i = 0; i < selectedLines.size(); i++) {
            if (!blitColorWithMirroredPadding(main, sourceTarget, sourcePaddingPixels)) return;
            renderDirectionalOffsetPass(main, selectedLines.get(i), i == lastLineIndex ? safeVoronoiState : VoronoiState.NONE);
        }
    }

    static void clear() {
        if (sourceTarget != null) {
            sourceTarget.destroyBuffers();
            sourceTarget = null;
        }
    }

    private static List<ScreenLine> selectRealCrackPathLines(List<ScreenLine> lines, int width, int height) {
        int limit = Math.max(1, SlashCofig.ScreenBreak.DISTORTION_LINE_COUNT);
        List<ScreenLine> selected = new ArrayList<>(limit);
        for (ScreenLine line : lines) {
            if (selected.size() >= limit) {
                break;
            }
            if (isRenderableCrackPath(line, width, height)) {
                selected.add(line);
            }
        }
        return selected;
    }

    private static boolean isRenderableCrackPath(ScreenLine line, int width, int height) {
        if (line == null || !line.crackPath() || line.alpha() <= 0.001f || line.strengthPixels() <= 0.001f) {
            return false;
        }

        float x0 = line.x0() * width;
        float y0 = line.y0() * height;
        float x1 = line.x1() * width;
        float y1 = line.y1() * height;
        if (hasNonFiniteCoordinates(x0, y0, x1, y1)) {
            return false;
        }

        float dx = x1 - x0;
        float dy = y1 - y0;
        return dx * dx + dy * dy >= 12.0f * 12.0f;
    }

    private static void ensureSourceTarget(int width, int height) {
        int padding = sourcePadding(width, height);
        int sourceWidth = width + padding * 2;
        int sourceHeight = height + padding * 2;

        if (sourceTarget == null) {
            sourceTarget = new TextureTarget(sourceWidth, sourceHeight, false, Minecraft.ON_OSX);
            sourceTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        } else if (sourceTarget.width != sourceWidth || sourceTarget.height != sourceHeight) {
            sourceTarget.resize(sourceWidth, sourceHeight, Minecraft.ON_OSX);
        }
        sourcePaddingPixels = padding;
    }

    private static int sourcePadding(int width, int height) {
        float linePadding = Math.max(0.0f, SlashCofig.ScreenBreak.DISTORTION_MAX_STRENGTH_PIXELS);
        float voronoiPadding = Math.max(0.0f, SlashCofig.ScreenBreak.VORONOI_WARP_PIXELS) * 1.6f;
        int requested = Mth.ceil(linePadding + voronoiPadding) + 8;
        int maxPadding = Math.max(1, Math.min(width, height));
        return Mth.clamp(requested, 1, maxPadding);
    }

    private static void renderDirectionalOffsetPass(RenderTarget main, ScreenLine line, VoronoiState voronoiState) {
        main.bindWrite(false);
        RenderSystem.viewport(0, 0, main.width, main.height);
        DriverBlendState.disable();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        withScreenProjection(main.width, main.height, () -> {
            RenderSystem.setShader(() -> shader);
            shader.setSampler("SceneSampler", sourceTarget.getColorTextureId());
            if (texelSizeUniform != null) texelSizeUniform.set(1.0f / main.width, 1.0f / main.height);
            if (sourceTexelSizeUniform != null) sourceTexelSizeUniform.set(1.0f / sourceTarget.width, 1.0f / sourceTarget.height);
            if (sourcePaddingUniform != null) sourcePaddingUniform.set((float) sourcePaddingPixels);
            if (lineStartUniform != null) lineStartUniform.set(line.x0(), line.y0());
            if (lineEndUniform != null) lineEndUniform.set(line.x1(), line.y1());
            if (lineWidthUniform != null) lineWidthUniform.set(Math.max(1.0f, line.widthPixels()));
            if (lineStrengthUniform != null) lineStrengthUniform.set(Math.max(0.0f, line.strengthPixels()));
            if (lineAlphaUniform != null) lineAlphaUniform.set(Mth.clamp(line.alpha(), 0.0f, 1.0f));
            if (directionFlipUniform != null) directionFlipUniform.set(line.safeDirectionFlip());
            if (centerFadeInnerUniform != null) centerFadeInnerUniform.set(Math.max(0.0f, SlashCofig.ScreenBreak.DISTORTION_CENTER_FADE_INNER_RADIUS));
            if (centerFadeOuterUniform != null) centerFadeOuterUniform.set(Math.max(0.0f, SlashCofig.ScreenBreak.DISTORTION_CENTER_FADE_OUTER_RADIUS));
            if (centerFadeMinStrengthUniform != null) centerFadeMinStrengthUniform.set(Mth.clamp(SlashCofig.ScreenBreak.DISTORTION_CENTER_FADE_MIN_STRENGTH, 0.0f, 1.0f));
            setVoronoiUniforms(voronoiState);

            BufferBuilder builder = FULLSCREEN_BUFFER.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(0.0f, (float) main.height, 0.0f).uv(0.0f, 0.0f).endVertex();
            builder.vertex((float) main.width, (float) main.height, 0.0f).uv(1.0f, 0.0f).endVertex();
            builder.vertex((float) main.width, 0.0f, 0.0f).uv(1.0f, 1.0f).endVertex();
            builder.vertex(0.0f, 0.0f, 0.0f).uv(0.0f, 1.0f).endVertex();
            BufferUploader.drawWithShader(builder.end());
        });

        DriverBlendState.enableDefault();
        main.bindWrite(false);
        RenderSystem.viewport(0, 0, main.width, main.height);
    }

    private static void setVoronoiUniforms(VoronoiState voronoiState) {
        VoronoiState safeState = voronoiState == null ? VoronoiState.NONE : voronoiState;
        float strength = Mth.clamp(safeState.strength() * SlashCofig.ScreenBreak.VORONOI_BLEND_AMOUNT, 0.0f, 1.0f);
        if (voronoiStrengthUniform != null) voronoiStrengthUniform.set(strength);
        if (voronoiAgeUniform != null) voronoiAgeUniform.set(Math.max(0.0f, safeState.age()));
        if (voronoiSeedUniform != null) voronoiSeedUniform.set((float) (Math.floorMod(safeState.seed(), 1000003L) + 1L));
        if (voronoiCellScaleUniform != null) voronoiCellScaleUniform.set(Math.max(1.0f, SlashCofig.ScreenBreak.VORONOI_CELL_SCALE));
        if (voronoiWarpPixelsUniform != null) voronoiWarpPixelsUniform.set(Math.max(0.0f, SlashCofig.ScreenBreak.VORONOI_WARP_PIXELS));
        if (voronoiCrackWidthUniform != null) voronoiCrackWidthUniform.set(Math.max(0.0f, SlashCofig.ScreenBreak.VORONOI_CRACK_WIDTH_PIXELS));
        if (voronoiCrackAlphaUniform != null) voronoiCrackAlphaUniform.set(Mth.clamp(SlashCofig.ScreenBreak.VORONOI_CRACK_ALPHA, 0.0f, 1.0f));
        if (voronoiCrackColorUniform != null) voronoiCrackColorUniform.set(Mth.clamp(SlashCofig.ScreenBreak.VORONOI_CRACK_COLOR, 0.0f, 1.0f));
        if (voronoiCrackRevealThresholdUniform != null) voronoiCrackRevealThresholdUniform.set(Math.max(0.0f, SlashCofig.ScreenBreak.VORONOI_CRACK_REVEAL_THRESHOLD));
        if (voronoiCenterInnerUniform != null) voronoiCenterInnerUniform.set(Math.max(0.0f, SlashCofig.ScreenBreak.VORONOI_CENTER_INNER_RADIUS));
        if (voronoiCenterOuterUniform != null) voronoiCenterOuterUniform.set(Math.max(0.0f, SlashCofig.ScreenBreak.VORONOI_CENTER_OUTER_RADIUS));
        if (voronoiCenterFalloffUniform != null) voronoiCenterFalloffUniform.set(Math.max(0.001f, SlashCofig.ScreenBreak.VORONOI_CENTER_FALLOFF_POWER));
        if (voronoiSpreadStartUniform != null) voronoiSpreadStartUniform.set(Math.max(0.0f, SlashCofig.ScreenBreak.VORONOI_SPREAD_START_RADIUS));
        if (voronoiSpreadEndUniform != null) voronoiSpreadEndUniform.set(Math.max(0.0f, SlashCofig.ScreenBreak.VORONOI_SPREAD_END_RADIUS));
        if (voronoiSpreadTicksUniform != null) voronoiSpreadTicksUniform.set(Math.max(0.001f, SlashCofig.ScreenBreak.VORONOI_SPREAD_TICKS));
        if (voronoiSpreadFeatherUniform != null) voronoiSpreadFeatherUniform.set(Math.max(0.001f, SlashCofig.ScreenBreak.VORONOI_SPREAD_FEATHER_RADIUS));
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

    private static boolean blitColorWithMirroredPadding(RenderTarget src, RenderTarget dst, int padding) {
        int p = Mth.clamp(padding, 1, Math.min(src.width, src.height));
        int w = src.width;
        int h = src.height;

        return FramebufferBlitter.blitColor(src, dst,
                new FramebufferBlitter.Region(0, 0, w, h, p, p, p + w, p + h),
                new FramebufferBlitter.Region(0, 0, p, h, p, p, 0, p + h),
                new FramebufferBlitter.Region(w - p, 0, w, h, p + w + p, p, p + w, p + h),
                new FramebufferBlitter.Region(0, 0, w, p, p, p, p + w, 0),
                new FramebufferBlitter.Region(0, h - p, w, h, p, p + h + p, p + w, p + h),
                new FramebufferBlitter.Region(0, 0, p, p, p, p, 0, 0),
                new FramebufferBlitter.Region(w - p, 0, w, p, p + w + p, p, p + w, 0),
                new FramebufferBlitter.Region(0, h - p, p, h, p, p + h + p, 0, p + h),
                new FramebufferBlitter.Region(w - p, h - p, w, h, p + w + p, p + h + p, p + w, p + h)
        );
    }

    private static boolean hasNonFiniteCoordinates(float x0, float y0, float x1, float y1) {
        return !Float.isFinite(x0) || !Float.isFinite(y0) || !Float.isFinite(x1) || !Float.isFinite(y1);
    }

    record ScreenLine(float x0, float y0, float x1, float y1, float widthPixels, float strengthPixels, float alpha, float directionFlip, boolean crackPath) {
        float safeDirectionFlip() {
            return directionFlip < 0.0f ? -1.0f : 1.0f;
        }
    }

    record VoronoiState(float strength, float age, long seed) {
        static final VoronoiState NONE = new VoronoiState(0.0f, 0.0f, 0L);
    }
}
