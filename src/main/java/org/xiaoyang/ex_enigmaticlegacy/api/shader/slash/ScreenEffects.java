package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.*;

public final class ScreenEffects {
    private static final float CENTER_X = 0.5f;
    private static final float CENTER_Y = 0.5f;
    private static final int MIN_SHARDS = 40;
    private static final int MAX_SHARDS = 200;
    private static final int SHARD_BUFFER_SIZE = 262144;
    private static final int FULLSCREEN_BUFFER_SIZE = 256;
    private static final int BURST_BUFFER_SIZE = 8192;
    private static final FullscreenBufferBuilder GLOW_BUFFER = new FullscreenBufferBuilder(SHARD_BUFFER_SIZE);
    private static final FullscreenBufferBuilder SHARD_BUFFER = new FullscreenBufferBuilder(SHARD_BUFFER_SIZE);
    private static final FullscreenBufferBuilder CRACK_BUFFER = new FullscreenBufferBuilder(SHARD_BUFFER_SIZE);
    private static final FullscreenBufferBuilder SCREEN_FX_BUFFER = new FullscreenBufferBuilder(FULLSCREEN_BUFFER_SIZE);
    private static final FullscreenBufferBuilder BREAK_BURST_BUFFER = new FullscreenBufferBuilder(BURST_BUFFER_SIZE);
    private static ShaderInstance shardShader;
    private static Uniform screenSizeUniform;
    private static Uniform texelSizeUniform;
    private static Uniform progressUniform;
    private static Uniform edgeUniform;
    private static Uniform mirrorUniform;
    private static ShaderInstance screenFxShader;
    private static Uniform fxTexelSizeUniform;
    private static Uniform fxProgressUniform;
    private static Uniform fxTimeUniform;
    private static Uniform fxMotionBlurVelocityUniform;
    private static Uniform fxMotionBlurIntensityUniform;
    private static Uniform fxMotionBlurFocusRadiusUniform;
    private static Uniform fxMotionBlurContractionUniform;
    private static Uniform fxPressureWarpStrengthUniform;
    private static Uniform fxShockwaveStrengthUniform;
    private static Uniform fxShockwaveProgressUniform;
    private static Uniform fxUomStrengthUniform;
    private static Uniform fxUomRateUniform;
    private static Uniform fxUomEdgeWeightUniform;
    private static Uniform fxUomContrastUniform;
    private static Uniform fxUomThresholdStrengthUniform;
    private static Uniform fxUomCenterFlashUniform;
    private static Uniform fxUomInvertStrobeUniform;
    private static Uniform fxUomMonoStrobeUniform;
    private static Uniform fxUomWhiteFlashUniform;
    private static Uniform fxUomVignetteStrengthUniform;
    private static Uniform fxEdgeStartUniform;
    private static Uniform fxEdgeEndUniform;
    private static Uniform fxSlashImpactStrengthUniform;
    private static Uniform fxSlashImpactDirectionUniform;
    private static Uniform fxSlashImpactOffsetPixelsUniform;
    private static Uniform fxSlashImpactMixUniform;
    private static Uniform fxSlashImpactCenterStrengthUniform;
    private static Uniform fxSlashImpactEdgeStartUniform;
    private static Uniform fxSlashImpactEdgeEndUniform;
    private static Uniform fxSlashImpactCompressionUniform;
    private static Uniform fxSlashImpactEdgeDarkenUniform;
    private static Uniform fxSlashImpactContrastUniform;
    private static ShaderInstance breakImpactShader;
    private static Uniform impactTexelSizeUniform;
    private static Uniform impactTimeUniform;
    private static Uniform impactFrameStrengthUniform;
    private static Uniform impactInvertStrengthUniform;
    private static Uniform impactThresholdStrengthUniform;
    private static Uniform impactCompressionStrengthUniform;
    private static Uniform impactCoreStrengthUniform;
    private static Uniform impactVignetteStrengthUniform;
    private static TextureTarget frozenTarget;
    private static TextureTarget postTarget;
    private static boolean pendingCapture;
    private static boolean active;
    private static long pendingSeed;
    private static long activeSeed;
    private static List<BreakLine> pendingBreakLines = List.of(BreakLine.horizontal());
    private static List<BreakLine> activeBreakLines = List.of(BreakLine.horizontal());
    private static int ageTicks;
    private static int ticksLeft;
    private static int pendingStartAge;
    private static float motionBlurLeadTicks;
    private static int targetWidth = -1;
    private static int targetHeight = -1;
    private static final List<GlassShard> shards = new ArrayList<>();

    private ScreenEffects() {
    }

    public static void shaderLoaded(ShaderInstance shader) {
        shardShader = shader;
        screenSizeUniform = shader.getUniform("ScreenSize");
        texelSizeUniform = shader.getUniform("TexelSize");
        progressUniform = shader.getUniform("Progress");
        edgeUniform = shader.getUniform("EdgeVisibility");
        mirrorUniform = shader.getUniform("MirrorStrength");
    }

    public static void screenFxShaderLoaded(ShaderInstance shader) {
        screenFxShader = shader;
        fxTexelSizeUniform = shader.getUniform("TexelSize");
        fxProgressUniform = shader.getUniform("Progress");
        fxTimeUniform = shader.getUniform("Time");
        fxMotionBlurVelocityUniform = shader.getUniform("MotionBlurVelocityPx");
        fxMotionBlurIntensityUniform = shader.getUniform("MotionBlurIntensity");
        fxMotionBlurFocusRadiusUniform = shader.getUniform("MotionBlurFocusRadius");
        fxMotionBlurContractionUniform = shader.getUniform("MotionBlurContraction");
        fxPressureWarpStrengthUniform = shader.getUniform("PressureWarpStrength");
        fxShockwaveStrengthUniform = shader.getUniform("ShockwaveStrength");
        fxShockwaveProgressUniform = shader.getUniform("ShockwaveProgress");
        fxUomStrengthUniform = shader.getUniform("UomStrength");
        fxUomRateUniform = shader.getUniform("UomRate");
        fxUomEdgeWeightUniform = shader.getUniform("UomEdgeWeight");
        fxUomContrastUniform = shader.getUniform("UomContrast");
        fxUomThresholdStrengthUniform = shader.getUniform("UomThresholdStrength");
        fxUomCenterFlashUniform = shader.getUniform("UomCenterFlash");
        fxUomInvertStrobeUniform = shader.getUniform("UomInvertStrobe");
        fxUomMonoStrobeUniform = shader.getUniform("UomMonoStrobe");
        fxUomWhiteFlashUniform = shader.getUniform("UomWhiteFlash");
        fxUomVignetteStrengthUniform = shader.getUniform("UomVignetteStrength");
        fxEdgeStartUniform = shader.getUniform("EdgeStart");
        fxEdgeEndUniform = shader.getUniform("EdgeEnd");
        fxSlashImpactStrengthUniform = shader.getUniform("SlashImpactStrength");
        fxSlashImpactDirectionUniform = shader.getUniform("SlashImpactDirection");
        fxSlashImpactOffsetPixelsUniform = shader.getUniform("SlashImpactOffsetPixels");
        fxSlashImpactMixUniform = shader.getUniform("SlashImpactMix");
        fxSlashImpactCenterStrengthUniform = shader.getUniform("SlashImpactCenterStrength");
        fxSlashImpactEdgeStartUniform = shader.getUniform("SlashImpactEdgeStart");
        fxSlashImpactEdgeEndUniform = shader.getUniform("SlashImpactEdgeEnd");
        fxSlashImpactCompressionUniform = shader.getUniform("SlashImpactCompression");
        fxSlashImpactEdgeDarkenUniform = shader.getUniform("SlashImpactEdgeDarken");
        fxSlashImpactContrastUniform = shader.getUniform("SlashImpactContrast");
    }

    public static void breakImpactShaderLoaded(ShaderInstance shader) {
        breakImpactShader = shader;
        impactTexelSizeUniform = shader.getUniform("TexelSize");
        impactTimeUniform = shader.getUniform("Time");
        impactFrameStrengthUniform = shader.getUniform("FrameStrength");
        impactInvertStrengthUniform = shader.getUniform("InvertStrength");
        impactThresholdStrengthUniform = shader.getUniform("ThresholdStrength");
        impactCompressionStrengthUniform = shader.getUniform("CompressionStrength");
        impactCoreStrengthUniform = shader.getUniform("CoreStrength");
        impactVignetteStrengthUniform = shader.getUniform("VignetteStrength");
    }

    static boolean trigger(long seed, float motionBlurLeadTicks) {
        return trigger(seed, motionBlurLeadTicks, 0.0f);
    }

    static boolean trigger(long seed, float motionBlurLeadTicks, float startAge) {
        if (shardShader == null) {
            clear();
            return false;
        }

        disposeFrozenTarget();
        disposePostTarget();
        pendingSeed = seed;
        pendingCapture = true;
        pendingBreakLines = List.of(BreakLine.horizontal());
        activeBreakLines = List.of(BreakLine.horizontal());
        active = false;
        ageTicks = 0;
        ticksLeft = SlashCofig.ScreenBreak.SHARD_MAX_LIFETIME_TICKS;
        pendingStartAge = PresentationPolicy.resumeAge(startAge);
        ScreenEffects.motionBlurLeadTicks = Math.max(0.0f, motionBlurLeadTicks);
        shards.clear();
        return true;
    }

    static void configurePendingBreakLines(List<ScreenBreakLine> lines) {
        if (!pendingCapture) return;
        if (lines == null || lines.isEmpty()) return;

        List<BreakLine> projected = new ArrayList<>(Math.min(lines.size(), SlashCofig.WorldSlash.MAX_SLASHES));
        for (ScreenBreakLine line : lines) {
            if (projected.size() >= SlashCofig.WorldSlash.MAX_SLASHES) break;
            BreakLine breakLine = BreakLine.from(line.x0(), line.y0(), line.x1(), line.y1());
            if (breakLine != null) {
                projected.add(breakLine);
            }
        }

        pendingBreakLines = projected.isEmpty() ? List.of(BreakLine.horizontal()) : List.copyOf(projected);
    }

    static void capturePending(RenderTarget main) {
        if (!pendingCapture) {
            return;
        }
        if (shardShader == null) {
            clear();
            return;
        }
        captureMainTarget(main, pendingSeed);
    }

    static void tick() {
        if (!active) return;

        ageTicks++;
        ticksLeft--;
        tickShards(ageTicks);

        if (ticksLeft <= 0 || allShardsDone()) {
            clear();
        }
    }

    static void clear() {
        pendingCapture = false;
        active = false;
        pendingBreakLines = List.of(BreakLine.horizontal());
        activeBreakLines = List.of(BreakLine.horizontal());
        pendingSeed = 0L;
        activeSeed = 0L;
        ageTicks = 0;
        ticksLeft = 0;
        pendingStartAge = 0;
        motionBlurLeadTicks = 0.0f;
        shards.clear();
        disposeFrozenTarget();
        disposePostTarget();
    }

    static boolean isActiveOrPending() {
        return active || pendingCapture;
    }

    static List<ScreenBreakLine> activeGlassCrackPathLines(float partialTick, int width, int height) {
        if (!active || shards.isEmpty() || width <= 0 || height <= 0) {
            return List.of();
        }
        if (width != targetWidth || height != targetHeight) {
            if (retargetActiveBreakFailed(width, height)) {
                return List.of();
            }
        }

        float renderAge = ageTicks + partialTick;
        int outputLimit = Math.max(1, SlashCofig.ScreenBreak.DISTORTION_LINE_COUNT * 2);
        List<CrackPathCandidate> candidates = new ArrayList<>(outputLimit * 4);
        Set<Long> seenEdges = new HashSet<>();
        for (GlassShard shard : shards) {
            shard.collectCrackPathLines(candidates, seenEdges, renderAge, width, height);
        }
        if (candidates.isEmpty()) {
            return List.of();
        }

        candidates.sort(Comparator.comparingDouble(CrackPathCandidate::score).reversed().thenComparingDouble(CrackPathCandidate::centerDistance));
        List<CrackPathCandidate> selectedCandidates = selectDistributedCrackPathCandidates(candidates, outputLimit, width, height);
        int count = Math.min(outputLimit, selectedCandidates.size());
        List<ScreenBreakLine> lines = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            lines.add(selectedCandidates.get(i).line());
        }
        return lines;
    }

    private static List<CrackPathCandidate> selectDistributedCrackPathCandidates(List<CrackPathCandidate> candidates, int limit, int width, int height) {
        if (candidates.isEmpty() || candidates.size() <= limit) {
            return candidates;
        }

        List<CrackPathCandidate> centerCandidates = new ArrayList<>();
        List<CrackPathCandidate> innerCandidates = new ArrayList<>();
        List<CrackPathCandidate> outerCandidates = new ArrayList<>();
        List<CrackPathCandidate> edgeCandidates = new ArrayList<>();
        float maxDistance = Mth.sqrt((float) width * width + (float) height * height) * 0.5f;
        for (CrackPathCandidate candidate : candidates) {
            float radial = Mth.clamp(candidate.centerDistance() / Math.max(1.0f, maxDistance), 0.0f, 1.0f);
            if (radial < 0.30f) {
                centerCandidates.add(candidate);
            } else if (radial < 0.55f) {
                innerCandidates.add(candidate);
            } else if (radial < 0.78f) {
                outerCandidates.add(candidate);
            } else {
                edgeCandidates.add(candidate);
            }
        }

        List<CrackPathCandidate> selected = new ArrayList<>(limit);
        int centerIndex = 0;
        int innerIndex = 0;
        int outerIndex = 0;
        int edgeIndex = 0;
        if (centerIndex < centerCandidates.size()) {
            selected.add(centerCandidates.get(centerIndex++));
        }
        while (selected.size() < limit) {
            boolean picked = false;
            if (innerIndex < innerCandidates.size()) {
                selected.add(innerCandidates.get(innerIndex++));
                picked = true;
            }
            if (outerIndex < outerCandidates.size() && selected.size() < limit) {
                selected.add(outerCandidates.get(outerIndex++));
                picked = true;
            }
            if (edgeIndex < edgeCandidates.size() && selected.size() < limit) {
                selected.add(edgeCandidates.get(edgeIndex++));
                picked = true;
            }
            if (centerIndex < centerCandidates.size() && selected.size() < limit) {
                selected.add(centerCandidates.get(centerIndex++));
                picked = true;
            }
            if (!picked) {
                break;
            }
        }
        return selected;
    }

    static boolean shouldReleaseScreenFreeze() {
        if (!active) {
            return false;
        }
        float releaseAge = SlashCofig.ScreenBreak.SHARD_LAUNCH_START_TICKS + SlashCofig.ScreenBreak.SCREEN_FREEZE_RELEASE_AFTER_GLASS_BREAK_TICKS;
        return ageTicks >= Math.round(releaseAge);
    }

    private static boolean allShardsDone() {
        if (targetWidth <= 0 || targetHeight <= 0 || ageTicks < SlashCofig.ScreenBreak.DURATION_TICKS) {
            return false;
        }

        for (GlassShard shard : shards) {
            if (!shard.isDone(ageTicks)) {
                return false;
            }
        }
        return true;
    }

    static void renderPost(float partialTick, float presentationAlpha) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || shardShader == null) {
            clear();
            return;
        }

        RenderTarget main = mc.getMainRenderTarget();
        capturePending(main);

        float safePresentationAlpha = Mth.clamp(presentationAlpha, 0.0f, 1.0f);
        if (!active || frozenTarget == null || shards.isEmpty() || safePresentationAlpha <= 0.001f) return;

        float renderAge = ageTicks + partialTick;
        renderShards(main, renderAge, safePresentationAlpha);
        renderFinalScreenFx(main, renderAge, safePresentationAlpha);
        renderBreakImpactFrame(main, renderAge, safePresentationAlpha);
        renderBreakBurstOverlay(main, renderAge, safePresentationAlpha);
    }

    static void renderPreBreakMotionBlur(RenderTarget main, float timelineAge, float timelineDuration, float presentationAlpha) {
        if (active || pendingCapture || screenFxShader == null) return;

        float safePresentationAlpha = Mth.clamp(presentationAlpha, 0.0f, 1.0f);
        float motionBlur = edgeMotionBlurEnvelope(timelineAge, timelineDuration) * SlashCofig.Quick.EDGE_MOTION_BLUR_SCALE * safePresentationAlpha;
        float pressureWarp = pressureWarpEnvelope(timelineAge, timelineDuration) * SlashCofig.Quick.PRESSURE_WARP_SCALE * safePresentationAlpha;
        float shockwaveProgress = Mth.clamp(progressBetween(timelineAge, 0.0f, timelineDuration), 0.0f, 1.0f);
        float shockwave = shockwaveEnvelope(shockwaveProgress) * SlashCofig.Quick.PRESSURE_WARP_SCALE * safePresentationAlpha;
        float animeImpactProgress = animeImpactProgress(timelineAge);
        float animeImpact = animeImpactVisibility(animeImpactProgress) * SlashCofig.Quick.UOM_SCREEN_FX_SCALE * safePresentationAlpha;
        if (motionBlur <= 0.001f && pressureWarp <= 0.001f && shockwave <= 0.001f && animeImpact <= 0.001f) return;

        renderScreenFx(main, timelineAge, 0.0f, motionBlur, edgeMotionBlurContraction(timelineAge, timelineDuration) * safePresentationAlpha, pressureWarp, shockwaveProgress, shockwave, animeImpact, animeImpactProgress);
    }

    static void renderSlashImpact(RenderTarget main, float strength, float directionX, float directionY) {
        if (active || pendingCapture || screenFxShader == null || strength <= 0.001f) return;

        float directionLength = Mth.sqrt(directionX * directionX + directionY * directionY);
        if (directionLength <= 0.001f) {
            directionX = 1.0f;
            directionY = 0.0f;
        } else {
            directionX /= directionLength;
            directionY /= directionLength;
        }
        renderScreenFx(main, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, Mth.clamp(strength, 0.0f, 1.0f), directionX, directionY);
    }

    static float motionBlurVisibility(float timelineAge, float timelineDuration) {
        return edgeMotionBlurEnvelope(timelineAge, timelineDuration);
    }

    static float pressureWarpVisibility(float timelineAge, float timelineDuration) {
        return pressureWarpEnvelope(timelineAge, timelineDuration);
    }

    static float waterWarpVisibility(float timelineAge, float timelineDuration) {
        float pressureWarp = pressureWarpEnvelope(timelineAge, timelineDuration);
        float shockwaveProgress = Mth.clamp(progressBetween(timelineAge, 0.0f, timelineDuration), 0.0f, 1.0f);
        float shockwave = shockwaveEnvelope(shockwaveProgress);
        return Mth.clamp(Math.max(pressureWarp, shockwave * 0.85f), 0.0f, 1.0f);
    }

    private static void captureMainTarget(RenderTarget main, long seed) {
        ensureFrozenTarget(main.width, main.height);
        if (frozenTarget == null) return;

        if (!FramebufferBlitter.blitColor(main, frozenTarget)) return;
        main.bindWrite(true);
        RenderSystem.viewport(0, 0, main.width, main.height);

        targetWidth = main.width;
        targetHeight = main.height;
        activeBreakLines = pendingBreakLines.isEmpty() ? List.of(BreakLine.horizontal()) : List.copyOf(pendingBreakLines);
        generateShards(seed, main.width, main.height);
        tickShardsToAge(pendingStartAge);
        pendingCapture = false;
        active = true;
        activeSeed = seed;
        ageTicks = pendingStartAge;
        ticksLeft = Math.max(1, SlashCofig.ScreenBreak.SHARD_MAX_LIFETIME_TICKS - pendingStartAge);
        pendingStartAge = 0;
    }

    private static boolean retargetActiveBreakFailed(int width, int height) {
        if (!active || width <= 0 || height <= 0 || frozenTarget == null) {
            return true;
        }
        if (width == targetWidth && height == targetHeight) {
            return false;
        }

        resizeFrozenTargetPreservingImage(width, height);
        if (frozenTarget == null || frozenTarget.width != width || frozenTarget.height != height) {
            return true;
        }

        int currentAge = ageTicks;
        targetWidth = width;
        targetHeight = height;
        generateShards(activeSeed, width, height);
        tickShardsToAge(currentAge);
        return false;
    }

    private static void tickShardsToAge(int age) {
        for (int tick = 1; tick <= age; tick++) {
            tickShards(tick);
        }
    }

    private static void tickShards(int age) {
        for (GlassShard shard : shards) {
            shard.tick(age);
        }
    }

    private static void resizeFrozenTargetPreservingImage(int width, int height) {
        if (frozenTarget == null || (frozenTarget.width == width && frozenTarget.height == height)) {
            return;
        }

        TextureTarget resized = new TextureTarget(width, height, false, Minecraft.ON_OSX);
        resized.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        if (!FramebufferBlitter.blitColor(frozenTarget, resized)) {
            resized.destroyBuffers();
            return;
        }
        frozenTarget.destroyBuffers();
        frozenTarget = resized;
    }

    private static void ensureFrozenTarget(int width, int height) {
        if (frozenTarget == null) {
            frozenTarget = new TextureTarget(width, height, false, Minecraft.ON_OSX);
            frozenTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        } else if (frozenTarget.width != width || frozenTarget.height != height) {
            frozenTarget.resize(width, height, Minecraft.ON_OSX);
        }
    }

    private static void disposeFrozenTarget() {
        if (frozenTarget != null) {
            frozenTarget.destroyBuffers();
            frozenTarget = null;
            targetWidth = -1;
            targetHeight = -1;
        }
    }

    private static void ensurePostTarget(int width, int height) {
        if (postTarget == null) {
            postTarget = new TextureTarget(width, height, false, Minecraft.ON_OSX);
            postTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        } else if (postTarget.width != width || postTarget.height != height) {
            postTarget.resize(width, height, Minecraft.ON_OSX);
        }
    }

    private static void disposePostTarget() {
        if (postTarget != null) {
            postTarget.destroyBuffers();
            postTarget = null;
        }
    }

    private static void generateShards(long seed, int width, int height) {
        shards.clear();
        Random random = new Random(seed ^ 0xD15EA5E5C4EEL);
        int count = Mth.clamp(Math.round(SlashCofig.ScreenBreak.SHARD_CELLS * SlashCofig.Quick.GLASS_SHARD_COUNT_SCALE), MIN_SHARDS, MAX_SHARDS);
        float aspect = width / (float) Math.max(height, 1);

        List<Point> sites = new ArrayList<>(count);
        addBreakLineSites(sites, count, random, activeBreakLines, width, height);
        for (int i = sites.size(); i < count; i++) {
            sites.add(randomSite(i, count, random, activeBreakLines, width, height));
        }

        for (int i = 0; i < sites.size(); i++) {
            List<Point> polygon = initialScreenPolygon();
            Point site = sites.get(i);
            Point siteAspect = toAspect(site, aspect);

            for (int j = 0; j < sites.size() && !polygon.isEmpty(); j++) {
                if (i == j) continue;
                Point otherAspect = toAspect(sites.get(j), aspect);
                float nx = otherAspect.x - siteAspect.x;
                float ny = otherAspect.y - siteAspect.y;
                float rhs = otherAspect.x * otherAspect.x + otherAspect.y * otherAspect.y - siteAspect.x * siteAspect.x - siteAspect.y * siteAspect.y;
                float a = 2.0f * nx * aspect;
                float b = 2.0f * ny;
                float c = -rhs;
                polygon = clipPolygon(polygon, a, b, c);
            }

            if (polygon.size() < 3 || Math.abs(area(polygon)) < 0.00004f) continue;
            shards.add(new GlassShard(polygon, random, width, height, activeBreakLines));
        }

        shards.sort(Comparator.comparingDouble(shard -> shard.depth));
    }

    private static void addBreakLineSites(List<Point> sites, int count, Random random, List<BreakLine> breakLines, int width, int height) {
        List<BreakLine> safeLines = breakLines == null || breakLines.isEmpty() ? List.of(BreakLine.horizontal()) : breakLines;
        int lineCount = safeLines.size();
        int lineSiteBudget = Math.min(count * 7 / 10, lineCount * 20);
        int pairsPerLine = Mth.clamp(lineSiteBudget / Math.max(2, lineCount * 2), 3, 10);
        float minDimension = Math.max(1.0f, Math.min(width, height));

        for (BreakLine breakLine : safeLines) {
            LineMetrics line = breakLine.metrics(width, height);
            float offset = minDimension * 0.014f;
            float alongSpan = Math.max(line.length * 0.94f, minDimension * 0.50f);
            for (int i = 0; i < pairsPerLine && sites.size() + 1 < count; i++) {
                float t = pairsPerLine == 1 ? 0.5f : i / (float) (pairsPerLine - 1);
                float along = (t - 0.5f) * alongSpan;
                float alongJitter = (random.nextFloat() - 0.5f) * minDimension * 0.012f;
                float sideJitter = (random.nextFloat() - 0.5f) * minDimension * 0.0025f;
                addScreenSite(sites, line.cx + line.ux * (along + alongJitter) + line.nx * (offset + sideJitter), line.cy + line.uy * (along + alongJitter) + line.ny * (offset + sideJitter), width, height);
                addScreenSite(sites, line.cx + line.ux * (along - alongJitter) - line.nx * (offset - sideJitter), line.cy + line.uy * (along - alongJitter) - line.ny * (offset - sideJitter), width, height);
            }
        }
    }

    private static void addScreenSite(List<Point> sites, float x, float y, int width, int height) {
        sites.add(new Point(
                Mth.clamp(x / Math.max(width, 1), -0.12f, 1.12f),
                Mth.clamp(y / Math.max(height, 1), -0.12f, 1.12f)
        ));
    }

    private static Point randomSite(int index, int count, Random random, List<BreakLine> breakLines, int width, int height) {
        boolean slashBand = index < count * 0.86f;
        LineMetrics line = selectBreakLine(index, random, breakLines, width, height, slashBand);
        float along;
        float cross;

        if (slashBand) {
            float centerSpread = random.nextFloat() < 0.58f
                    ? (float) Math.pow(random.nextFloat(), 0.72f) * (random.nextBoolean() ? -1.0f : 1.0f)
                    : random.nextFloat() * 2.0f - 1.0f;
            float alongSpan = Math.max(line.length * 0.58f, Math.min(width, height) * 0.42f);
            along = centerSpread * alongSpan;
            cross = (random.nextFloat() + random.nextFloat() - 1.0f) * Math.min(width, height) * 0.046f;
        } else {
            float side = random.nextBoolean() ? -1.0f : 1.0f;
            along = (random.nextFloat() * 2.0f - 1.0f) * Math.max(line.length * 0.72f, Math.max(width, height) * 0.40f);
            cross = side * (0.14f + (float) Math.pow(random.nextFloat(), 0.72f) * 0.50f) * Math.min(width, height);
            cross += (random.nextFloat() - 0.5f) * Math.min(width, height) * 0.065f;
        }

        float x = (line.cx + line.ux * along + line.nx * cross) / Math.max(width, 1);
        float y = (line.cy + line.uy * along + line.ny * cross) / Math.max(height, 1);
        x += (random.nextFloat() - 0.5f) * 0.026f;
        y += (random.nextFloat() - 0.5f) * 0.026f;
        return new Point(Mth.clamp(x, -0.10f, 1.10f), Mth.clamp(y, -0.10f, 1.10f));
    }

    private static LineMetrics selectBreakLine(int index, Random random, List<BreakLine> breakLines, int width, int height, boolean cycle) {
        List<BreakLine> safeLines = breakLines == null || breakLines.isEmpty() ? List.of(BreakLine.horizontal()) : breakLines;
        if (cycle) {
            return safeLines.get(index % safeLines.size()).metrics(width, height);
        }

        float total = 0.0f;
        for (BreakLine line : safeLines) {
            total += Math.max(1.0f, line.metrics(width, height).length);
        }

        float pick = random.nextFloat() * Math.max(total, 1.0f);
        for (BreakLine line : safeLines) {
            LineMetrics metrics = line.metrics(width, height);
            pick -= Math.max(1.0f, metrics.length);
            if (pick <= 0.0f) {
                return metrics;
            }
        }
        return safeLines.get(safeLines.size() - 1).metrics(width, height);
    }

    private static LineMetrics nearestBreakLine(float x, float y, List<BreakLine> breakLines, int width, int height) {
        List<BreakLine> safeLines = breakLines == null || breakLines.isEmpty() ? List.of(BreakLine.horizontal()) : breakLines;
        LineMetrics best = safeLines.get(0).metrics(width, height);
        float bestDistance = distanceToLineSegmentSq(x, y, best);

        for (int i = 1; i < safeLines.size(); i++) {
            LineMetrics candidate = safeLines.get(i).metrics(width, height);
            float distance = distanceToLineSegmentSq(x, y, candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    private static float distanceToLineSegmentSq(float x, float y, LineMetrics line) {
        float dx = x - line.cx;
        float dy = y - line.cy;
        float along = dx * line.ux + dy * line.uy;
        float cross = dx * line.nx + dy * line.ny;
        float outside = Math.max(0.0f, Math.abs(along) - line.length * 0.5f);
        return outside * outside + cross * cross;
    }

    private static List<Point> initialScreenPolygon() {
        List<Point> polygon = new ArrayList<>(4);
        polygon.add(new Point(0.0f, 0.0f));
        polygon.add(new Point(1.0f, 0.0f));
        polygon.add(new Point(1.0f, 1.0f));
        polygon.add(new Point(0.0f, 1.0f));
        return polygon;
    }

    private static Point toAspect(Point point, float aspect) {
        return new Point(point.x * aspect, point.y);
    }

    private static List<Point> clipPolygon(List<Point> input, float a, float b, float c) {
        List<Point> output = new ArrayList<>(input.size() + 1);
        Point previous = input.get(input.size() - 1);
        float previousValue = a * previous.x + b * previous.y + c;
        boolean previousInside = previousValue <= 0.000001f;

        for (Point current : input) {
            float currentValue = a * current.x + b * current.y + c;
            boolean currentInside = currentValue <= 0.000001f;

            if (currentInside) {
                if (!previousInside) {
                    output.add(intersection(previous, current, previousValue, currentValue));
                }
                output.add(current);
            } else if (previousInside) {
                output.add(intersection(previous, current, previousValue, currentValue));
            }

            previous = current;
            previousValue = currentValue;
            previousInside = currentInside;
        }

        return output;
    }

    private static Point intersection(Point a, Point b, float av, float bv) {
        float t = av / (av - bv);
        t = Mth.clamp(t, 0.0f, 1.0f);
        return new Point(Mth.lerp(t, a.x, b.x), Mth.lerp(t, a.y, b.y));
    }

    private static float area(List<Point> polygon) {
        float sum = 0.0f;
        for (int i = 0; i < polygon.size(); i++) {
            Point a = polygon.get(i);
            Point b = polygon.get((i + 1) % polygon.size());
            sum += a.x * b.y - b.x * a.y;
        }
        return sum * 0.5f;
    }

    private static Point centroid(List<Point> polygon) {
        float area2 = 0.0f;
        float cx = 0.0f;
        float cy = 0.0f;

        for (int i = 0; i < polygon.size(); i++) {
            Point a = polygon.get(i);
            Point b = polygon.get((i + 1) % polygon.size());
            float cross = a.x * b.y - b.x * a.y;
            area2 += cross;
            cx += (a.x + b.x) * cross;
            cy += (a.y + b.y) * cross;
        }

        if (Math.abs(area2) < 0.000001f) {
            for (Point point : polygon) {
                cx += point.x;
                cy += point.y;
            }
            float inv = 1.0f / polygon.size();
            return new Point(cx * inv, cy * inv);
        }

        float inv = 1.0f / (3.0f * area2);
        return new Point(cx * inv, cy * inv);
    }

    private static void renderShards(RenderTarget main, float renderAge, float presentationAlpha) {
        int width = main.width;
        int height = main.height;
        if (width <= 0 || height <= 0) {
            return;
        }
        if (width != targetWidth || height != targetHeight) {
            if (retargetActiveBreakFailed(width, height)) {
                return;
            }
        }
        if (frozenTarget == null) return;
        ensurePostTarget(width, height);
        if (postTarget == null) return;
        if (!FramebufferBlitter.blitColor(main, postTarget)) return;
        main.bindWrite(true);
        RenderSystem.viewport(0, 0, width, height);

        RenderSystem.disableScissor();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        DriverBlendState.enableDefault();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.backupProjectionMatrix();
        Matrix4f projection = new Matrix4f().setOrtho(0.0f, (float) width, (float) height, 0.0f, 1000.0f, 3000.0f);
        RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);

        var modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.setIdentity();
        modelView.translate(0.0f, 0.0f, -2000.0f);
        RenderSystem.applyModelViewMatrix();

        try {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            DriverBlendState.enableAdditive();
            BufferBuilder glowBuilder = GLOW_BUFFER.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            shards.sort(Comparator.comparingDouble(GlassShard::drawOrder));
            for (GlassShard shard : shards) {
                shard.renderGlow(glowBuilder, renderAge, width, height, presentationAlpha);
            }
            FullscreenBufferBuilder.drawWithShaderOrDiscard(glowBuilder);

            DriverBlendState.enableDefault();
            RenderSystem.setShader(() -> shardShader);
            shardShader.setSampler("SceneTex", frozenTarget.getColorTextureId());
            shardShader.setSampler("LiveSceneTex", postTarget.getColorTextureId());
            if (screenSizeUniform != null) screenSizeUniform.set((float) width, (float) height);
            if (texelSizeUniform != null) texelSizeUniform.set(1.0f / width, 1.0f / height);
            if (progressUniform != null) progressUniform.set(Mth.clamp(renderAge / SlashCofig.ScreenBreak.DURATION_TICKS, 0.0f, 1.0f));
            if (edgeUniform != null) edgeUniform.set(SlashCofig.ScreenBreak.EDGE_VISIBILITY * presentationAlpha);
            if (mirrorUniform != null) mirrorUniform.set(SlashCofig.ScreenBreak.SHARD_MIRROR_STRENGTH * presentationAlpha);

            BufferBuilder builder = SHARD_BUFFER.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR_TEX);

            for (GlassShard shard : shards) {
                shard.render(builder, renderAge, width, height, presentationAlpha);
            }

            FullscreenBufferBuilder.drawWithShaderOrDiscard(builder);

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            DriverBlendState.enableAdditive();
            BufferBuilder crackBuilder = CRACK_BUFFER.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (GlassShard shard : shards) {
                shard.renderCrackLight(crackBuilder, renderAge, width, height, presentationAlpha);
            }
            FullscreenBufferBuilder.drawWithShaderOrDiscard(crackBuilder);

            DriverBlendState.enableDefault();
            main.bindWrite(true);
            RenderSystem.viewport(0, 0, width, height);
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

    private static void renderFinalScreenFx(RenderTarget main, float renderAge, float presentationAlpha) {
        if (screenFxShader == null) return;

        float uom = (1.0f - smooth(Mth.clamp(renderAge / atLeast(SlashCofig.ScreenBreak.UOM_DURATION_TICKS, 1.0f), 0.0f, 1.0f)))
                * SlashCofig.Quick.UOM_SCREEN_FX_SCALE
                * SlashCofig.ScreenBreak.UOM_FINAL_SCALE;
        float motionBlurTimelineAge = motionBlurLeadTicks + renderAge;
        float motionBlurTimelineDuration = motionBlurLeadTicks + SlashCofig.ScreenBreak.DURATION_TICKS;
        float motionBlur = edgeMotionBlurEnvelope(motionBlurTimelineAge, motionBlurTimelineDuration) * SlashCofig.Quick.EDGE_MOTION_BLUR_SCALE;
        float motionBlurContraction = edgeMotionBlurContraction(motionBlurTimelineAge, motionBlurTimelineDuration);
        float pressureWarp = pressureWarpEnvelope(motionBlurTimelineAge, motionBlurTimelineDuration) * SlashCofig.Quick.PRESSURE_WARP_SCALE;
        float shockwaveProgress = Mth.clamp(progressBetween(motionBlurTimelineAge, 0.0f, motionBlurTimelineDuration), 0.0f, 1.0f);
        float shockwave = shockwaveEnvelope(shockwaveProgress) * SlashCofig.Quick.PRESSURE_WARP_SCALE;
        float animeImpactProgress = animeImpactProgress(motionBlurTimelineAge);
        float animeImpact = animeImpactVisibility(animeImpactProgress) * SlashCofig.Quick.UOM_SCREEN_FX_SCALE;
        uom *= presentationAlpha;
        motionBlur *= presentationAlpha;
        motionBlurContraction *= presentationAlpha;
        pressureWarp *= presentationAlpha;
        shockwave *= presentationAlpha;
        animeImpact *= presentationAlpha;
        if (uom <= 0.001f && motionBlur <= 0.001f && pressureWarp <= 0.001f && shockwave <= 0.001f && animeImpact <= 0.001f) return;

        renderScreenFx(main, renderAge, uom, motionBlur, motionBlurContraction, pressureWarp, shockwaveProgress, shockwave, animeImpact, animeImpactProgress);
    }

    private static void renderScreenFx(RenderTarget main, float renderAge, float uom, float motionBlur, float motionBlurContraction, float pressureWarp, float shockwaveProgress, float shockwave, float animeImpact, float animeImpactProgress) {
        renderScreenFx(main, renderAge, uom, motionBlur, motionBlurContraction, pressureWarp, shockwaveProgress, shockwave, animeImpact, animeImpactProgress, 0.0f, 1.0f, 0.0f);
    }

    private static void renderScreenFx(RenderTarget main, float renderAge, float uom, float motionBlur, float motionBlurContraction, float pressureWarp, float shockwaveProgress, float shockwave, float animeImpact, float animeImpactProgress, float slashImpactStrength, float slashImpactDirectionX, float slashImpactDirectionY) {
        ensurePostTarget(main.width, main.height);
        if (postTarget == null) return;

        if (!FramebufferBlitter.blitColor(main, postTarget)) return;
        main.bindWrite(false);
        RenderSystem.viewport(0, 0, main.width, main.height);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        DriverBlendState.disable();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.backupProjectionMatrix();
        Matrix4f projection = new Matrix4f().setOrtho(0.0f, (float) main.width, (float) main.height, 0.0f, 1000.0f, 3000.0f);
        RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);

        var modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.setIdentity();
        modelView.translate(0.0f, 0.0f, -2000.0f);
        RenderSystem.applyModelViewMatrix();

        try {
            RenderSystem.setShader(() -> screenFxShader);
            screenFxShader.setSampler("DiffuseSampler", postTarget.getColorTextureId());
            if (fxTexelSizeUniform != null) fxTexelSizeUniform.set(1.0f / main.width, 1.0f / main.height);
            if (fxProgressUniform != null) fxProgressUniform.set(Mth.clamp(renderAge / SlashCofig.ScreenBreak.DURATION_TICKS, 0.0f, 1.0f));
            if (fxTimeUniform != null) fxTimeUniform.set(renderAge / 20.0f);
            float motionVelocity = SlashCofig.ScreenBreak.EDGE_MOTION_BLUR_VELOCITY_PIXELS * motionBlur;
            if (fxMotionBlurVelocityUniform != null) fxMotionBlurVelocityUniform.set(motionVelocity, 0.0f);
            if (fxMotionBlurIntensityUniform != null) fxMotionBlurIntensityUniform.set(SlashCofig.ScreenBreak.EDGE_MOTION_BLUR_INTENSITY * motionBlur);
            if (fxMotionBlurFocusRadiusUniform != null) fxMotionBlurFocusRadiusUniform.set(SlashCofig.ScreenBreak.EDGE_MOTION_BLUR_FOCUS_RADIUS);
            if (fxMotionBlurContractionUniform != null) fxMotionBlurContractionUniform.set(motionBlurContraction);
            if (fxPressureWarpStrengthUniform != null) fxPressureWarpStrengthUniform.set(SlashCofig.ScreenBreak.PRESSURE_WARP_STRENGTH * pressureWarp);
            if (fxShockwaveStrengthUniform != null) fxShockwaveStrengthUniform.set(SlashCofig.ScreenBreak.PRESSURE_SHOCKWAVE_STRENGTH * shockwave);
            if (fxShockwaveProgressUniform != null) fxShockwaveProgressUniform.set(shockwaveProgress);
            if (fxUomStrengthUniform != null) fxUomStrengthUniform.set(SlashCofig.ScreenBreak.UOM_STRENGTH * uom);
            if (fxUomRateUniform != null) fxUomRateUniform.set(SlashCofig.ScreenBreak.UOM_RATE);
            if (fxUomEdgeWeightUniform != null) fxUomEdgeWeightUniform.set(SlashCofig.ScreenBreak.UOM_EDGE_WEIGHT);
            float impactNegative = animeImpact * animeImpactNegativePulse(animeImpactProgress);
            float impactWhite = animeImpact * animeImpactWhitePulse(animeImpactProgress);
            float impactMono = animeImpact * Math.max(animeImpactNegativePulse(animeImpactProgress), animeImpactWhitePulse(animeImpactProgress) * 0.72f);
            float impactDriver = Math.max(impactNegative, Math.max(impactWhite, impactMono));
            float contrastScale = Math.max(SlashCofig.ScreenBreak.UOM_FINAL_CONTRAST_SCALE,
                    impactDriver * SlashCofig.ScreenBreak.ANIME_IMPACT_CONTRAST_SCALE);
            if (fxUomContrastUniform != null) fxUomContrastUniform.set(SlashCofig.ScreenBreak.UOM_CONTRAST * contrastScale);
            if (fxUomThresholdStrengthUniform != null) fxUomThresholdStrengthUniform.set(SlashCofig.ScreenBreak.ANIME_IMPACT_THRESHOLD_STRENGTH * impactMono);
            if (fxUomCenterFlashUniform != null) fxUomCenterFlashUniform.set(SlashCofig.ScreenBreak.UOM_CENTER_FLASH * (uom * 0.10f + impactDriver * SlashCofig.ScreenBreak.ANIME_IMPACT_CENTER_FLASH_SCALE));
            if (fxUomInvertStrobeUniform != null) fxUomInvertStrobeUniform.set(SlashCofig.ScreenBreak.ANIME_IMPACT_INVERT_STRENGTH * impactNegative);
            if (fxUomMonoStrobeUniform != null) fxUomMonoStrobeUniform.set(SlashCofig.ScreenBreak.ANIME_IMPACT_MONO_STRENGTH * impactMono);
            if (fxUomWhiteFlashUniform != null) fxUomWhiteFlashUniform.set(SlashCofig.ScreenBreak.ANIME_IMPACT_WHITE_FLASH_STRENGTH * impactWhite);
            if (fxUomVignetteStrengthUniform != null) fxUomVignetteStrengthUniform.set(SlashCofig.ScreenBreak.UOM_VIGNETTE_STRENGTH);
            if (fxEdgeStartUniform != null) fxEdgeStartUniform.set(SlashCofig.ScreenBreak.SCREEN_CHROMA_EDGE_START);
            if (fxEdgeEndUniform != null) fxEdgeEndUniform.set(SlashCofig.ScreenBreak.SCREEN_CHROMA_EDGE_END);
            if (fxSlashImpactStrengthUniform != null) fxSlashImpactStrengthUniform.set(Mth.clamp(slashImpactStrength, 0.0f, 1.0f));
            if (fxSlashImpactDirectionUniform != null) fxSlashImpactDirectionUniform.set(slashImpactDirectionX, slashImpactDirectionY);
            if (fxSlashImpactOffsetPixelsUniform != null) fxSlashImpactOffsetPixelsUniform.set(SlashCofig.ScreenBreak.PerSlashImpact.CHROMA_OFFSET_PIXELS);
            if (fxSlashImpactMixUniform != null) fxSlashImpactMixUniform.set(SlashCofig.ScreenBreak.PerSlashImpact.CHROMA_MIX);
            if (fxSlashImpactCenterStrengthUniform != null) fxSlashImpactCenterStrengthUniform.set(SlashCofig.ScreenBreak.PerSlashImpact.CHROMA_CENTER_STRENGTH);
            if (fxSlashImpactEdgeStartUniform != null) fxSlashImpactEdgeStartUniform.set(SlashCofig.ScreenBreak.PerSlashImpact.CHROMA_EDGE_START);
            if (fxSlashImpactEdgeEndUniform != null) fxSlashImpactEdgeEndUniform.set(SlashCofig.ScreenBreak.PerSlashImpact.CHROMA_EDGE_END);
            if (fxSlashImpactCompressionUniform != null) fxSlashImpactCompressionUniform.set(SlashCofig.ScreenBreak.PerSlashImpact.PRESSURE_COMPRESSION);
            if (fxSlashImpactEdgeDarkenUniform != null) fxSlashImpactEdgeDarkenUniform.set(SlashCofig.ScreenBreak.PerSlashImpact.PRESSURE_EDGE_DARKEN);
            if (fxSlashImpactContrastUniform != null) fxSlashImpactContrastUniform.set(SlashCofig.ScreenBreak.PerSlashImpact.PRESSURE_CONTRAST);
            drawFullscreenQuad(main.width, main.height);
        } finally {
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            DriverBlendState.enableDefault();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            main.bindWrite(false);
            RenderSystem.viewport(0, 0, main.width, main.height);
        }
    }

    private static void drawFullscreenQuad(int width, int height) {
        BufferBuilder builder = SCREEN_FX_BUFFER.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(0.0f, (float) height, 0.0f).uv(0.0f, 0.0f).endVertex();
        builder.vertex((float) width, (float) height, 0.0f).uv(1.0f, 0.0f).endVertex();
        builder.vertex((float) width, 0.0f, 0.0f).uv(1.0f, 1.0f).endVertex();
        builder.vertex(0.0f, 0.0f, 0.0f).uv(0.0f, 1.0f).endVertex();
        BufferUploader.drawWithShader(builder.end());
    }

    private static void renderBreakImpactFrame(RenderTarget main, float renderAge, float presentationAlpha) {
        if (breakImpactShader == null) return;

        float burstAge = renderAge - SlashCofig.ScreenBreak.SHARD_LAUNCH_START_TICKS;
        float preTicks = atLeast(SlashCofig.ScreenBreak.BREAK_BURST_IMPACT_PRE_TICKS, 0.001f);
        float postTicks = atLeast(SlashCofig.ScreenBreak.BREAK_BURST_IMPACT_POST_TICKS, 0.001f);
        if (burstAge < -preTicks || burstAge > postTicks) return;

        float preEnvelope = 1.0f - smooth(Mth.clamp(-burstAge / preTicks, 0.0f, 1.0f));
        float postEnvelope = 1.0f - smooth(Mth.clamp(burstAge / postTicks, 0.0f, 1.0f));
        float envelope = burstAge < 0.0f ? preEnvelope : postEnvelope;
        float polarity = burstAge < 0.0f ? 1.0f : 0.72f;
        float frameStrength = envelope * SlashCofig.ScreenBreak.BREAK_BURST_IMPACT_STRENGTH * presentationAlpha;
        if (frameStrength <= 0.001f) return;

        ensurePostTarget(main.width, main.height);
        if (postTarget == null) return;

        if (!FramebufferBlitter.blitColor(main, postTarget)) return;
        main.bindWrite(false);
        RenderSystem.viewport(0, 0, main.width, main.height);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        DriverBlendState.disable();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.backupProjectionMatrix();
        Matrix4f projection = new Matrix4f().setOrtho(0.0f, (float) main.width, (float) main.height, 0.0f, 1000.0f, 3000.0f);
        RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);

        var modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.setIdentity();
        modelView.translate(0.0f, 0.0f, -2000.0f);
        RenderSystem.applyModelViewMatrix();

        try {
            RenderSystem.setShader(() -> breakImpactShader);
            breakImpactShader.setSampler("DiffuseSampler", postTarget.getColorTextureId());
            if (impactTexelSizeUniform != null) impactTexelSizeUniform.set(1.0f / main.width, 1.0f / main.height);
            if (impactTimeUniform != null) impactTimeUniform.set(renderAge / 20.0f);
            if (impactFrameStrengthUniform != null) impactFrameStrengthUniform.set(frameStrength);
            if (impactInvertStrengthUniform != null) impactInvertStrengthUniform.set(SlashCofig.ScreenBreak.BREAK_BURST_IMPACT_INVERT_STRENGTH * frameStrength * polarity);
            if (impactThresholdStrengthUniform != null) impactThresholdStrengthUniform.set(SlashCofig.ScreenBreak.BREAK_BURST_IMPACT_THRESHOLD_STRENGTH * frameStrength);
            if (impactCompressionStrengthUniform != null) impactCompressionStrengthUniform.set(SlashCofig.ScreenBreak.BREAK_BURST_IMPACT_COMPRESSION * frameStrength * (burstAge < 0.0f ? 1.0f : 0.48f));
            if (impactCoreStrengthUniform != null) impactCoreStrengthUniform.set(SlashCofig.ScreenBreak.BREAK_BURST_IMPACT_CORE_ALPHA * frameStrength);
            if (impactVignetteStrengthUniform != null) impactVignetteStrengthUniform.set(SlashCofig.ScreenBreak.BREAK_BURST_IMPACT_VIGNETTE_STRENGTH * frameStrength);
            drawFullscreenQuad(main.width, main.height);
        } finally {
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            DriverBlendState.enableDefault();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            main.bindWrite(false);
            RenderSystem.viewport(0, 0, main.width, main.height);
        }
    }

    private static void renderBreakBurstOverlay(RenderTarget main, float renderAge, float presentationAlpha) {
        float burstAge = renderAge - SlashCofig.ScreenBreak.SHARD_LAUNCH_START_TICKS;
        float duration = atLeast(SlashCofig.ScreenBreak.BREAK_BURST_DURATION_TICKS, 0.001f);
        if (burstAge < 0.0f || burstAge > duration) return;

        float progress = Mth.clamp(burstAge / duration, 0.0f, 1.0f);
        float expansion = easeOutQuart(progress);
        float flashFade = 1.0f - smooth(progressBetween(progress, 0.0f, 0.38f));
        float coreFade = 1.0f - smooth(progressBetween(progress, 0.18f, 0.82f));
        float ringFade = 1.0f - smooth(progressBetween(progress, 0.24f, 1.0f));
        float flashAlpha = SlashCofig.ScreenBreak.BREAK_BURST_FLASH_ALPHA * flashFade * presentationAlpha;
        float coreAlpha = SlashCofig.ScreenBreak.BREAK_BURST_CORE_ALPHA * coreFade * presentationAlpha;
        float ringAlpha = SlashCofig.ScreenBreak.BREAK_BURST_RING_ALPHA * ringFade * presentationAlpha;
        float rayAlpha = SlashCofig.ScreenBreak.BREAK_BURST_RAY_ALPHA * ringFade * presentationAlpha;
        if (flashAlpha <= 0.001f && coreAlpha <= 0.001f && ringAlpha <= 0.001f && rayAlpha <= 0.001f) return;

        main.bindWrite(false);
        RenderSystem.viewport(0, 0, main.width, main.height);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        DriverBlendState.enableDefault();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.backupProjectionMatrix();
        Matrix4f projection = new Matrix4f().setOrtho(0.0f, (float) main.width, (float) main.height, 0.0f, 1000.0f, 3000.0f);
        RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);

        var modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.setIdentity();
        modelView.translate(0.0f, 0.0f, -2000.0f);
        RenderSystem.applyModelViewMatrix();

        try {
            if (flashAlpha > 0.001f) {
                DriverBlendState.enableDefault();
                BufferBuilder flashBuilder = BREAK_BURST_BUFFER.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                addFullscreenColorQuad(flashBuilder, main.width, main.height, flashAlpha);
                FullscreenBufferBuilder.drawWithShaderOrDiscard(flashBuilder);
            }

            DriverBlendState.enableAdditive();
            float centerX = main.width * CENTER_X;
            float centerY = main.height * CENTER_Y;
            float maxX = Math.max(centerX, main.width - centerX);
            float maxY = Math.max(centerY, main.height - centerY);
            float maxRadius = Math.max(1.0f, Mth.sqrt(maxX * maxX + maxY * maxY));
            float startRadius = SlashCofig.ScreenBreak.BREAK_BURST_START_RADIUS * maxRadius;
            float endRadius = SlashCofig.ScreenBreak.BREAK_BURST_END_RADIUS * maxRadius;
            float radius = Mth.lerp(expansion, startRadius, endRadius);
            float ringWidth = Math.max(Math.min(main.width, main.height) * 0.035f, maxRadius * SlashCofig.ScreenBreak.BREAK_BURST_RING_WIDTH * (1.0f - progress * 0.42f));

            if (coreAlpha > 0.001f) {
                BufferBuilder coreBuilder = BREAK_BURST_BUFFER.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
                addBurstDisk(coreBuilder, centerX, centerY, Math.max(radius * 0.58f, ringWidth * 1.35f), coreAlpha);
                FullscreenBufferBuilder.drawWithShaderOrDiscard(coreBuilder);
            }

            if (ringAlpha > 0.001f || rayAlpha > 0.001f) {
                BufferBuilder burstBuilder = BREAK_BURST_BUFFER.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                addBurstRing(burstBuilder, centerX, centerY, radius, ringWidth, ringAlpha);
                addBurstRays(burstBuilder, centerX, centerY, radius, ringWidth, maxRadius, progress, rayAlpha);
                FullscreenBufferBuilder.drawWithShaderOrDiscard(burstBuilder);
            }
        } finally {
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            DriverBlendState.enableDefault();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            main.bindWrite(false);
            RenderSystem.viewport(0, 0, main.width, main.height);
        }
    }

    private static void addFullscreenColorQuad(BufferBuilder builder, int width, int height, float alpha) {
        float safeAlpha = Mth.clamp(alpha, 0.0f, 1.0f);
        builder.vertex(0.0f, (float) height, 0.0f).color(1.0f, 1.0f, 1.0f, safeAlpha).endVertex();
        builder.vertex((float) width, (float) height, 0.0f).color(1.0f, 1.0f, 1.0f, safeAlpha).endVertex();
        builder.vertex((float) width, 0.0f, 0.0f).color(1.0f, 1.0f, 1.0f, safeAlpha).endVertex();
        builder.vertex(0.0f, 0.0f, 0.0f).color(1.0f, 1.0f, 1.0f, safeAlpha).endVertex();
    }

    private static void addBurstDisk(BufferBuilder builder, float centerX, float centerY, float radius, float alpha) {
        int segments = 64;
        float safeAlpha = Mth.clamp(alpha, 0.0f, 1.0f);
        for (int i = 0; i < segments; i++) {
            float a0 = i * Mth.TWO_PI / segments;
            float a1 = (i + 1) * Mth.TWO_PI / segments;
            float x0 = centerX + Mth.cos(a0) * radius;
            float y0 = centerY + Mth.sin(a0) * radius;
            float x1 = centerX + Mth.cos(a1) * radius;
            float y1 = centerY + Mth.sin(a1) * radius;
            builder.vertex(centerX, centerY, 0.0f).color(1.0f, 1.0f, 1.0f, safeAlpha).endVertex();
            builder.vertex(x0, y0, 0.0f).color(1.0f, 1.0f, 1.0f, 0.0f).endVertex();
            builder.vertex(x1, y1, 0.0f).color(1.0f, 1.0f, 1.0f, 0.0f).endVertex();
        }
    }

    private static void addBurstRing(BufferBuilder builder, float centerX, float centerY, float radius, float width, float alpha) {
        float safeAlpha = Mth.clamp(alpha, 0.0f, 1.0f);
        if (safeAlpha <= 0.001f) return;

        float halfWidth = Math.max(0.001f, width * 0.5f);
        float coreHalfWidth = halfWidth * Mth.clamp(SlashCofig.ScreenBreak.BREAK_BURST_RING_CORE_FRACTION, 0.02f, 0.96f);
        float innerFeatherRadius = Math.max(0.0f, radius - halfWidth);
        float innerCoreRadius = Math.max(0.0f, radius - coreHalfWidth);
        float outerCoreRadius = radius + coreHalfWidth;
        float outerFeatherRadius = radius + halfWidth;

        addBurstBand(builder, centerX, centerY, innerFeatherRadius, innerCoreRadius, 0.0f, safeAlpha);
        addBurstBand(builder, centerX, centerY, innerCoreRadius, outerCoreRadius, safeAlpha, safeAlpha);
        addBurstBand(builder, centerX, centerY, outerCoreRadius, outerFeatherRadius, safeAlpha, 0.0f);
    }

    private static void addBurstBand(BufferBuilder builder, float centerX, float centerY, float innerRadius, float outerRadius, float innerAlpha, float outerAlpha) {
        if (outerRadius - innerRadius <= 0.001f) return;

        int segments = 72;
        for (int i = 0; i < segments; i++) {
            float a0 = i * Mth.TWO_PI / segments;
            float a1 = (i + 1) * Mth.TWO_PI / segments;
            float inX0 = centerX + Mth.cos(a0) * innerRadius;
            float inY0 = centerY + Mth.sin(a0) * innerRadius;
            float outX0 = centerX + Mth.cos(a0) * outerRadius;
            float outY0 = centerY + Mth.sin(a0) * outerRadius;
            float outX1 = centerX + Mth.cos(a1) * outerRadius;
            float outY1 = centerY + Mth.sin(a1) * outerRadius;
            float inX1 = centerX + Mth.cos(a1) * innerRadius;
            float inY1 = centerY + Mth.sin(a1) * innerRadius;
            builder.vertex(inX0, inY0, 0.0f).color(1.0f, 1.0f, 1.0f, innerAlpha).endVertex();
            builder.vertex(outX0, outY0, 0.0f).color(1.0f, 1.0f, 1.0f, outerAlpha).endVertex();
            builder.vertex(outX1, outY1, 0.0f).color(1.0f, 1.0f, 1.0f, outerAlpha).endVertex();
            builder.vertex(inX1, inY1, 0.0f).color(1.0f, 1.0f, 1.0f, innerAlpha).endVertex();
        }
    }

    private static void addBurstRays(BufferBuilder builder, float centerX, float centerY, float radius, float ringWidth, float maxRadius, float progress, float alpha) {
        float safeAlpha = Mth.clamp(alpha, 0.0f, 1.0f);
        if (safeAlpha <= 0.001f) return;

        int rays = 18;
        for (int i = 0; i < rays; i++) {
            float jitter = 0.5f + 0.5f * Mth.sin(i * 8.931f + 0.37f);
            float angle = i * Mth.TWO_PI / rays + Mth.sin(i * 12.9898f) * 0.18f;
            float dirX = Mth.cos(angle);
            float dirY = Mth.sin(angle);
            float sideX = -dirY;
            float inner = Math.max(0.0f, radius - ringWidth * (0.52f + jitter * 0.20f));
            float outer = Math.min(maxRadius * 1.30f, radius + maxRadius * (0.10f + jitter * 0.12f) * (0.62f + progress * 0.38f));
            float halfWidth = Math.max(2.0f, ringWidth * (0.045f + jitter * 0.035f) * (1.0f - progress * 0.30f));
            float startAlpha = safeAlpha * (0.42f + jitter * 0.28f);
            float endAlpha = 0.0f;

            float ax = centerX + dirX * inner + sideX * halfWidth;
            float ay = centerY + dirY * inner + dirX * halfWidth;
            float bx = centerX + dirX * inner - sideX * halfWidth;
            float by = centerY + dirY * inner - dirX * halfWidth;
            float cx = centerX + dirX * outer - sideX * halfWidth * 0.18f;
            float cy = centerY + dirY * outer - dirX * halfWidth * 0.18f;
            float dx = centerX + dirX * outer + sideX * halfWidth * 0.18f;
            float dy = centerY + dirY * outer + dirX * halfWidth * 0.18f;

            builder.vertex(ax, ay, 0.0f).color(1.0f, 1.0f, 1.0f, startAlpha).endVertex();
            builder.vertex(bx, by, 0.0f).color(1.0f, 1.0f, 1.0f, startAlpha).endVertex();
            builder.vertex(cx, cy, 0.0f).color(1.0f, 1.0f, 1.0f, endAlpha).endVertex();
            builder.vertex(dx, dy, 0.0f).color(1.0f, 1.0f, 1.0f, endAlpha).endVertex();
        }
    }

    private static float smooth(float value) {
        value = Mth.clamp(value, 0.0f, 1.0f);
        return value * value * (3.0f - 2.0f * value);
    }

    private static float easeOutQuart(float value) {
        value = Mth.clamp(value, 0.0f, 1.0f);
        float inverse = 1.0f - value;
        return 1.0f - inverse * inverse * inverse * inverse;
    }

    private static float atLeast(float value, float minimum) {
        return Math.max(value, minimum);
    }

    private static float progressBetween(float age, float start, float end) {
        float duration = end - start;
        if (duration <= 0.001f) {
            return age >= end ? 1.0f : 0.0f;
        }
        return (age - start) / duration;
    }

    private static float edgeMotionBlurEnvelope(float timelineAge, float timelineDuration) {
        float rampEnd = atLeast(SlashCofig.ScreenBreak.EDGE_MOTION_BLUR_RAMP_IN_TICKS, 0.001f);
        float fadeStart = Math.max(0.0f, timelineDuration - SlashCofig.ScreenBreak.EDGE_MOTION_BLUR_FADE_TICKS);
        float fadeEnd = fadeStart + atLeast(SlashCofig.ScreenBreak.EDGE_MOTION_BLUR_FADE_TICKS, 1.0f);
        float ramp = Mth.clamp(progressBetween(timelineAge, 0.0f, rampEnd), 0.0f, 1.0f);
        float fade = 1.0f - Mth.clamp(progressBetween(timelineAge, fadeStart, fadeEnd), 0.0f, 1.0f);
        return Mth.clamp(ramp * fade, 0.0f, 1.0f);
    }

    private static float edgeMotionBlurContraction(float timelineAge, float timelineDuration) {
        return Mth.clamp(progressBetween(timelineAge, 0.0f, timelineDuration), 0.0f, 1.0f);
    }

    private static float pressureWarpEnvelope(float timelineAge, float timelineDuration) {
        float rampEnd = atLeast(SlashCofig.ScreenBreak.EDGE_MOTION_BLUR_RAMP_IN_TICKS, 0.001f);
        float fadeStart = Math.max(0.0f, timelineDuration - SlashCofig.ScreenBreak.EDGE_MOTION_BLUR_FADE_TICKS);
        float fadeEnd = fadeStart + atLeast(SlashCofig.ScreenBreak.EDGE_MOTION_BLUR_FADE_TICKS, 1.0f);
        float ramp = smooth(progressBetween(timelineAge, 0.0f, rampEnd));
        float fade = 1.0f - Mth.clamp(progressBetween(timelineAge, fadeStart, fadeEnd), 0.0f, 1.0f);
        return Mth.clamp(ramp * fade, 0.0f, 1.0f);
    }

    private static float shockwaveEnvelope(float progress) {
        progress = Mth.clamp(progress, 0.0f, 1.0f);
        float attack = smooth(progressBetween(progress, 0.0f, 0.035f));
        float exitFade = 1.0f - smooth(progressBetween(progress, 0.40f, 0.58f));
        return Mth.clamp(attack * exitFade, 0.0f, 1.0f);
    }

    private static float animeImpactProgress(float timelineAge) {
        return Mth.clamp(timelineAge / atLeast(SlashCofig.ScreenBreak.ANIME_IMPACT_DURATION_TICKS, 0.001f), 0.0f, 1.0f);
    }

    private static float animeImpactVisibility(float progress) {
        progress = Mth.clamp(progress, 0.0f, 1.0f);
        float active = smooth(progressBetween(progress, 0.0f, 0.035f));
        float fade = 1.0f - smooth(progressBetween(progress, 0.900f, 1.0f));
        return Mth.clamp(active * fade, 0.0f, 1.0f);
    }

    private static float animeImpactNegativePulse(float progress) {
        float primary = animeImpactPulse(progress, 0.030f, 0.150f, 0.310f);
        float breakAccent = animeImpactPulse(progress, 0.660f, 0.780f, 0.980f) * 0.76f;
        return Math.max(primary, breakAccent);
    }

    private static float animeImpactWhitePulse(float progress) {
        return animeImpactPulse(progress, 0.330f, 0.450f, 0.610f) * 0.90f;
    }

    private static float animeImpactPulse(float progress, float start, float peak, float end) {
        float attack = smooth(progressBetween(progress, start, peak));
        float release = 1.0f - smooth(progressBetween(progress, peak, end));
        return Mth.clamp(attack * release, 0.0f, 1.0f);
    }

    private static float shardFade(float age) {
        return 1.0f - smooth((age - SlashCofig.ScreenBreak.SHARD_FADE_START_TICKS) / atLeast(SlashCofig.ScreenBreak.SHARD_FADE_TICKS, 1.0f));
    }

    private static float stagedCrackCoverage(float age) {
        float launchStart = atLeast(SlashCofig.ScreenBreak.SHARD_LAUNCH_START_TICKS, 1.0f);
        float stage30End = Math.min(1.5f, launchStart * 0.12f);
        float stage30HoldEnd = Math.min(launchStart, stage30End + 1.4f);
        float stage60End = Math.min(launchStart, stage30HoldEnd + 2.0f);
        float stage60HoldEnd = Math.min(launchStart, stage60End + 1.2f);
        float stage90End = Math.min(launchStart - 2.0f, stage60HoldEnd + 3.0f);
        if (age <= 0.0f) {
            return 0.0f;
        }
        if (age < stage30End) {
            return 0.30f * smooth(age / stage30End);
        }
        if (age < stage30HoldEnd) {
            return 0.30f;
        }
        if (age < stage60End) {
            return 0.30f + 0.30f * smooth(progressBetween(age, stage30HoldEnd, stage60End));
        }
        if (age < stage60HoldEnd) {
            return 0.60f;
        }
        if (age < stage90End) {
            return 0.60f + 0.34f * smooth(progressBetween(age, stage60HoldEnd, stage90End));
        }
        if (age < launchStart) {
            return 0.94f + 0.05f * smooth(progressBetween(age, stage90End, launchStart));
        }
        return 1.0f;
    }

    private record Point(float x, float y) {}
    private record PixelOffset(float x, float y) {}

    record ScreenBreakLine(float x0, float y0, float x1, float y1) {}

    private record CrackPathCandidate(ScreenBreakLine line, float score, float centerDistance) {}

    private record BreakLine(Point start, Point end) {
        private static BreakLine horizontal() {
            return new BreakLine(new Point(0.12f, CENTER_Y), new Point(0.88f, CENTER_Y));
        }

        private static BreakLine from(float x0, float y0, float x1, float y1) {
            Point start = new Point(Mth.clamp(x0, -0.35f, 1.35f), Mth.clamp(y0, -0.35f, 1.35f));
            Point end = new Point(Mth.clamp(x1, -0.35f, 1.35f), Mth.clamp(y1, -0.35f, 1.35f));
            float dx = end.x - start.x;
            float dy = end.y - start.y;
            if (dx * dx + dy * dy < 0.035f * 0.035f) {
                return null;
            }
            return new BreakLine(start, end);
        }

        private LineMetrics metrics(int width, int height) {
            float ax = start.x * width;
            float ay = start.y * height;
            float bx = end.x * width;
            float by = end.y * height;
            float dx = bx - ax;
            float dy = by - ay;
            float length = Mth.sqrt(dx * dx + dy * dy);
            if (length < 1.0f) {
                return new LineMetrics(width * 0.5f, height * 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, width * 0.76f);
            }
            float ux = dx / length;
            float uy = dy / length;
            return new LineMetrics((ax + bx) * 0.5f, (ay + by) * 0.5f, ux, uy, -uy, ux, length);
        }
    }

    private record LineMetrics(float cx, float cy, float ux, float uy, float nx, float ny, float length) {}

    private static final class GlassShard {
        private final List<Point> polygon;
        private final Point center;
        private final float baseX;
        private final float baseY;
        private final float velocityX;
        private final float velocityY;
        private final float velocityZ;
        private final float angularVelocityX;
        private final float angularVelocityY;
        private final float angularVelocityZ;
        private final float depth;
        private final float lift;
        private final float shade;
        private final float edge;
        private final float slashInfluence;
        private final float centerPullScale;
        private final float crackOrder;
        private final int revealTick;
        private final int launchTick;
        private float positionX;
        private float positionY;
        private float positionZ;
        private float rotationX;
        private float rotationY;
        private float rotationZ;
        private float scale = 1.0f;
        private float currentVelocityX;
        private float currentVelocityY;
        private float currentVelocityZ;
        private float currentAngularVelocityX;
        private float currentAngularVelocityY;
        private float currentAngularVelocityZ;
        private boolean launched;

        private GlassShard(List<Point> polygon, Random random, int width, int height, List<BreakLine> breakLines) {
            this.polygon = List.copyOf(polygon);
            this.center = centroid(polygon);
            this.baseX = center.x * width;
            this.baseY = center.y * height;
            this.positionX = baseX;
            this.positionY = baseY;

            LineMetrics line = nearestBreakLine(baseX, baseY, breakLines, width, height);
            float dx = baseX - line.cx;
            float dy = baseY - line.cy;
            float along = dx * line.ux + dy * line.uy;
            float cross = dx * line.nx + dy * line.ny;
            float horizontalOrder = Mth.clamp(Math.abs(along) / Math.max(line.length * 0.54f, Math.min(width, height) * 0.38f), 0.0f, 1.0f);
            float bandOrder = Mth.clamp(Math.abs(cross) / Math.max(Math.min(width, height) * 0.42f, 1.0f), 0.0f, 1.0f);
            float screenCenterX = width * CENTER_X;
            float screenCenterY = height * CENTER_Y;
            float centerDistanceDx = baseX - screenCenterX;
            float centerDistanceDy = baseY - screenCenterY;
            float maxCenterDistanceDx = Math.max(screenCenterX, width - screenCenterX);
            float maxCenterDistanceDy = Math.max(screenCenterY, height - screenCenterY);
            float maxCenterDistance = Math.max(1.0f, Mth.sqrt(maxCenterDistanceDx * maxCenterDistanceDx + maxCenterDistanceDy * maxCenterDistanceDy));
            float distanceOrder = Mth.clamp(Mth.sqrt(centerDistanceDx * centerDistanceDx + centerDistanceDy * centerDistanceDy) / maxCenterDistance, 0.0f, 1.0f);

            float slashInfluence = 1.0f - bandOrder;
            this.slashInfluence = slashInfluence;
            this.centerPullScale = centerPullScale(polygon, width, height);
            this.crackOrder = distanceOrder;
            float horizontalSign = Math.abs(along) > 0.001f ? Math.signum(along) : (random.nextBoolean() ? -1.0f : 1.0f);
            float verticalSign = Math.abs(cross) > Math.min(width, height) * 0.035f ? Math.signum(cross) : (random.nextBoolean() ? -1.0f : 1.0f);
            float pushAlong = horizontalSign * (0.24f + horizontalOrder * 0.52f) + (random.nextFloat() - 0.5f) * 0.26f;
            float pushNormal = verticalSign * (0.96f + slashInfluence * 0.42f) + (random.nextFloat() - 0.5f) * 0.18f;
            float pushX = line.ux * pushAlong + line.nx * pushNormal;
            float pushY = line.uy * pushAlong + line.ny * pushNormal;
            float pushLength = Math.max(0.001f, Mth.sqrt(pushX * pushX + pushY * pushY));
            float nx = pushX / pushLength;
            float ny = pushY / pushLength;
            float tangent = (random.nextFloat() - 0.5f) * (0.22f + slashInfluence * 0.24f);
            float tx = -ny;

            float moveScale = SlashCofig.Quick.GLASS_SHARD_EXPLOSION_SCALE;
            float speed = Math.min(width, height) * (0.0044f + random.nextFloat() * 0.0058f + slashInfluence * 0.0088f) * moveScale;
            this.velocityX = (nx + tx * tangent) * speed + horizontalSign * Math.min(width, height) * (0.0009f + horizontalOrder * 0.0012f) * moveScale;
            this.velocityY = (ny + nx * tangent) * speed - (0.12f + slashInfluence * 0.28f) * moveScale;
            this.velocityZ = (2.0f + random.nextFloat() * 4.2f + slashInfluence * 5.0f) * moveScale * SlashCofig.ScreenBreak.SHARD_DEPTH_SPEED;
            float tumbleScale = SlashCofig.ScreenBreak.SHARD_TUMBLE_SPEED * SlashCofig.Quick.GLASS_SHARD_TUMBLE_SCALE;
            this.angularVelocityX = ((random.nextFloat() - 0.5f) * 0.180f + Math.copySign(0.045f, verticalSign)) * (0.65f + slashInfluence) * tumbleScale;
            this.angularVelocityY = ((random.nextFloat() - 0.5f) * 0.150f + Math.copySign(0.035f, horizontalSign)) * (0.72f + slashInfluence) * tumbleScale;
            this.angularVelocityZ = ((random.nextFloat() - 0.5f) * 0.070f + Math.copySign(0.020f, horizontalSign)) * (0.70f + slashInfluence * 0.72f) * tumbleScale;
            this.depth = random.nextFloat();
            this.lift = 0.34f + random.nextFloat() * 0.58f;
            this.shade = 0.04f + random.nextFloat() * 0.16f;
            this.edge = 0.70f + random.nextFloat() * 0.30f;
            this.rotationX = (random.nextFloat() - 0.5f) * 0.20f;
            this.rotationY = (random.nextFloat() - 0.5f) * 0.20f;
            this.rotationZ = (random.nextFloat() - 0.5f) * 0.12f;
            this.revealTick = Math.round(distanceOrder * 2.0f + random.nextFloat() * 0.6f);
            int baseLaunch = Math.round(SlashCofig.ScreenBreak.SHARD_LAUNCH_START_TICKS);
            this.launchTick = baseLaunch + Math.round(distanceOrder * SlashCofig.ScreenBreak.SHARD_LAUNCH_SPREAD_TICKS + random.nextFloat() * 0.8f);
        }

        private void tick(int age) {
            if (age < launchTick) return;

            if (!launched) {
                launched = true;
                currentVelocityX = velocityX;
                currentVelocityY = velocityY;
                currentVelocityZ = velocityZ;
                currentAngularVelocityX = angularVelocityX;
                currentAngularVelocityY = angularVelocityY;
                currentAngularVelocityZ = angularVelocityZ;
            }

            float fallAge = age - launchTick;
            float fall = smooth((fallAge - 5.0f) / 18.0f);

            positionX += currentVelocityX;
            positionY += currentVelocityY;
            positionZ += currentVelocityZ;
            rotationX += currentAngularVelocityX;
            rotationY += currentAngularVelocityY;
            rotationZ += currentAngularVelocityZ;
            scale = Math.min(1.30f, scale + 0.0030f * lift);

            float gravity = SlashCofig.ScreenBreak.SHARD_GRAVITY * SlashCofig.Quick.GLASS_SHARD_GRAVITY_SCALE * (0.82f + lift * 0.42f) * fall;
            currentVelocityX *= Mth.lerp(fall, 0.986f, 0.972f);
            currentVelocityY = currentVelocityY * 0.996f + gravity;
            currentVelocityZ = currentVelocityZ * Mth.lerp(fall, 0.962f, 0.928f) - gravity * 0.0024f;
            currentAngularVelocityX *= 0.993f;
            currentAngularVelocityY *= 0.993f;
            currentAngularVelocityZ *= 0.990f;
        }

        private boolean isDone(int age) {
            return shardFade(age) <= 0.002f;
        }

        private double drawOrder() {
            return positionZ + depth * 8.0f;
        }

        private void render(BufferBuilder builder, float age, int width, int height, float presentationAlpha) {
            float reveal = crackReveal(age) * breakOpenReveal(age);
            if (reveal <= 0.002f) return;

            float explode = smooth((age - launchTick) / 13.0f);
            float fade = shardFade(age);
            float partial = age - (int) age;
            float renderRotationX = rotationX + (launched ? currentAngularVelocityX * partial : 0.0f);
            float renderRotationY = rotationY + (launched ? currentAngularVelocityY * partial : 0.0f);
            float renderRotationZ = rotationZ + (launched ? currentAngularVelocityZ * partial : 0.0f);
            float facing = facing(renderRotationX, renderRotationY);
            float alpha = reveal * fade * Mth.lerp(explode, 0.12f, 0.42f) * (0.38f + facing * 0.32f) * presentationAlpha;
            if (alpha <= 0.003f) return;

            float jitter = preLaunchJitter(age, reveal, explode);
            float jitterX = Mth.sin(age * 0.42f + depth * 17.0f) * jitter;
            float jitterY = Mth.cos(age * 0.37f + depth * 13.0f) * jitter;
            PixelOffset centerPull = centerPull(age, reveal, explode, width, height);
            float renderX = positionX + (launched ? currentVelocityX * partial : 0.0f) + jitterX + centerPull.x();
            float renderY = positionY + (launched ? currentVelocityY * partial : 0.0f) + jitterY + centerPull.y();
            float renderZ = positionZ + (launched ? currentVelocityZ * partial : 0.0f);
            float renderScale = scale + explode * lift * 0.045f;
            float angleShade = (1.0f - facing) * 0.42f;
            float materialShade = Mth.clamp(shade + angleShade + Math.max(renderZ, 0.0f) * 0.00035f, 0.0f, 1.0f);
            float materialEdge = Mth.clamp(edge + (1.0f - facing) * 0.24f, 0.0f, 1.0f);
            float materialLift = Mth.clamp(lift * (0.76f + explode * 0.28f), 0.0f, 1.0f);

            float centerU = center.x;
            float centerV = 1.0f - center.y;
            float centerAlpha = alpha * Mth.lerp(explode, 0.48f, 0.64f);
            float pixelScale = Math.max(0.72f, Math.min(width, height) / 720.0f);
            float thickness = SlashCofig.ScreenBreak.SHARD_THICKNESS_PIXELS * SlashCofig.Quick.GLASS_SHARD_THICKNESS_SCALE * pixelScale * (0.78f + lift * 0.44f) * (0.82f + explode * 0.18f);
            float frontZ = thickness * 0.5f;
            float backZ = -frontZ;
            float backAlpha = alpha * 0.22f;
            float sideAlpha = alpha * Mth.lerp(explode, 0.32f, 0.58f);
            float sideShade = Mth.clamp(materialShade + 0.42f, 0.0f, 1.0f);
            float sideLift = Mth.clamp(materialLift + 0.18f, 0.0f, 1.0f);

            for (int i = 0; i < polygon.size(); i++) {
                Point a = polygon.get(i);
                Point b = polygon.get((i + 1) % polygon.size());
                addVertex(builder, renderX, renderY, renderZ, baseX, baseY, center, frontZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height, 0.08f, materialShade, materialLift, centerAlpha, centerU, centerV);
                addVertex(builder, renderX, renderY, renderZ, baseX, baseY, a, frontZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height, materialEdge, materialShade, materialLift, alpha, a.x, 1.0f - a.y);
                addVertex(builder, renderX, renderY, renderZ, baseX, baseY, b, frontZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height, materialEdge, materialShade, materialLift, alpha, b.x, 1.0f - b.y);

                addVertex(builder, renderX, renderY, renderZ, baseX, baseY, center, backZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height, 0.18f, sideShade, materialLift, backAlpha, centerU, centerV);
                addVertex(builder, renderX, renderY, renderZ, baseX, baseY, b, backZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height, materialEdge, sideShade, materialLift, backAlpha, b.x, 1.0f - b.y);
                addVertex(builder, renderX, renderY, renderZ, baseX, baseY, a, backZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height, materialEdge, sideShade, materialLift, backAlpha, a.x, 1.0f - a.y);

                addVertex(builder, renderX, renderY, renderZ, baseX, baseY, a, frontZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height, 1.0f, sideShade, sideLift, sideAlpha, a.x, 1.0f - a.y);
                addVertex(builder, renderX, renderY, renderZ, baseX, baseY, a, backZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height, 1.0f, sideShade, sideLift, sideAlpha, a.x, 1.0f - a.y);
                addVertex(builder, renderX, renderY, renderZ, baseX, baseY, b, backZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height, 1.0f, sideShade, sideLift, sideAlpha, b.x, 1.0f - b.y);

                addVertex(builder, renderX, renderY, renderZ, baseX, baseY, a, frontZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height, 1.0f, sideShade, sideLift, sideAlpha, a.x, 1.0f - a.y);
                addVertex(builder, renderX, renderY, renderZ, baseX, baseY, b, backZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height, 1.0f, sideShade, sideLift, sideAlpha, b.x, 1.0f - b.y);
                addVertex(builder, renderX, renderY, renderZ, baseX, baseY, b, frontZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height, 1.0f, sideShade, sideLift, sideAlpha, b.x, 1.0f - b.y);
            }
        }

        private void renderGlow(BufferBuilder builder, float age, int width, int height, float presentationAlpha) {
            float reveal = crackReveal(age);
            if (reveal <= 0.002f) return;

            float explode = smooth((age - launchTick) / 13.0f);
            float fade = shardFade(age);
            float partial = age - (int) age;
            float renderRotationX = rotationX + (launched ? currentAngularVelocityX * partial : 0.0f);
            float renderRotationY = rotationY + (launched ? currentAngularVelocityY * partial : 0.0f);
            float renderRotationZ = rotationZ + (launched ? currentAngularVelocityZ * partial : 0.0f);
            float facing = facing(renderRotationX, renderRotationY);
            float lineBoost = 0.72f + slashInfluence * 0.50f;
            float alpha = reveal * fade * Mth.lerp(explode, 0.26f, 0.92f) * (0.54f + facing * 0.46f) * SlashCofig.ScreenBreak.SHARD_BLOOM_ALPHA * lineBoost * presentationAlpha;
            if (alpha <= 0.003f) return;

            float jitter = preLaunchJitter(age, reveal, explode);
            float jitterX = Mth.sin(age * 0.42f + depth * 17.0f) * jitter;
            float jitterY = Mth.cos(age * 0.37f + depth * 13.0f) * jitter;
            PixelOffset centerPull = centerPull(age, reveal, explode, width, height);
            float renderX = positionX + (launched ? currentVelocityX * partial : 0.0f) + jitterX + centerPull.x();
            float renderY = positionY + (launched ? currentVelocityY * partial : 0.0f) + jitterY + centerPull.y();
            float renderZ = positionZ + (launched ? currentVelocityZ * partial : 0.0f);
            float renderScale = scale + explode * lift * 0.045f;

            float pixelScale = Math.max(0.72f, Math.min(width, height) / 720.0f);
            float thickness = SlashCofig.ScreenBreak.SHARD_THICKNESS_PIXELS * SlashCofig.Quick.GLASS_SHARD_THICKNESS_SCALE * pixelScale * (0.78f + lift * 0.44f) * (0.82f + explode * 0.18f);
            float frontZ = thickness * 0.5f;
            float glowWidth = SlashCofig.ScreenBreak.SHARD_BLOOM_PIXELS * pixelScale * (0.72f + lift * 0.48f) * (0.82f + explode * 0.18f);
            float blurTicks = launched ? Math.min(SlashCofig.ScreenBreak.SHARD_MOTION_BLUR_TICKS, Math.max(0.0f, age - launchTick)) : 0.0f;
            boolean blurActive = blurTicks > 0.05f;
            float trailX = renderX;
            float trailY = renderY;
            float trailZ = renderZ;
            float trailRotationX = renderRotationX;
            float trailRotationY = renderRotationY;
            float trailRotationZ = renderRotationZ;
            float trailScale = renderScale;
            float trailFrontZ = frontZ;
            if (blurActive) {
                float trailAge = age - blurTicks;
                float trailReveal = crackReveal(trailAge);
                float trailExplode = smooth((trailAge - launchTick) / 13.0f);
                float trailJitter = preLaunchJitter(trailAge, trailReveal, trailExplode);
                PixelOffset trailCenterPull = centerPull(trailAge, trailReveal, trailExplode, width, height);
                trailX = positionX + currentVelocityX * (partial - blurTicks) + Mth.sin(trailAge * 0.42f + depth * 17.0f) * trailJitter + trailCenterPull.x();
                trailY = positionY + currentVelocityY * (partial - blurTicks) + Mth.cos(trailAge * 0.37f + depth * 13.0f) * trailJitter + trailCenterPull.y();
                trailZ = positionZ + currentVelocityZ * (partial - blurTicks);
                trailRotationX = renderRotationX - currentAngularVelocityX * blurTicks;
                trailRotationY = renderRotationY - currentAngularVelocityY * blurTicks;
                trailRotationZ = renderRotationZ - currentAngularVelocityZ * blurTicks;
                trailScale = Math.max(0.72f, renderScale - 0.0030f * lift * blurTicks);
                float trailThickness = SlashCofig.ScreenBreak.SHARD_THICKNESS_PIXELS * SlashCofig.Quick.GLASS_SHARD_THICKNESS_SCALE * pixelScale * (0.78f + lift * 0.44f) * (0.82f + trailExplode * 0.18f);
                trailFrontZ = trailThickness * 0.5f;
            }

            for (int i = 0; i < polygon.size(); i++) {
                Point a = polygon.get(i);
                Point b = polygon.get((i + 1) % polygon.size());
                ProjectedPoint pa = projectPoint(renderX, renderY, renderZ, baseX, baseY, a, frontZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height);
                ProjectedPoint pb = projectPoint(renderX, renderY, renderZ, baseX, baseY, b, frontZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height);
                if (blurActive) {
                    ProjectedPoint ta = projectPoint(trailX, trailY, trailZ, baseX, baseY, a, trailFrontZ, trailRotationX, trailRotationY, trailRotationZ, trailScale, width, height);
                    ProjectedPoint tb = projectPoint(trailX, trailY, trailZ, baseX, baseY, b, trailFrontZ, trailRotationX, trailRotationY, trailRotationZ, trailScale, width, height);
                    float sweep = Math.max(screenDistance(pa, ta), screenDistance(pb, tb));
                    if (sweep > 0.25f) {
                        float motionAlpha = alpha * SlashCofig.ScreenBreak.SHARD_MOTION_BLUR_ALPHA * Mth.clamp(sweep / Math.max(18.0f * pixelScale, 1.0f), 0.16f, 1.0f);
                        addGlowQuad(builder, ta, tb, pb, pa, 0.34f, 0.72f, motionAlpha * 0.22f);
                        addGlowQuad(builder, ta, tb, pb, pa, 1.0f, 0.55f, motionAlpha * 0.11f);
                    }
                }
                addGlowRibbon(builder, pa, pb, glowWidth * 1.45f, 0.42f, 0.78f, alpha * 0.16f);
                addGlowRibbon(builder, pa, pb, glowWidth * 0.58f, 1.0f, 0.72f, alpha * 0.14f);
            }
        }

        private void renderCrackLight(BufferBuilder builder, float age, int width, int height, float presentationAlpha) {
            float reveal = crackReveal(age);
            if (reveal <= 0.002f) return;

            float explode = smooth((age - launchTick) / 13.0f);
            float fade = shardFade(age);
            float partial = age - (int) age;
            float renderRotationX = rotationX + (launched ? currentAngularVelocityX * partial : 0.0f);
            float renderRotationY = rotationY + (launched ? currentAngularVelocityY * partial : 0.0f);
            float renderRotationZ = rotationZ + (launched ? currentAngularVelocityZ * partial : 0.0f);
            float facing = facing(renderRotationX, renderRotationY);
            float crackOpen = 1.0f - smooth((age - launchTick + 2.0f) / 24.0f);
            float lineBoost = 0.70f + slashInfluence * 0.62f;
            float alpha = reveal * fade * Mth.lerp(explode, 0.42f, 0.88f) * (0.64f + facing * 0.36f) * SlashCofig.ScreenBreak.SHARD_CRACK_LIGHT_ALPHA * (0.44f + crackOpen * 0.56f) * lineBoost * presentationAlpha;
            if (alpha <= 0.003f) return;

            float jitter = preLaunchJitter(age, reveal, explode);
            float jitterX = Mth.sin(age * 0.42f + depth * 17.0f) * jitter;
            float jitterY = Mth.cos(age * 0.37f + depth * 13.0f) * jitter;
            PixelOffset centerPull = centerPull(age, reveal, explode, width, height);
            float renderX = positionX + (launched ? currentVelocityX * partial : 0.0f) + jitterX + centerPull.x();
            float renderY = positionY + (launched ? currentVelocityY * partial : 0.0f) + jitterY + centerPull.y();
            float renderZ = positionZ + (launched ? currentVelocityZ * partial : 0.0f);
            float renderScale = scale + explode * lift * 0.045f;
            float pixelScale = Math.max(0.72f, Math.min(width, height) / 720.0f);
            float thickness = SlashCofig.ScreenBreak.SHARD_THICKNESS_PIXELS * SlashCofig.Quick.GLASS_SHARD_THICKNESS_SCALE * pixelScale * (0.78f + lift * 0.44f) * (0.82f + explode * 0.18f);
            float frontZ = thickness * 0.5f;
            float crackBeamLength = SlashCofig.ScreenBreak.SHARD_CRACK_LIGHT_LENGTH_PIXELS * pixelScale * (0.70f + lift * 0.42f) * (1.08f - explode * 0.34f);
            float crackBeamWidth = SlashCofig.ScreenBreak.SHARD_CRACK_LIGHT_WIDTH_PIXELS * pixelScale * (0.82f + lift * 0.34f);
            float coreWidth = Math.max(1.6f * pixelScale, crackBeamWidth * 0.16f);

            ProjectedPoint pc = projectPoint(renderX, renderY, renderZ, baseX, baseY, center, frontZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height);
            for (int i = 0; i < polygon.size(); i++) {
                Point a = polygon.get(i);
                Point b = polygon.get((i + 1) % polygon.size());
                ProjectedPoint pa = projectPoint(renderX, renderY, renderZ, baseX, baseY, a, frontZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height);
                ProjectedPoint pb = projectPoint(renderX, renderY, renderZ, baseX, baseY, b, frontZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height);
                if (screenDistance(pa, pb) <= 1.25f) continue;

                float midX = (pa.x + pb.x) * 0.5f;
                float midY = (pa.y + pb.y) * 0.5f;
                float dirX = midX - pc.x;
                float dirY = midY - pc.y;
                float dirLength = Mth.sqrt(dirX * dirX + dirY * dirY);
                if (dirLength <= 0.001f) {
                    float edgeX = pb.x - pa.x;
                    float edgeY = pb.y - pa.y;
                    float edgeLength = Mth.sqrt(edgeX * edgeX + edgeY * edgeY);
                    if (edgeLength > 0.001f) {
                        dirX = -edgeY / edgeLength;
                        dirY = edgeX / edgeLength;
                        dirLength = 1.0f;
                    }
                }
                if (dirLength <= 0.001f) continue;

                dirX /= dirLength;
                dirY /= dirLength;
                addCrackLightBeam(builder, pa, pb, dirX, dirY, crackBeamLength * 1.20f, crackBeamWidth * 1.70f, 0.34f, 0.70f, alpha * 0.18f);
                addCrackLightBeam(builder, pa, pb, dirX, dirY, crackBeamLength * 0.56f, crackBeamWidth * 0.68f, 0.92f, 0.98f, alpha * 0.34f);
                addGlowRibbon(builder, pa, pb, coreWidth, 0.98f, 1.0f, alpha * 0.74f);
            }
        }

        private void collectCrackPathLines(List<CrackPathCandidate> output, Set<Long> seenEdges, float age, int width, int height) {
            float fade = shardFade(age);
            if (fade <= 0.002f) return;

            float reveal = Math.max(crackReveal(age), 0.12f);
            float explode = smooth((age - launchTick) / 13.0f);
            float partial = age - (int) age;
            float renderRotationX = rotationX + (launched ? currentAngularVelocityX * partial : 0.0f);
            float renderRotationY = rotationY + (launched ? currentAngularVelocityY * partial : 0.0f);
            float renderRotationZ = rotationZ + (launched ? currentAngularVelocityZ * partial : 0.0f);
            float jitter = preLaunchJitter(age, reveal, explode);
            float jitterX = Mth.sin(age * 0.42f + depth * 17.0f) * jitter;
            float jitterY = Mth.cos(age * 0.37f + depth * 13.0f) * jitter;
            PixelOffset centerPull = centerPull(age, reveal, explode, width, height);
            float renderX = positionX + (launched ? currentVelocityX * partial : 0.0f) + jitterX + centerPull.x();
            float renderY = positionY + (launched ? currentVelocityY * partial : 0.0f) + jitterY + centerPull.y();
            float renderZ = positionZ + (launched ? currentVelocityZ * partial : 0.0f);
            float renderScale = scale + explode * lift * 0.045f;
            float pixelScale = Math.max(0.72f, Math.min(width, height) / 720.0f);
            float thickness = SlashCofig.ScreenBreak.SHARD_THICKNESS_PIXELS * SlashCofig.Quick.GLASS_SHARD_THICKNESS_SCALE * pixelScale * (0.78f + lift * 0.44f) * (0.82f + explode * 0.18f);
            float frontZ = thickness * 0.5f;

            for (int i = 0; i < polygon.size(); i++) {
                Point a = polygon.get(i);
                Point b = polygon.get((i + 1) % polygon.size());
                if (isOriginalScreenBorderEdge(a, b)) continue;

                ProjectedPoint pa = projectPoint(renderX, renderY, renderZ, baseX, baseY, a, frontZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height);
                ProjectedPoint pb = projectPoint(renderX, renderY, renderZ, baseX, baseY, b, frontZ, renderRotationX, renderRotationY, renderRotationZ, renderScale, width, height);
                float length = screenDistance(pa, pb);
                if (length <= 12.0f || outsideSameScreenSide(pa, pb, width, height)) continue;

                long key = crackEdgeKey(pa, pb);
                if (!seenEdges.add(key)) continue;

                float x0 = Mth.clamp(pa.x / Math.max(width, 1), -0.12f, 1.12f);
                float y0 = Mth.clamp(pa.y / Math.max(height, 1), -0.12f, 1.12f);
                float x1 = Mth.clamp(pb.x / Math.max(width, 1), -0.12f, 1.12f);
                float y1 = Mth.clamp(pb.y / Math.max(height, 1), -0.12f, 1.12f);
                float midpointX = (pa.x + pb.x) * 0.5f;
                float midpointY = (pa.y + pb.y) * 0.5f;
                float centerDx = midpointX - width * CENTER_X;
                float centerDy = midpointY - height * CENTER_Y;
                float centerDistance = Mth.sqrt(centerDx * centerDx + centerDy * centerDy);
                float lengthScore = Mth.clamp(length / Math.max(Math.min(width, height) * 0.22f, 1.0f), 0.0f, 1.0f);
                float edgeDistance = Math.min(Math.min(midpointX, width - midpointX), Math.min(midpointY, height - midpointY));
                float edgeGuard = Math.max(10.0f, Math.min(width, height) * 0.045f);
                float edgeWeight = smooth((edgeDistance + edgeGuard * 0.35f) / edgeGuard);
                float noise = crackEdgeNoise(pa, pb) * 0.08f;
                float score = reveal * fade * (0.44f + slashInfluence * 0.56f) * (0.48f + lengthScore * 0.52f) * edgeWeight + noise;
                output.add(new CrackPathCandidate(new ScreenBreakLine(x0, y0, x1, y1), score, centerDistance));
            }
        }

        private static void addVertex(BufferBuilder builder, float renderX, float renderY, float renderZ, float baseX, float baseY, Point point, float localZ, float rotationX, float rotationY, float rotationZ, float scale, int width, int height, float edge, float shade, float lift, float alpha, float u, float v) {
            ProjectedPoint projected = projectPoint(renderX, renderY, renderZ, baseX, baseY, point, localZ, rotationX, rotationY, rotationZ, scale, width, height);
            builder.vertex(projected.x, projected.y, projected.z).color(Mth.clamp(edge, 0.0f, 1.0f), Mth.clamp(shade, 0.0f, 1.0f), Mth.clamp(lift, 0.0f, 1.0f), Mth.clamp(alpha, 0.0f, 1.0f)).uv(u, v).endVertex();
        }

        private float preLaunchJitter(float age, float reveal, float explode) {
            float settle = 1.0f - smooth((age - revealTick) / 8.0f);
            return (1.0f - explode) * reveal * settle * 0.22f;
        }

        private PixelOffset centerPull(float age, float reveal, float explode, int width, int height) {
            float release = 1.0f - smooth((age - launchTick + 2.0f) / 4.0f);
            float ramp = smooth((age - revealTick) / 4.0f);
            float pressure = Mth.clamp(reveal * ramp * release * (1.0f - explode), 0.0f, 1.0f);
            if (pressure <= 0.001f) {
                return new PixelOffset(0.0f, 0.0f);
            }

            float centerX = width * CENTER_X;
            float centerY = height * CENTER_Y;
            float dx = centerX - baseX;
            float dy = centerY - baseY;
            float distance = Mth.sqrt(dx * dx + dy * dy);
            if (distance <= 0.001f) {
                return new PixelOffset(0.0f, 0.0f);
            }

            float maxDx = Math.max(centerX, width - centerX);
            float maxDy = Math.max(centerY, height - centerY);
            float centerDistance = Mth.clamp(distance / Math.max(1.0f, Mth.sqrt(maxDx * maxDx + maxDy * maxDy)), 0.0f, 1.0f);
            float pixelScale = Math.max(0.72f, Math.min(width, height) / 720.0f);
            float pull = SlashCofig.ScreenBreak.SHARD_CENTER_PULL_PIXELS
                    * pixelScale
                    * pressure
                    * centerPullScale
                    * (0.70f + centerDistance * 0.45f);
            return new PixelOffset(dx / distance * pull, dy / distance * pull);
        }

        private static float centerPullScale(List<Point> polygon, int width, int height) {
            float minEdgeDistance = Float.MAX_VALUE;
            for (Point point : polygon) {
                float x = point.x * width;
                float y = point.y * height;
                float edgeDistance = Math.min(Math.min(x, width - x), Math.min(y, height - y));
                minEdgeDistance = Math.min(minEdgeDistance, edgeDistance);
            }

            float guard = Math.max(8.0f, Math.min(width, height) * 0.16f);
            return smooth((minEdgeDistance - 2.0f) / guard);
        }

        private float crackReveal(float age) {
            float revealBand = 0.065f + (1.0f - slashInfluence) * 0.025f;
            return smooth((stagedCrackCoverage(age) - crackOrder) / revealBand);
        }

        private float breakOpenReveal(float age) {
            return smooth((age - launchTick + 1.0f) / 3.0f);
        }

        private static ProjectedPoint projectPoint(float renderX, float renderY, float renderZ, float baseX, float baseY, Point point, float localZ, float rotationX, float rotationY, float rotationZ, float scale, int width, int height) {
            float px = point.x * width;
            float py = point.y * height;
            float lx = (px - baseX) * scale;
            float ly = (py - baseY) * scale;
            float lz = localZ * scale;

            float cx = Mth.cos(rotationX);
            float sx = Mth.sin(rotationX);
            float y1 = ly * cx - lz * sx;
            float z1 = ly * sx + lz * cx;

            float cy = Mth.cos(rotationY);
            float sy = Mth.sin(rotationY);
            float x2 = lx * cy + z1 * sy;
            float z2 = -lx * sy + z1 * cy;

            float cz = Mth.cos(rotationZ);
            float sz = Mth.sin(rotationZ);
            float x3 = x2 * cz - y1 * sz;
            float y3 = x2 * sz + y1 * cz;
            float z = renderZ + z2;
            float focal = atLeast(SlashCofig.ScreenBreak.SHARD_PERSPECTIVE_FOCAL, 240.0f);
            float perspective = Mth.clamp(focal / Math.max(focal - z, focal * 0.24f), 0.42f, 2.65f);
            float x = renderX + x3 * perspective;
            float y = renderY + y3 * perspective;

            return new ProjectedPoint(x, y, Mth.clamp(z * 0.18f, -280.0f, 280.0f));
        }

        private static void addGlowRibbon(BufferBuilder builder, ProjectedPoint a, ProjectedPoint b, float width, float r, float g, float alpha) {
            float dx = b.x - a.x;
            float dy = b.y - a.y;
            float length = Mth.sqrt(dx * dx + dy * dy);
            if (length <= 0.001f) return;

            float sx = -dy / length * width * 0.5f;
            float sy = dx / length * width * 0.5f;
            float z = (a.z + b.z) * 0.5f;
            float safeAlpha = Mth.clamp(alpha, 0.0f, 1.0f);
            builder.vertex(a.x + sx, a.y + sy, z).color(r, g, 1.0f, safeAlpha).endVertex();
            builder.vertex(a.x - sx, a.y - sy, z).color(r, g, 1.0f, safeAlpha).endVertex();
            builder.vertex(b.x - sx, b.y - sy, z).color(r, g, 1.0f, safeAlpha).endVertex();
            builder.vertex(b.x + sx, b.y + sy, z).color(r, g, 1.0f, safeAlpha).endVertex();
        }

        private static void addGlowQuad(BufferBuilder builder, ProjectedPoint a, ProjectedPoint b, ProjectedPoint c, ProjectedPoint d, float r, float g, float alpha) {
            float safeAlpha = Mth.clamp(alpha, 0.0f, 1.0f);
            float z = (a.z + b.z + c.z + d.z) * 0.25f;
            builder.vertex(a.x, a.y, z).color(r, g, 1.0f, safeAlpha).endVertex();
            builder.vertex(b.x, b.y, z).color(r, g, 1.0f, safeAlpha).endVertex();
            builder.vertex(c.x, c.y, z).color(r, g, 1.0f, safeAlpha).endVertex();
            builder.vertex(d.x, d.y, z).color(r, g, 1.0f, safeAlpha).endVertex();
        }

        private static void addCrackLightBeam(BufferBuilder builder, ProjectedPoint a, ProjectedPoint b, float dirX, float dirY, float length, float endWidth, float r, float g, float alpha) {
            float sx = -dirY * endWidth * 0.5f;
            float sy = dirX * endWidth * 0.5f;
            float z = (a.z + b.z) * 0.5f;
            float safeAlpha = Mth.clamp(alpha, 0.0f, 1.0f);
            float endAX = a.x + dirX * length - sx;
            float endAY = a.y + dirY * length - sy;
            float endBX = b.x + dirX * length + sx;
            float endBY = b.y + dirY * length + sy;

            builder.vertex(a.x, a.y, z).color(r, g, 1.0f, safeAlpha).endVertex();
            builder.vertex(b.x, b.y, z).color(r, g, 1.0f, safeAlpha).endVertex();
            builder.vertex(endBX, endBY, z).color(r, g, 1.0f, 0.0f).endVertex();
            builder.vertex(endAX, endAY, z).color(r, g, 1.0f, 0.0f).endVertex();
        }

        private static float screenDistance(ProjectedPoint a, ProjectedPoint b) {
            float dx = a.x - b.x;
            float dy = a.y - b.y;
            return Mth.sqrt(dx * dx + dy * dy);
        }

        private static boolean isOriginalScreenBorderEdge(Point a, Point b) {
            float margin = 0.004f;
            return (a.x <= margin && b.x <= margin)
                    || (a.x >= 1.0f - margin && b.x >= 1.0f - margin)
                    || (a.y <= margin && b.y <= margin)
                    || (a.y >= 1.0f - margin && b.y >= 1.0f - margin);
        }

        private static boolean outsideSameScreenSide(ProjectedPoint a, ProjectedPoint b, int width, int height) {
            float margin = Math.max(12.0f, Math.min(width, height) * 0.04f);
            return (a.x < -margin && b.x < -margin)
                    || (a.x > width + margin && b.x > width + margin)
                    || (a.y < -margin && b.y < -margin)
                    || (a.y > height + margin && b.y > height + margin);
        }

        private static long crackEdgeKey(ProjectedPoint a, ProjectedPoint b) {
            int first = quantizedPointHash(a);
            int second = quantizedPointHash(b);
            if (Integer.compareUnsigned(first, second) > 0) {
                int swap = first;
                first = second;
                second = swap;
            }
            return ((long) first << 32) ^ (second & 0xFFFFFFFFL);
        }

        private static int quantizedPointHash(ProjectedPoint point) {
            int qx = Math.round(point.x / 4.0f);
            int qy = Math.round(point.y / 4.0f);
            return qx * 73428767 ^ qy * 912931;
        }

        private static float crackEdgeNoise(ProjectedPoint a, ProjectedPoint b) {
            int hash = quantizedPointHash(a) * 31 + quantizedPointHash(b);
            hash ^= hash >>> 16;
            hash *= 0x7FEB352D;
            hash ^= hash >>> 15;
            return (hash & 0x00FFFFFF) / 16777215.0f;
        }

        private static float facing(float rotationX, float rotationY) {
            return Mth.clamp(Math.abs(Mth.cos(rotationX) * Mth.cos(rotationY)), 0.0f, 1.0f);
        }

        private static float smooth(float value) {
            value = Mth.clamp(value, 0.0f, 1.0f);
            return value * value * (3.0f - 2.0f * value);
        }

        private record ProjectedPoint(float x, float y, float z) {}
    }
}
