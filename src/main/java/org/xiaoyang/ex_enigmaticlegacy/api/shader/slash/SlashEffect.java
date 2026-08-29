package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.EXEShaders;

import java.util.*;

public final class SlashEffect {
    private final long seed;
    private final Vec3 center;
    private final float radius;
    private final UUID sourceOwnerId;
    private final boolean screenEffectsEnabled;
    private final boolean spatialAudio;
    private final Random random;
    private final Random domainParticleRandom;
    private final List<SlashLine> lines;
    private final SlashLine finalStrike;
    private final List<WorldShard> worldShards = new ArrayList<>();
    private final int lastRevealTick;
    private final int finalBurstTick;
    private final int fadeStartTick;
    private final int presentationEndTick;
    private final long pressureBedOwner;
    private final CompensationState compensationState = new CompensationState(SlashCofig.Audio.SPACE_FRACTURE_TO_PRESSURE_DELAY_TICKS);
    private int age;
    private boolean pressureBedStarted;
    private boolean breakPrepareSoundPlayed;
    private boolean finalBurstSpawned;
    private boolean finalTrueDemonBurstSpawned;
    private boolean worldShardsCreated;
    private boolean finalGroundImpactTriggered;
    private Vec3 domainCenter;
    private Vec3 finalGroundImpactCenter;
    private int nextDomainGroundSearchTick;
    private int nextFinalGroundImpactSearchTick;
    private int domainEntranceStartTick;
    private float finalGroundImpactAge;
    private float previousRangeVisibility = 1.0f;
    private float currentRangeVisibility = 1.0f;
    private boolean rangeVisibilityInitialized;

    private SlashEffect(Vec3 center, float radius, long seed, List<SlashLine> lines, boolean screenEffectsEnabled, UUID sourceOwnerId) {
        this.seed = seed;
        this.center = center;
        this.radius = Math.max(0.0f, radius);
        this.sourceOwnerId = sourceOwnerId;
        this.screenEffectsEnabled = screenEffectsEnabled;
        this.spatialAudio = AudiencePolicy.usesSpatialAudio(screenEffectsEnabled);
        this.random = new Random(seed ^ 0x8D15EA5EEDL);
        this.domainParticleRandom = new Random(seed ^ 0xD04A1C0FFEE71A5L);
        this.lines = lines;
        this.finalStrike = lines.stream().filter(SlashLine::isFinalStrike).findFirst().orElse(null);
        this.finalGroundImpactAge = finalStrike == null ? Float.POSITIVE_INFINITY : finalStrike.startTick() + SlashCofig.WorldSlash.PRE_CRACK_TICKS + SlashCofig.WorldSlash.SWEEP_TICKS;
        this.lastRevealTick = lines.stream().mapToInt(line -> line.startTick() + line.revealTicks()).max().orElse(0);
        this.finalBurstTick = lastRevealTick + SlashCofig.WorldSlash.FINAL_BURST_DELAY_TICKS;
        this.fadeStartTick = this.finalBurstTick;
        this.presentationEndTick = PressureTimeline.presentationEndTick(lines.size());
        this.pressureBedOwner = Sounds.newPressureBedOwner();
    }

    public static SlashEffect create(Vec3 center, int slashCount, float length, float radius, long seed) {
        return create(center, slashCount, length, radius, seed, new Vec3(0.0, 0.0, 1.0), true, null);
    }

    public static SlashEffect create(Vec3 center, int slashCount, float length, float radius, long seed, boolean screenEffectsEnabled) {
        return create(center, slashCount, length, radius, seed, new Vec3(0.0, 0.0, 1.0), screenEffectsEnabled, null);
    }

    public static SlashEffect create(Vec3 center, int slashCount, float length, float radius, long seed, boolean screenEffectsEnabled, UUID sourceOwnerId) {
        return create(center, slashCount, length, radius, seed, new Vec3(0.0, 0.0, 1.0), screenEffectsEnabled, sourceOwnerId);
    }

    public static SlashEffect create(Vec3 center, int slashCount, float length, float radius, long seed, Vec3 facing, boolean screenEffectsEnabled, UUID sourceOwnerId) {
        int count = Mth.clamp(slashCount, 1, SlashCofig.WorldSlash.MAX_SLASHES);
        float safeLength = Mth.clamp(length, SlashCofig.WorldSlash.MIN_LENGTH, SlashCofig.WorldSlash.MAX_LENGTH);
        float safeRadius = Mth.clamp(radius, SlashCofig.WorldSlash.MIN_RADIUS, SlashCofig.WorldSlash.MAX_RADIUS);
        List<SlashLine> lines = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            boolean finalStrike = Geometry.isFinalStrike(i, count);
            Geometry.Placement placement = Geometry.placement(i, count, safeRadius, facing);
            Vec3 lineCenter = center.add(placement.offset());
            Vec3 direction = placement.direction();
            Vec3 normal = placement.normal();
            float lineLength = safeLength * placement.lengthScale();
            float width = placement.width();
            int outer = fixedOuterColor(i);
            int core = (i % 3 == 0) ? SlashCofig.WorldSlash.CORE_COLOR_ALT : SlashCofig.WorldSlash.CORE_COLOR_PRIMARY;
            boolean sweepReversed = Geometry.sweepReversed(seed, i, count);
            int startTick = Geometry.slashStartTick(i, count);

            lines.add(new SlashLine(lineCenter, direction, normal, lineLength, width, outer, core, startTick, SlashCofig.WorldSlash.LINE_REVEAL_TICKS, sweepReversed, finalStrike));
        }

        return new SlashEffect(center, safeRadius, seed, lines, screenEffectsEnabled, sourceOwnerId);
    }

    public static void renderXMark(PoseStack poseStack, MultiBufferSource buffers, ClientLevel level, Vec3 center, Vec3 camera, float markAge, float length, float width, float alpha, long seed) {
        if (alpha <= 0.001f) return;

        Vec3 normal = camera.subtract(center);
        if (normal.lengthSqr() < 1.0e-8) normal = new Vec3(0.0, 0.0, 1.0);
        normal = normal.normalize();

        Vec3 right = new Vec3(0.0, 1.0, 0.0).cross(normal);
        if (right.lengthSqr() < 1.0e-8) right = new Vec3(1.0, 0.0, 0.0);
        right = right.normalize();

        Vec3 up = normal.cross(right);
        if (up.lengthSqr() < 1.0e-8) up = new Vec3(0.0, 1.0, 0.0);
        up = up.normalize();

        Vec3 rising = right.add(up).normalize();
        Vec3 falling = right.subtract(up).normalize();
        float safeLength = Math.max(0.1f, length);
        float safeWidth = Math.max(0.02f, width);
        int holdTicks = Math.max(1, SlashCofig.WorldSlash.LINE_HOLD_TICKS);
        List<SlashLine> markLines = List.of(new SlashLine(center, rising, normal, safeLength, safeWidth, SlashCofig.WorldSlash.OUTER_COLOR_PRIMARY, SlashCofig.WorldSlash.CORE_COLOR_PRIMARY, 0, 1), new SlashLine(center, falling, normal, safeLength * 0.96f, safeWidth * 0.92f, SlashCofig.WorldSlash.OUTER_COLOR_ALT_A, SlashCofig.WorldSlash.CORE_COLOR_ALT, 0, 1));

        SlashEffect mark = new SlashEffect(center, 1.0f, seed, markLines, false, null);
        float heldSlashAge = SlashCofig.WorldSlash.LINE_REVEAL_TICKS + Math.min(Math.max(0.0f, markAge), holdTicks * 0.45f);
        // The line renderer applies the same flicker/alpha tuning as a world slash.
        float renderAlpha = Mth.clamp(alpha, 0.0f, 1.0f);
        // Compose both cuts as one X. Completing one cut before drawing the other would let
        // the second purple layer wash over the first cut's white edge at the intersection.
        for (XMarkLayer layer : XMarkLayer.values()) {
            for (SlashLine line : markLines) {
                mark.renderXMarkLineLayer(poseStack, buffers, level, camera, line, heldSlashAge, renderAlpha, layer);
            }
        }
    }

    private enum XMarkLayer {
        PURPLE_SILHOUETTE,
        WHITE_EDGE,
        BLACK_CORE
    }

    public boolean tick(ClientLevel level) {
        boolean screenVisible = isPresentationVisible();
        boolean worldVisible = isWorldVisible();
        float soundVolume = spatialAudio ? 1.0f : currentRangeVisibility;

        float effectAge = age + 1.0f;
        emitDomainParticles(level, effectAge, worldVisible);
        for (SlashLine line : lines) {
            if (line.consumeSweepStartSound(effectAge)) {
                if (worldVisible) {
                    if (line.isFinalStrike()) {
                        Sounds.playFinalCut(pressureBedOwner, level, line, soundVolume);
                    } else {
                        Sounds.playCut(pressureBedOwner, level, line, soundVolume);
                    }
                }
                if (screenEffectsEnabled && screenVisible
                        && (!line.isFinalStrike() || !SlashCofig.WorldSlash.FinalStrike.GroundImpact.ENABLED)) {
                    ClientEffects.triggerSlashImpact(this, line, random.nextLong(), level.getGameTime());
                }
            }
            if (line.consumeCompletionEffect(effectAge)) {
                if (worldVisible) ParticleBurst.spawnSlashShatter(level, line, random, seed);
            }
        }
        triggerFinalGroundImpactIfNeeded(effectAge, screenVisible, level.getGameTime());

        if (!pressureBedStarted && compensationState.consumePressureStartTick()) {
            pressureBedStarted = true;
            if (worldVisible) Sounds.startPressureBed(pressureBedOwner, center, spatialAudio, soundVolume);
        }

        if (!finalBurstSpawned && effectAge >= finalBurstTick) {
            finalBurstSpawned = true;
        }
        ensureWorldShards(effectAge, worldVisible);

        if (!breakPrepareSoundPlayed && effectAge >= breakPrepareSoundTick()) {
            breakPrepareSoundPlayed = true;
            if (worldVisible) Sounds.playBreakPrepare(pressureBedOwner, center, spatialAudio, soundVolume);
        }

        if (!finalTrueDemonBurstSpawned && effectAge >= finalTrueDemonBurstTick()) {
            finalTrueDemonBurstSpawned = true;
            Sounds.releasePressureBed(pressureBedOwner);
            if (worldVisible) {
                ParticleBurst.spawnFinalBurst(level, center, radius + 3.0f, random, seed);
                Sounds.playGlassBreak(pressureBedOwner, center, spatialAudio, soundVolume);
            }
        }

        if (spatialAudio && effectAge >= lastRevealTick && compensationState.consumeDesaturationFrame()) {
            Sounds.playFinalGlassTail(pressureBedOwner, center, true, 1.0f);
        } else if (!screenVisible && effectAge >= lastRevealTick) {
            compensationState.consumeDesaturationFrame();
        }

        Iterator<WorldShard> shardIterator = worldShards.iterator();
        while (shardIterator.hasNext()) {
            WorldShard shard = shardIterator.next();
            shard.tick();
            if (!shard.isAlive()) shardIterator.remove();
        }

        age++;
        return age <= presentationEndTick || !worldShards.isEmpty();
    }

    private void triggerFinalGroundImpactIfNeeded(float effectAge, boolean screenVisible, long gameTick) {
        if (!SlashCofig.WorldSlash.FinalStrike.GroundImpact.ENABLED
                || finalGroundImpactTriggered
                || finalStrike == null
                || effectAge < finalGroundImpactAge) {
            return;
        }

        finalGroundImpactTriggered = true;
        if (screenEffectsEnabled && screenVisible) {
            ClientEffects.triggerSlashImpact(this, finalStrike, random.nextLong(), gameTick);
        }
    }

    boolean isAwaitingDesaturationAudio(float partialTick) {
        if (!screenEffectsEnabled) return false;
        float renderAge = renderAge(partialTick);
        return compensationState.isAwaitingDesaturationFrame() && renderAge >= lastRevealTick && renderAge < finalTrueDemonBurstTick();
    }

    void onDesaturationShaderRendered() {
        if (!screenEffectsEnabled) return;
        if (!compensationState.consumeDesaturationFrame()) return;
        if (isPresentationVisible()) {
            Sounds.playFinalGlassTail(pressureBedOwner, center, false, currentRangeVisibility);
        }
    }

    void stopSounds() {
        Sounds.stopOwner(pressureBedOwner, SlashCofig.Audio.RANGE_EXIT_FADE_TICKS);
    }

    void updateViewerPosition(ClientLevel level, Vec3 viewerPosition) {
        resolveDomainCenterIfNeeded(level);
        resolveFinalGroundImpactIfNeeded(level);
        float visibleRadius = radius;
        if (SlashCofig.WorldSlash.Domain.ENABLED) {
            visibleRadius = domainCenter == null
                    ? 0.0f
                    : DomainTimeline.radius(radius, domainEntrance(age + 1.0f));
        }
        float nextVisibility = RangeVisibility.at(
                viewerPosition,
                center,
                visibleRadius,
                SlashCofig.WorldSlash.Domain.VISIBILITY_EDGE_FADE_FRACTION
        );
        if (!rangeVisibilityInitialized) {
            previousRangeVisibility = nextVisibility;
            currentRangeVisibility = nextVisibility;
            rangeVisibilityInitialized = true;
            if (!spatialAudio) Sounds.updateOwnerVolume(pressureBedOwner, nextVisibility);
            if (screenEffectsEnabled) EarthquakeVisualsManager.updateDimensionalSlashOwnerVisibility(pressureBedOwner, nextVisibility);
            return;
        }

        boolean wasVisible = isPresentationVisible();
        previousRangeVisibility = currentRangeVisibility;
        currentRangeVisibility = nextVisibility;
        if (screenEffectsEnabled) EarthquakeVisualsManager.updateDimensionalSlashOwnerVisibility(pressureBedOwner, nextVisibility);

        boolean nowVisible = isPresentationVisible();
        if (!spatialAudio) {
            if (wasVisible && !nowVisible) {
                Sounds.stopOwner(pressureBedOwner, SlashCofig.Audio.RANGE_EXIT_FADE_TICKS);
            } else if (nowVisible) {
                Sounds.updateOwnerVolume(pressureBedOwner, nextVisibility);
            }
        }
    }

    boolean isPresentationVisible() {
        return currentRangeVisibility > 0.001f;
    }

    boolean hasPresentationForFrame() {
        return screenEffectsEnabled && (previousRangeVisibility > 0.001f || currentRangeVisibility > 0.001f);
    }

    float rangeVisibility(float partialTick) {
        return Mth.lerp(Mth.clamp(partialTick, 0.0f, 1.0f), previousRangeVisibility, currentRangeVisibility);
    }

    float worldVisibility(float partialTick) {
        return AudiencePolicy.worldVisibility(screenEffectsEnabled, rangeVisibility(partialTick));
    }

    boolean suppressesScreenEffects() {
        return !screenEffectsEnabled;
    }

    UUID sourceOwnerId() {
        return sourceOwnerId;
    }

    private boolean isWorldVisible() {
        return AudiencePolicy.worldVisibility(screenEffectsEnabled, currentRangeVisibility) > 0.001f;
    }

    long presentationOwnerId() {
        return pressureBedOwner;
    }

    long roundId() {
        return seed;
    }

    long screenBreakSeed() {
        return seed ^ 0x51A5B8E4C3D2796FL;
    }

    boolean isFinalScreenTimelineActive() {
        if (!screenEffectsEnabled) return false;
        float screenAge = screenBreakAge();
        return PresentationPolicy.isScreenCandidate(
                hasPresentationForFrame(),
                finalBurstSpawned,
                screenAge
        );
    }

    float screenBreakAge() {
        return age - finalBurstTick;
    }

    boolean shouldHoldScreenFreeze(float partialTick) {
        if (!screenEffectsEnabled) return false;
        return PresentationPolicy.holdsScreenFreeze(
                renderAge(partialTick),
                lastRevealTick,
                finalBurstTick
        );
    }

    int presentationEndTick() {
        return presentationEndTick;
    }

    int breakPrepareSoundTick() {
        int peakAlignedStartTick = Math.round(finalTrueDemonBurstTick() - SlashCofig.Audio.BREAK_PREPARE_PEAK_OFFSET_TICKS);
        return Math.max(lastRevealTick, peakAlignedStartTick);
    }

    int finalTrueDemonBurstTick() {
        return finalBurstTick + Math.max(0, Math.round(SlashCofig.ScreenBreak.SHARD_LAUNCH_START_TICKS));
    }

    private int domainEndTick() {
        return presentationEndTick;
    }

    public void render(PoseStack poseStack, MultiBufferSource buffers, Vec3 camera, float partialTick) {
        renderBody(poseStack, buffers, camera, partialTick);
        renderWhiteCores(poseStack, buffers, camera, partialTick);
    }

    public void renderBody(PoseStack poseStack, MultiBufferSource buffers, Vec3 camera, float partialTick) {
        renderBody(poseStack, buffers, camera, partialTick, 1.0f);
    }

    void renderBody(PoseStack poseStack, MultiBufferSource buffers, Vec3 camera, float partialTick, float alphaScale) {
        float renderAge = age + partialTick;
        float safeAlphaScale = Mth.clamp(alphaScale, 0.0f, 1.0f);
        float alpha = globalAlpha(renderAge) * safeAlphaScale;
        float domainAlpha = domainTimelineAlpha(renderAge) * safeAlphaScale;
        if (domainAlpha > 0.001f) {
            renderDomain(poseStack, buffers, camera, renderAge, domainAlpha);
        }
        renderFinalGroundImpactWave(poseStack, buffers, camera, renderAge, alpha);
        if (alpha > 0.001f) {
            for (SlashLine line : lines) {
                renderLineBody(poseStack, buffers, camera, line, renderAge, alpha);
            }
        }
    }

    public void renderWhiteCores(PoseStack poseStack, MultiBufferSource buffers, Vec3 camera, float partialTick) {
        renderWhiteCores(poseStack, buffers, camera, partialTick, 1.0f);
    }

    void renderWhiteCores(PoseStack poseStack, MultiBufferSource buffers, Vec3 camera, float partialTick, float alphaScale) {
        float renderAge = age + partialTick;
        float alpha = globalAlpha(renderAge) * Mth.clamp(alphaScale, 0.0f, 1.0f);
        if (alpha <= 0.001f) return;

        for (SlashLine line : lines) {
            renderLineWhiteCore(poseStack, buffers, camera, line, renderAge, alpha);
        }
    }

    public boolean hasWorldShards() {
        return !worldShards.isEmpty();
    }

    List<SlashLine> screenBreakLines() {
        return List.copyOf(lines);
    }

    int finalBurstTick() {
        return finalBurstTick;
    }

    float renderAge(float partialTick) {
        return age + partialTick;
    }

    float screenDistortionAlpha(float partialTick) {
        if (!screenEffectsEnabled) return 0.0f;
        float renderAge = renderAge(partialTick);
        if (renderAge < lastRevealTick) {
            return 0.0f;
        }
        return globalAlpha(renderAge);
    }

    float topChromaAlpha(float partialTick) {
        if (!screenEffectsEnabled || !SlashCofig.ScreenBreak.TopChroma.ENABLED) return 0.0f;

        float renderAge = renderAge(partialTick);
        float firstSlashStartTick = lines.stream().mapToInt(SlashLine::startTick).min().orElse(0);
        float finalScreenBreakEndTick = finalBurstTick + PresentationPolicy.screenVisualDuration();
        float timelineAlpha = ChromaTimeline.envelope(
                renderAge,
                firstSlashStartTick,
                finalScreenBreakEndTick
        );
        return Mth.clamp(timelineAlpha * rangeVisibility(partialTick), 0.0f, 1.0f);
    }

    float screenVoronoiStrength(float partialTick) {
        float distortionAlpha = screenDistortionAlpha(partialTick);
        if (distortionAlpha <= 0.001f) {
            return 0.0f;
        }

        float waterWarpDriver = screenWaterWarpDriver(partialTick);
        if (waterWarpDriver <= 0.001f) {
            return 0.0f;
        }

        return Mth.clamp(distortionAlpha * waterWarpDriver, 0.0f, 1.0f);
    }

    float screenVoronoiAge(float partialTick) {
        return Math.max(0.0f, screenMotionBlurTimelineAge(partialTick));
    }

    boolean shouldRenderPreBreakMotionBlur(float partialTick) {
        if (!screenEffectsEnabled) return false;
        float renderAge = renderAge(partialTick);
        return renderAge >= lastRevealTick && renderAge < finalBurstTick;
    }

    float screenMotionBlurTimelineAge(float partialTick) {
        return renderAge(partialTick) - lastRevealTick;
    }

    float screenMotionBlurTimelineDuration() {
        return screenMotionBlurLeadTicks() + SlashCofig.ScreenBreak.DURATION_TICKS;
    }

    private float screenWaterWarpDriver(float partialTick) {
        return ScreenEffects.waterWarpVisibility(screenMotionBlurTimelineAge(partialTick), screenMotionBlurTimelineDuration());
    }

    float screenMotionBlurLeadTicks() {
        return Math.max(0.0f, finalBurstTick - lastRevealTick);
    }

    long screenCrackSeed() {
        return seed;
    }

    public void renderWorldShards(PoseStack poseStack, com.mojang.blaze3d.vertex.BufferBuilder builder, Vec3 camera, float partialTick, float alphaScale) {
        for (WorldShard shard : worldShards) {
            shard.render(poseStack, builder, camera, partialTick, alphaScale);
        }
    }

    void renderBloomMask(PoseStack poseStack, MultiBufferSource buffers, Vec3 camera, float partialTick, float alphaScale) {
        if (!BloomRenderer.canRenderBloomMask()) return;

        float renderAge = age + partialTick;
        float safeAlphaScale = Mth.clamp(alphaScale, 0.0f, 1.0f);
        float alpha = globalBloomAlpha(renderAge) * safeAlphaScale * safeAlphaScale;
        if (alpha <= 0.001f) return;

        renderFinalGroundImpactBloomMask(poseStack, buffers, camera, renderAge, alpha);
        for (SlashLine line : lines) {
            renderLineBloomMask(poseStack, buffers, camera, line, renderAge, alpha);
        }
    }

    private void renderDomain(PoseStack poseStack, MultiBufferSource buffers, Vec3 camera, float renderAge, float globalAlpha) {
        if (!SlashCofig.WorldSlash.Domain.ENABLED || domainCenter == null) return;

        float entrance = domainEntrance(renderAge);
        float visibility = entrance * globalAlpha;
        if (visibility <= 0.001f) return;

        float domainRadius = DomainTimeline.radius(radius, entrance);
        float ringBaseWidth = SlashCofig.WorldSlash.Domain.RING_BASE_WIDTH;
        float outerWidth = ringBaseWidth * SlashCofig.WorldSlash.MAIN_OUTER_WIDTH;
        float coreWidth = ringBaseWidth * SlashCofig.WorldSlash.MAIN_CORE_WIDTH;
        float blackCoreWidth = coreWidth * SlashCofig.WorldSlash.MAIN_BLACK_CORE_WIDTH;
        VertexConsumer core = buffers.getBuffer(EXEShaders.DIMENSIONAL_SLASH_CORE);
        VertexConsumer ink = buffers.getBuffer(EXEShaders.DIMENSIONAL_SLASH_INK_CORE);

        drawRadialDisc(poseStack, core, camera, domainCenter, domainRadius,
                SlashCofig.WorldSlash.Domain.FILL_CENTER_ALPHA * visibility);
        drawFeatheredGroundRing(poseStack, core, camera, domainCenter, domainRadius,
                outerWidth,
                SlashCofig.WorldSlash.OUTER_COLOR_PRIMARY,
                SlashCofig.WorldSlash.MAIN_OUTER_ALPHA * visibility,
                SlashCofig.WorldSlash.MAIN_OUTER_SOFT_EDGE);
        drawFeatheredGroundRingBand(poseStack, core, camera, domainCenter, domainRadius,
                coreWidth,
                whiteCoreInnerGapWidth(blackCoreWidth),
                SlashCofig.WorldSlash.MAIN_CORE_ALPHA * visibility,
                SlashCofig.WorldSlash.MAIN_CORE_SOFT_EDGE);
        drawFeatheredGroundRing(poseStack, ink, camera, domainCenter, domainRadius,
                blackCoreWidth,
                SlashCofig.WorldSlash.MAIN_BLACK_CORE_COLOR,
                SlashCofig.WorldSlash.MAIN_BLACK_CORE_ALPHA * visibility,
                SlashCofig.WorldSlash.MAIN_BLACK_CORE_SOFT_EDGE);
    }

    private void renderFinalGroundImpactWave(PoseStack poseStack, MultiBufferSource buffers, Vec3 camera, float renderAge, float globalAlpha) {
        if (!SlashCofig.WorldSlash.FinalStrike.GroundImpact.ENABLED || finalGroundImpactCenter == null || globalAlpha <= 0.001f || !GroundImpactTimeline.isActive(renderAge, finalGroundImpactAge, SlashCofig.WorldSlash.FinalStrike.GroundImpact.DURATION_TICKS)) {
            return;
        }

        float progress = GroundImpactTimeline.progress(
                renderAge,
                finalGroundImpactAge,
                SlashCofig.WorldSlash.FinalStrike.GroundImpact.DURATION_TICKS
        );
        float timelineAlpha = GroundImpactTimeline.alpha(progress) * globalAlpha;
        if (timelineAlpha <= 0.001f) return;

        float maximumRadius = GroundImpactShockwave.maximumRadius(radius);
        float waveRadius = GroundImpactTimeline.radius(
                SlashCofig.WorldSlash.FinalStrike.GroundImpact.START_RADIUS,
                maximumRadius,
                progress
        );
        float waveWidth = GroundImpactShockwave.waveWidth(progress);
        float outerWidth = waveWidth * SlashCofig.WorldSlash.FinalStrike.GroundImpact.OUTER_WIDTH_SCALE;
        float coreWidth = waveWidth * SlashCofig.WorldSlash.MAIN_CORE_WIDTH;
        float blackCoreWidth = coreWidth * SlashCofig.WorldSlash.MAIN_BLACK_CORE_WIDTH;
        VertexConsumer core = buffers.getBuffer(EXEShaders.DIMENSIONAL_SLASH_CORE);
        VertexConsumer ink = buffers.getBuffer(EXEShaders.DIMENSIONAL_SLASH_INK_CORE);

        drawFeatheredGroundRing(poseStack, core, camera, finalGroundImpactCenter, waveRadius,
                outerWidth,
                SlashCofig.WorldSlash.OUTER_COLOR_PRIMARY,
                SlashCofig.WorldSlash.FinalStrike.GroundImpact.OUTER_ALPHA * timelineAlpha,
                SlashCofig.WorldSlash.FinalStrike.GroundImpact.SOFT_EDGE);
        drawFeatheredGroundRingBand(poseStack, core, camera, finalGroundImpactCenter, waveRadius,
                coreWidth,
                whiteCoreInnerGapWidth(blackCoreWidth),
                SlashCofig.WorldSlash.FinalStrike.GroundImpact.CORE_ALPHA * timelineAlpha,
                SlashCofig.WorldSlash.FinalStrike.GroundImpact.CORE_SOFT_EDGE);
        drawFeatheredGroundRing(poseStack, ink, camera, finalGroundImpactCenter, waveRadius,
                blackCoreWidth,
                SlashCofig.WorldSlash.MAIN_BLACK_CORE_COLOR,
                SlashCofig.WorldSlash.FinalStrike.GroundImpact.BLACK_ALPHA * timelineAlpha,
                SlashCofig.WorldSlash.FinalStrike.GroundImpact.BLACK_SOFT_EDGE);
    }

    private void renderFinalGroundImpactBloomMask(PoseStack poseStack, MultiBufferSource buffers, Vec3 camera, float renderAge, float globalAlpha) {
        if (!SlashCofig.WorldSlash.FinalStrike.GroundImpact.ENABLED || finalGroundImpactCenter == null || globalAlpha <= 0.001f || !GroundImpactTimeline.isActive(renderAge, finalGroundImpactAge, SlashCofig.WorldSlash.FinalStrike.GroundImpact.DURATION_TICKS)) {
            return;
        }

        float progress = GroundImpactTimeline.progress(renderAge, finalGroundImpactAge, SlashCofig.WorldSlash.FinalStrike.GroundImpact.DURATION_TICKS);
        float timelineAlpha = GroundImpactTimeline.alpha(progress) * globalAlpha * SlashCofig.WorldSlash.FinalStrike.GroundImpact.BLOOM_ALPHA;
        if (timelineAlpha <= 0.001f) return;

        float maximumRadius = GroundImpactShockwave.maximumRadius(radius);
        float waveRadius = GroundImpactTimeline.radius(SlashCofig.WorldSlash.FinalStrike.GroundImpact.START_RADIUS, maximumRadius, progress);
        float waveWidth = GroundImpactShockwave.waveWidth(progress) * SlashCofig.WorldSlash.FinalStrike.GroundImpact.BLOOM_WIDTH_SCALE;
        VertexConsumer mask = buffers.getBuffer(BloomRenderer.maskRenderType());

        drawFeatheredGroundRing(poseStack, mask, camera, finalGroundImpactCenter, waveRadius, waveWidth, SlashCofig.WorldSlash.OUTER_COLOR_PRIMARY, timelineAlpha, SlashCofig.WorldSlash.FinalStrike.GroundImpact.SOFT_EDGE);
    }

    private void renderLineBody(PoseStack poseStack, MultiBufferSource buffers, Vec3 camera, SlashLine line, float renderAge, float globalAlpha) {
        if (renderAge < line.startTick()) return;

        float preCrack = line.preCrackProgress(renderAge);
        float sweep = line.sweepProgress(renderAge);
        if (preCrack <= 0.001f && sweep <= 0.001f) return;

        Vec3 fullStart = line.start();
        Vec3 fullEnd = line.end();
        Vec3 side = cameraFacingSide(fullStart, fullEnd, camera, line.normal());
        float flicker = SlashCofig.WorldSlash.FLICKER_BASE + SlashCofig.WorldSlash.FLICKER_RANDOM * Mth.sin((renderAge + line.startTick()) * SlashCofig.WorldSlash.FLICKER_SPEED);
        float lineAlpha = globalAlpha * flicker * SlashCofig.Quick.WORLD_SLASH_ALPHA_SCALE;

        float preCrackAlpha = preCrack * (1.0f - sweep * 0.65f);
        if (preCrackAlpha > 0.001f) {
            renderPreCrack(poseStack, buffers, camera, line, fullStart, fullEnd, side, lineAlpha * preCrackAlpha);
        }

        if (sweep > 0.001f) {
            Vec3 start = line.sweptStart(renderAge);
            Vec3 currentEnd = line.sweptEnd(renderAge);
            float residualAlpha = lineAlpha * SlashCofig.WorldSlash.SWEEP_RESIDUAL_ALPHA;
            renderMainBlade(poseStack, buffers, camera, line, start, currentEnd, side, residualAlpha);
            if (sweep < 0.999f) {
                renderSweepHead(poseStack, buffers, camera, line, renderAge, side, lineAlpha);
            }
        }

    }

    private void renderLineWhiteCore(PoseStack poseStack, MultiBufferSource buffers, Vec3 camera, SlashLine line, float renderAge, float globalAlpha) {
        if (renderAge < line.startTick()) return;

        float sweep = line.sweepProgress(renderAge);
        if (sweep <= 0.001f) return;

        Vec3 start = line.sweptStart(renderAge);
        Vec3 currentEnd = line.sweptEnd(renderAge);
        Vec3 side = cameraFacingSide(line.start(), line.end(), camera, line.normal());
        float flicker = SlashCofig.WorldSlash.FLICKER_BASE + SlashCofig.WorldSlash.FLICKER_RANDOM * Mth.sin((renderAge + line.startTick()) * SlashCofig.WorldSlash.FLICKER_SPEED);
        float lineAlpha = globalAlpha * flicker * SlashCofig.Quick.WORLD_SLASH_ALPHA_SCALE;
        renderWhiteCore(poseStack, buffers, camera, line, start, currentEnd, side, lineAlpha * SlashCofig.WorldSlash.SWEEP_RESIDUAL_ALPHA);
    }

    private void renderXMarkLineLayer(PoseStack poseStack, MultiBufferSource buffers, ClientLevel level, Vec3 camera, SlashLine line, float renderAge, float globalAlpha, XMarkLayer layer) {
        if (renderAge < line.startTick()) return;

        float sweep = line.sweepProgress(renderAge);
        if (sweep <= 0.001f) return;

        Vec3 start = line.sweptStart(renderAge);
        Vec3 currentEnd = line.sweptEnd(renderAge);
        Vec3 side = cameraFacingSide(line.start(), line.end(), camera, line.normal());
        float flicker = SlashCofig.WorldSlash.FLICKER_BASE + SlashCofig.WorldSlash.FLICKER_RANDOM * Mth.sin((renderAge + line.startTick()) * SlashCofig.WorldSlash.FLICKER_SPEED);
        float lineAlpha = globalAlpha * flicker * SlashCofig.Quick.WORLD_SLASH_ALPHA_SCALE;
        float width = worldLineWidth(line);
        float coreWidth = width * SlashCofig.WorldSlash.MAIN_CORE_WIDTH;
        float blackCoreWidth = coreWidth * SlashCofig.WorldSlash.MAIN_BLACK_CORE_WIDTH;

        switch (layer) {
            case PURPLE_SILHOUETTE -> {
                VertexConsumer energy = buffers.getBuffer(EXEShaders.DIMENSIONAL_SLASH_ENTITY_PIERCE);
                drawFeatheredCutTaperedRibbonAgainstBlocks(poseStack, energy, level, camera, start, currentEnd, side, width * SlashCofig.WorldSlash.MAIN_OUTER_WIDTH, line.outerColor(), SlashCofig.WorldSlash.MAIN_OUTER_ALPHA * lineAlpha, SlashCofig.WorldSlash.MAIN_OUTER_ALPHA * lineAlpha, SlashCofig.WorldSlash.MAIN_OUTER_SOFT_EDGE, true);
            }
            case WHITE_EDGE -> {
                VertexConsumer energy = buffers.getBuffer(EXEShaders.DIMENSIONAL_SLASH_ENTITY_PIERCE);
                drawFeatheredTaperedRibbonBandAgainstBlocks(poseStack, energy, level, camera, start, currentEnd, side, coreWidth, whiteCoreInnerGapWidth(blackCoreWidth), line.coreColor(), SlashCofig.WorldSlash.MAIN_CORE_ALPHA * lineAlpha, SlashCofig.WorldSlash.MAIN_CORE_ALPHA * lineAlpha);
            }
            case BLACK_CORE -> {
                VertexConsumer ink = buffers.getBuffer(EXEShaders.DIMENSIONAL_SLASH_ENTITY_PIERCE_INK);
                drawFeatheredCutTaperedRibbonAgainstBlocks(poseStack, ink, level, camera, start, currentEnd, side, blackCoreWidth, SlashCofig.WorldSlash.MAIN_BLACK_CORE_COLOR, SlashCofig.WorldSlash.MAIN_BLACK_CORE_ALPHA * lineAlpha, SlashCofig.WorldSlash.MAIN_BLACK_CORE_ALPHA * lineAlpha, SlashCofig.WorldSlash.MAIN_BLACK_CORE_SOFT_EDGE, false);
            }
        }
    }

    private void renderPreCrack(PoseStack poseStack, MultiBufferSource buffers, Vec3 camera, SlashLine line, Vec3 start, Vec3 end, Vec3 side, float alpha) {
        VertexConsumer glow = buffers.getBuffer(EXEShaders.DIMENSIONAL_SLASH_CORE);
        VertexConsumer ink = buffers.getBuffer(EXEShaders.DIMENSIONAL_SLASH_INK_CORE);
        float width = worldLineWidth(line);
        drawFeatheredTaperedRibbon(poseStack, glow, camera, start, end, side, width * SlashCofig.WorldSlash.PRE_CRACK_GLOW_WIDTH, SlashCofig.WorldSlash.PRE_CRACK_GLOW_COLOR, SlashCofig.WorldSlash.PRE_CRACK_GLOW_ALPHA * alpha, SlashCofig.WorldSlash.PRE_CRACK_GLOW_ALPHA * alpha, 0.86f);
        drawFeatheredTaperedRibbon(poseStack, ink, camera, start, end, side, width * SlashCofig.WorldSlash.PRE_CRACK_WIDTH, SlashCofig.WorldSlash.PRE_CRACK_COLOR, SlashCofig.WorldSlash.PRE_CRACK_ALPHA * alpha, SlashCofig.WorldSlash.PRE_CRACK_ALPHA * alpha, 0.28f);
    }

    private void renderSweepHead(PoseStack poseStack, MultiBufferSource buffers, Vec3 camera, SlashLine line, float renderAge, Vec3 side, float alpha) {
        VertexConsumer core = buffers.getBuffer(EXEShaders.DIMENSIONAL_SLASH_CORE);
        Vec3 head = line.sweepHead(renderAge);
        Vec3 trail = line.sweepHeadTrail(renderAge, SlashCofig.WorldSlash.SWEEP_HEAD_LENGTH);
        Vec3 afterimageTrail = line.sweepHeadTrail(renderAge, SlashCofig.WorldSlash.SWEEP_AFTERIMAGE_LENGTH);
        float width = worldLineWidth(line);
        drawFeatheredTaperedRibbon(poseStack, core, camera, afterimageTrail, head, side, width * SlashCofig.WorldSlash.SWEEP_AFTERIMAGE_WIDTH, line.outerColor(), SlashCofig.WorldSlash.SWEEP_AFTERIMAGE_ALPHA * alpha, SlashCofig.WorldSlash.SWEEP_AFTERIMAGE_ALPHA * alpha, 0.92f);
        drawFeatheredTaperedRibbon(poseStack, core, camera, trail, head, side, width * SlashCofig.WorldSlash.SWEEP_HEAD_OUTER_WIDTH, line.outerColor(), SlashCofig.WorldSlash.SWEEP_HEAD_OUTER_ALPHA * alpha, SlashCofig.WorldSlash.SWEEP_HEAD_OUTER_ALPHA * alpha, 0.76f);
        drawFeatheredTaperedRibbon(poseStack, core, camera, trail, head, side, width * SlashCofig.WorldSlash.SWEEP_HEAD_CORE_WIDTH, line.coreColor(), SlashCofig.WorldSlash.SWEEP_HEAD_CORE_ALPHA * alpha, SlashCofig.WorldSlash.SWEEP_HEAD_CORE_ALPHA * alpha, 0.48f);
    }

    private void renderMainBlade(PoseStack poseStack, MultiBufferSource buffers, Vec3 camera, SlashLine line, Vec3 start, Vec3 end, Vec3 side, float alpha) {
        VertexConsumer core = buffers.getBuffer(EXEShaders.DIMENSIONAL_SLASH_CORE);
        float width = worldLineWidth(line);
        drawFeatheredTaperedRibbon(poseStack, core, camera, start, end, side, width * SlashCofig.WorldSlash.MAIN_OUTER_WIDTH, line.outerColor(), SlashCofig.WorldSlash.MAIN_OUTER_ALPHA * alpha, SlashCofig.WorldSlash.MAIN_OUTER_ALPHA * alpha, SlashCofig.WorldSlash.MAIN_OUTER_SOFT_EDGE);
    }

    private void renderLineBloomMask(PoseStack poseStack, MultiBufferSource buffers, Vec3 camera, SlashLine line, float renderAge, float globalAlpha) {
        if (renderAge < line.startTick()) return;

        float sweep = line.sweepProgress(renderAge);
        if (sweep <= 0.001f) return;

        Vec3 start = line.sweptStart(renderAge);
        Vec3 currentEnd = line.sweptEnd(renderAge);
        Vec3 side = cameraFacingSide(line.start(), line.end(), camera, line.normal());
        float flicker = SlashCofig.WorldSlash.FLICKER_BASE + SlashCofig.WorldSlash.FLICKER_RANDOM * Mth.sin((renderAge + line.startTick()) * SlashCofig.WorldSlash.FLICKER_SPEED);
        float settle = line.settleProgress(renderAge);
        float pulse = 1.0f + Mth.sin(Mth.PI * settle) * SlashCofig.WorldSlash.SETTLE_BLOOM_PULSE;
        float alpha = globalAlpha * flicker * pulse * SlashCofig.WorldSlash.BLOOM_MASK_ALPHA * SlashCofig.Quick.WORLD_BLOOM_INTENSITY_SCALE;
        float width = worldLineWidth(line);
        VertexConsumer mask = buffers.getBuffer(BloomRenderer.maskRenderType());

        if (sweep < 0.999f) {
            Vec3 head = line.sweepHead(renderAge);
            Vec3 trail = line.sweepHeadTrail(renderAge, SlashCofig.WorldSlash.SWEEP_HEAD_LENGTH);
            drawTaperedRibbon(poseStack, mask, camera, trail, head, side, width * SlashCofig.WorldSlash.SWEEP_HEAD_OUTER_WIDTH, line.outerColor(), alpha * SlashCofig.WorldSlash.SWEEP_HEAD_OUTER_ALPHA * 1.35f, alpha * SlashCofig.WorldSlash.SWEEP_HEAD_OUTER_ALPHA * 1.35f);
            drawTaperedRibbon(poseStack, mask, camera, trail, head, side, width * SlashCofig.WorldSlash.SWEEP_HEAD_CORE_WIDTH, line.coreColor(), alpha * SlashCofig.WorldSlash.SWEEP_HEAD_CORE_ALPHA * 1.55f, alpha * SlashCofig.WorldSlash.SWEEP_HEAD_CORE_ALPHA * 1.55f);
        }

        drawTaperedRibbon(poseStack, mask, camera, start, currentEnd, side, width * SlashCofig.WorldSlash.BLOOM_MASK_WIDTH, line.outerColor(), alpha, alpha);
        float coreWidth = width * SlashCofig.WorldSlash.MAIN_CORE_WIDTH;
        float blackCoreWidth = coreWidth * SlashCofig.WorldSlash.MAIN_BLACK_CORE_WIDTH;
        drawTaperedRibbonBand(poseStack, mask, camera, start, currentEnd, side, coreWidth, whiteCoreInnerGapWidth(blackCoreWidth), line.coreColor(), alpha, alpha);

    }

    private void renderWhiteCore(PoseStack poseStack, MultiBufferSource buffers, Vec3 camera, SlashLine line, Vec3 start, Vec3 end, Vec3 side, float alpha) {
        VertexConsumer consumer = buffers.getBuffer(EXEShaders.DIMENSIONAL_SLASH_CORE);
        VertexConsumer ink = buffers.getBuffer(EXEShaders.DIMENSIONAL_SLASH_INK_CORE);
        float width = worldLineWidth(line);
        float coreWidth = width * SlashCofig.WorldSlash.MAIN_CORE_WIDTH;
        float blackCoreWidth = coreWidth * SlashCofig.WorldSlash.MAIN_BLACK_CORE_WIDTH;
        drawFeatheredTaperedRibbonBand(poseStack, consumer, camera, start, end, side, coreWidth, whiteCoreInnerGapWidth(blackCoreWidth), line.coreColor(), SlashCofig.WorldSlash.MAIN_CORE_ALPHA * alpha, SlashCofig.WorldSlash.MAIN_CORE_ALPHA * alpha);
        Vec3 inkOffset = blackCoreDepthOffset(camera, start, end);
        drawFeatheredTaperedRibbon(poseStack, ink, camera, start.add(inkOffset), end.add(inkOffset), side, blackCoreWidth, SlashCofig.WorldSlash.MAIN_BLACK_CORE_COLOR, SlashCofig.WorldSlash.MAIN_BLACK_CORE_ALPHA * alpha, SlashCofig.WorldSlash.MAIN_BLACK_CORE_ALPHA * alpha, SlashCofig.WorldSlash.MAIN_BLACK_CORE_SOFT_EDGE);
    }

    private Vec3 blackCoreDepthOffset(Vec3 camera, Vec3 start, Vec3 end) {
        Vec3 midpoint = start.add(end).scale(0.5);
        Vec3 toCamera = camera.subtract(midpoint);
        if (toCamera.lengthSqr() < 1.0e-8) {
            return new Vec3(0.0, 0.0, 0.0);
        }
        return toCamera.normalize().scale(SlashCofig.WorldSlash.MAIN_BLACK_CORE_DEPTH_BIAS);
    }

    private float globalAlpha(float renderAge) {
        if (renderAge <= fadeStartTick) return 1.0f;
        float t = (renderAge - fadeStartTick) / SlashCofig.WorldSlash.FINAL_FADE_TICKS;
        return smooth(1.0f - Mth.clamp(t, 0.0f, 1.0f));
    }

    private float globalBloomAlpha(float renderAge) {
        float alpha = globalAlpha(renderAge);
        return alpha * alpha;
    }

    private float domainTimelineAlpha(float renderAge) {
        return DomainTimeline.fadeAlpha(renderAge, domainEndTick(), SlashCofig.WorldSlash.Domain.FADE_TICKS);
    }

    private Vec3 resolveGroundSurface(ClientLevel level, double x, double z, double searchStartY) {
        if (level == null || !level.hasChunkAt(BlockPos.containing(x, center.y, z))) return null;

        double rayStartY = Math.min(level.getMaxBuildHeight() - 1.0e-3, searchStartY);
        double rayEndY = level.getMinBuildHeight() - 1.0;
        if (rayStartY <= rayEndY) return null;

        Vec3 rayStart = new Vec3(x, rayStartY, z);
        Vec3 rayEnd = new Vec3(x, rayEndY, z);
        HitResult hit = level.clip(new ClipContext(rayStart, rayEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
        if (hit.getType() != HitResult.Type.BLOCK) return null;

        return hit.getLocation();
    }

    private Vec3 resolveDomainCenter(ClientLevel level) {
        Vec3 location = resolveGroundSurface(level, center.x, center.z, center.y + Math.max(0.0f, SlashCofig.WorldSlash.Domain.GROUND_SEARCH_ABOVE));
        if (location == null) return null;

        return new Vec3(center.x, location.y + SlashCofig.WorldSlash.Domain.GROUND_OFFSET, center.z);
    }

    private void resolveDomainCenterIfNeeded(ClientLevel level) {
        if (!SlashCofig.WorldSlash.Domain.ENABLED || domainCenter != null || age < nextDomainGroundSearchTick) return;

        Vec3 resolvedCenter = resolveDomainCenter(level);
        if (resolvedCenter != null) {
            domainCenter = resolvedCenter;
            domainEntranceStartTick = age + 1;
        }
        nextDomainGroundSearchTick = age + Math.max(1, SlashCofig.WorldSlash.Domain.GROUND_RETRY_TICKS);
    }

    private void resolveFinalGroundImpactIfNeeded(ClientLevel level) {
        if (!SlashCofig.WorldSlash.FinalStrike.GroundImpact.ENABLED || finalGroundImpactCenter != null || finalStrike == null || age < nextFinalGroundImpactSearchTick) {
            return;
        }

        double searchStartY = Math.max(center.y + Math.max(0.0f, SlashCofig.WorldSlash.Domain.GROUND_SEARCH_ABOVE), Math.max(finalStrike.start().y, finalStrike.end().y) + 1.0);
        Vec3 centerGround = resolveGroundSurface(level, center.x, center.z, searchStartY);
        if (centerGround != null) {
            Vec3 estimatedContact = finalStrike.start().lerp(
                    finalStrike.end(),
                    GroundImpactTimeline.lineProgressAtGround(finalStrike, centerGround.y)
            );
            Vec3 impactGround = resolveGroundSurface(level, estimatedContact.x, estimatedContact.z, searchStartY);
            if (impactGround != null) {
                Vec3 refinedContact = finalStrike.start().lerp(
                        finalStrike.end(),
                        GroundImpactTimeline.lineProgressAtGround(finalStrike, impactGround.y)
                );
                Vec3 refinedGround = resolveGroundSurface(level, refinedContact.x, refinedContact.z, searchStartY);
                if (refinedGround != null) {
                    impactGround = refinedGround;
                    refinedContact = finalStrike.start().lerp(
                            finalStrike.end(),
                            GroundImpactTimeline.lineProgressAtGround(finalStrike, impactGround.y)
                    );
                }

                finalGroundImpactAge = Mth.ceil(GroundImpactTimeline.contactAge(finalStrike, impactGround.y));
                finalGroundImpactCenter = new Vec3(refinedContact.x, impactGround.y + SlashCofig.WorldSlash.FinalStrike.GroundImpact.GROUND_OFFSET, refinedContact.z);
            }
        }

        nextFinalGroundImpactSearchTick = age + Math.max(1, SlashCofig.WorldSlash.FinalStrike.GroundImpact.GROUND_RETRY_TICKS);
    }

    private void ensureWorldShards(float effectAge, boolean presentationVisible) {
        if (!presentationVisible || worldShardsCreated || !finalBurstSpawned) return;

        int shardAge = Math.max(0, Mth.floor(effectAge - finalBurstTick));
        int maximumLifetime = Math.max(1, SlashCofig.WorldSlash.WORLD_SHARD_LIFETIME_TICKS
                + SlashCofig.WorldSlash.WORLD_SHARD_LIFETIME_TICKS / 3);
        if (shardAge >= maximumLifetime) {
            worldShardsCreated = true;
            return;
        }

        Random shardRandom = new Random(seed ^ 0x7A41D5EED39BC62FL);
        ParticleBurst.spawnFinalWorldShards(center, radius + 3.0f, shardRandom, worldShards);
        for (int tick = 0; tick < shardAge; tick++) {
            Iterator<WorldShard> iterator = worldShards.iterator();
            while (iterator.hasNext()) {
                WorldShard shard = iterator.next();
                shard.tick();
                if (!shard.isAlive()) iterator.remove();
            }
        }
        worldShardsCreated = true;
    }

    private void emitDomainParticles(ClientLevel level, float effectAge, boolean presentationVisible) {
        if (!SlashCofig.WorldSlash.Domain.ENABLED || !SlashCofig.WorldSlash.Domain.PARTICLES_ENABLED || !presentationVisible || domainCenter == null || effectAge >= domainEndTick() - SlashCofig.WorldSlash.Domain.PARTICLE_STOP_EARLY_TICKS) {
            return;
        }

        float entrance = domainEntrance(effectAge);
        float currentRadius = DomainTimeline.radius(radius, entrance);
        if (currentRadius <= 0.001f) return;

        ParticleBurst.spawnDomainCorrosion(level, domainCenter, currentRadius, entrance * entrance, domainParticleRandom, seed);
    }

    private float domainEntrance(float renderAge) {
        return DomainTimeline.entrance(Math.max(0.0f, renderAge - domainEntranceStartTick), SlashCofig.WorldSlash.Domain.INTRO_TICKS);
    }

    private static void drawRadialDisc(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vec3 center, float radius, float centerAlpha) {
        if (radius <= 0.001f || centerAlpha <= 0.001f) return;

        int safeSegments = Math.max(16, SlashCofig.WorldSlash.Domain.SEGMENTS);
        Vec3 localCenter = center.subtract(camera);
        Matrix4f matrix = poseStack.last().pose();
        int color = SlashCofig.WorldSlash.Domain.FILL_COLOR;
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        float alpha = Mth.clamp(centerAlpha, 0.0f, 1.0f);

        for (int index = 0; index < safeSegments; index++) {
            float angle0 = Mth.TWO_PI * index / safeSegments;
            float angle1 = Mth.TWO_PI * (index + 1) / safeSegments;
            float centerX = (float) localCenter.x;
            float centerY = (float) localCenter.y;
            float centerZ = (float) localCenter.z;
            float edgeX0 = centerX + Mth.cos(angle0) * radius;
            float edgeZ0 = centerZ + Mth.sin(angle0) * radius;
            float edgeX1 = centerX + Mth.cos(angle1) * radius;
            float edgeZ1 = centerZ + Mth.sin(angle1) * radius;

            consumer.vertex(matrix, centerX, centerY, centerZ).color(red, green, blue, alpha).endVertex();
            consumer.vertex(matrix, centerX, centerY, centerZ).color(red, green, blue, alpha).endVertex();
            consumer.vertex(matrix, edgeX1, centerY, edgeZ1).color(red, green, blue, 0.0f).endVertex();
            consumer.vertex(matrix, edgeX0, centerY, edgeZ0).color(red, green, blue, 0.0f).endVertex();
        }
    }

    private static void drawFeatheredGroundRing(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vec3 center, float radius, float width, int color, float alpha, float softEdge) {
        if (radius <= 0.001f || width <= 0.001f || alpha <= 0.001f) return;

        int safeSegments = Math.max(16, SlashCofig.WorldSlash.Domain.SEGMENTS);
        float halfWidth = width * 0.5f;
        float featherFraction = Mth.clamp(softEdge, 0.0f, 1.0f);
        float solidHalfWidth = halfWidth * (1.0f - featherFraction);
        float innerRadius = Math.max(0.0f, radius - halfWidth);
        float innerSolidRadius = Math.max(0.0f, radius - solidHalfWidth);
        float outerSolidRadius = radius + solidHalfWidth;
        float outerRadius = radius + halfWidth;
        Vec3 localCenter = center.subtract(camera);
        Matrix4f matrix = poseStack.last().pose();
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float ridgeAlpha = Mth.clamp(alpha, 0.0f, 1.0f);

        for (int i = 0; i < safeSegments; i++) {
            float angle0 = Mth.TWO_PI * i / safeSegments;
            float angle1 = Mth.TWO_PI * (i + 1) / safeSegments;
            emitGroundRingBand(matrix, consumer, localCenter, angle0, angle1, innerRadius, innerSolidRadius, r, g, b, 0.0f, ridgeAlpha);
            if (outerSolidRadius > innerSolidRadius + 1.0e-5f) {
                emitGroundRingBand(matrix, consumer, localCenter, angle0, angle1, innerSolidRadius, outerSolidRadius, r, g, b, ridgeAlpha, ridgeAlpha);
            }
            emitGroundRingBand(matrix, consumer, localCenter, angle0, angle1, outerSolidRadius, outerRadius, r, g, b, ridgeAlpha, 0.0f);
        }
    }

    private static void drawFeatheredGroundRingBand(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vec3 center, float radius, float outerWidth, float innerWidth, float alpha, float softEdge) {
        if (radius <= 0.001f || outerWidth <= innerWidth + 0.001f || alpha <= 0.001f) return;

        int safeSegments = Math.max(16, SlashCofig.WorldSlash.Domain.SEGMENTS);
        float outerHalfWidth = outerWidth * 0.5f;
        float innerHalfWidth = Math.max(0.0f, innerWidth * 0.5f);
        float solidFraction = 1.0f - Mth.clamp(softEdge, 0.0f, 1.0f);
        float solidHalfWidth = Mth.lerp(solidFraction, innerHalfWidth, outerHalfWidth);
        Vec3 localCenter = center.subtract(camera);
        Matrix4f matrix = poseStack.last().pose();
        int color = SlashCofig.WorldSlash.CORE_COLOR_PRIMARY;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float ridgeAlpha = Mth.clamp(alpha, 0.0f, 1.0f);

        for (int index = 0; index < safeSegments; index++) {
            float angle0 = Mth.TWO_PI * index / safeSegments;
            float angle1 = Mth.TWO_PI * (index + 1) / safeSegments;
            float negativeOuter = Math.max(0.0f, radius - outerHalfWidth);
            float negativeSolid = Math.max(0.0f, radius - solidHalfWidth);
            float negativeInner = Math.max(0.0f, radius - innerHalfWidth);
            emitGroundRingBand(matrix, consumer, localCenter, angle0, angle1, negativeOuter, negativeSolid, r, g, b, 0.0f, ridgeAlpha);
            emitGroundRingBand(matrix, consumer, localCenter, angle0, angle1, negativeSolid, negativeInner, r, g, b, ridgeAlpha, ridgeAlpha);

            float positiveInner = radius + innerHalfWidth;
            float positiveSolid = radius + solidHalfWidth;
            float positiveOuter = radius + outerHalfWidth;
            emitGroundRingBand(matrix, consumer, localCenter, angle0, angle1, positiveInner, positiveSolid, r, g, b, ridgeAlpha, ridgeAlpha);
            emitGroundRingBand(matrix, consumer, localCenter, angle0, angle1, positiveSolid, positiveOuter, r, g, b, ridgeAlpha, 0.0f);
        }
    }

    private static void emitGroundRingBand(Matrix4f matrix, VertexConsumer consumer, Vec3 center, float angle0, float angle1, float innerRadius, float outerRadius, float r, float g, float b, float innerAlpha, float outerAlpha) {
        if (outerRadius <= innerRadius + 1.0e-5f) return;

        float innerX0 = (float) center.x + Mth.cos(angle0) * innerRadius;
        float innerZ0 = (float) center.z + Mth.sin(angle0) * innerRadius;
        float innerX1 = (float) center.x + Mth.cos(angle1) * innerRadius;
        float innerZ1 = (float) center.z + Mth.sin(angle1) * innerRadius;
        float outerX0 = (float) center.x + Mth.cos(angle0) * outerRadius;
        float outerZ0 = (float) center.z + Mth.sin(angle0) * outerRadius;
        float outerX1 = (float) center.x + Mth.cos(angle1) * outerRadius;
        float outerZ1 = (float) center.z + Mth.sin(angle1) * outerRadius;
        float y = (float) center.y;

        consumer.vertex(matrix, innerX0, y, innerZ0).color(r, g, b, innerAlpha).endVertex();
        consumer.vertex(matrix, innerX1, y, innerZ1).color(r, g, b, innerAlpha).endVertex();
        consumer.vertex(matrix, outerX1, y, outerZ1).color(r, g, b, outerAlpha).endVertex();
        consumer.vertex(matrix, outerX0, y, outerZ0).color(r, g, b, outerAlpha).endVertex();
    }

    private static float worldLineWidth(SlashLine line) {
        return line.width() * SlashCofig.Quick.WORLD_SLASH_WIDTH_SCALE;
    }

    private static float whiteCoreInnerGapWidth(float blackCoreWidth) {
        float blackSoftEdge = Mth.clamp(SlashCofig.WorldSlash.MAIN_BLACK_CORE_SOFT_EDGE, 0.0f, 1.0f);
        return Math.max(0.0f, blackCoreWidth * (1.0f - blackSoftEdge * 0.5f));
    }

    private static void drawFeatheredCutTaperedRibbonAgainstBlocks(PoseStack poseStack, VertexConsumer consumer, ClientLevel level, Vec3 camera, Vec3 start, Vec3 end, Vec3 side, float width, int color, float startAlpha, float endAlpha, float softEdge, boolean normalizeCrossingAlpha) {
        Vec3 span = end.subtract(start);
        double length = span.length();
        if (length <= 1.0e-5) return;

        int segments = Math.max(8, Math.min(SlashCofig.WorldSlash.TAPER_SEGMENTS * 2, Mth.ceil(length * 8.0)));
        for (int i = 0; i < segments; i++) {
            float t0 = i / (float) segments;
            float t1 = (i + 1) / (float) segments;
            Vec3 p0 = start.lerp(end, t0);
            Vec3 p1 = start.lerp(end, t1);
            Vec3 midpoint = start.lerp(end, (t0 + t1) * 0.5f);
            if (isOccludedByBlock(level, camera, midpoint)) continue;

            float w0 = width * taperProfile(t0);
            float w1 = width * taperProfile(t1);
            float a0 = Mth.lerp(t0, startAlpha, endAlpha) * alphaProfile(t0);
            float a1 = Mth.lerp(t1, startAlpha, endAlpha) * alphaProfile(t1);
            if (normalizeCrossingAlpha) {
                a0 *= xMarkCrossingAlpha(t0);
                a1 *= xMarkCrossingAlpha(t1);
            }
            drawFeatheredRibbonSegment(poseStack, consumer, camera, p0, p1, side, w0, w1, color, a0, a1, softEdge);
        }
    }

    private static void drawFeatheredTaperedRibbonBandAgainstBlocks(PoseStack poseStack, VertexConsumer consumer, ClientLevel level, Vec3 camera, Vec3 start, Vec3 end, Vec3 side, float outerWidth, float innerWidth, int color, float startAlpha, float endAlpha) {
        Vec3 span = end.subtract(start);
        double length = span.length();
        if (length <= 1.0e-5) return;

        int segments = Math.max(8, Math.min(SlashCofig.WorldSlash.TAPER_SEGMENTS * 2, Mth.ceil(length * 8.0)));
        for (int i = 0; i < segments; i++) {
            float t0 = i / (float) segments;
            float t1 = (i + 1) / (float) segments;
            Vec3 p0 = start.lerp(end, t0);
            Vec3 p1 = start.lerp(end, t1);
            Vec3 midpoint = start.lerp(end, (t0 + t1) * 0.5f);
            if (isOccludedByBlock(level, camera, midpoint)) continue;

            float profile0 = taperProfile(t0);
            float profile1 = taperProfile(t1);
            float outer0 = outerWidth * profile0;
            float outer1 = outerWidth * profile1;
            float inner0 = innerWidth * profile0;
            float inner1 = innerWidth * profile1;
            float a0 = Mth.lerp(t0, startAlpha, endAlpha) * alphaProfile(t0) * xMarkCrossingAlpha(t0);
            float a1 = Mth.lerp(t1, startAlpha, endAlpha) * alphaProfile(t1) * xMarkCrossingAlpha(t1);
            drawFeatheredRibbonBandSegment(poseStack, consumer, camera, p0, p1, side, outer0, outer1, inner0, inner1, color, a0, a1);
        }
    }

    private static float xMarkCrossingAlpha(float t) {
        float distance = Math.abs(Mth.clamp(t, 0.0f, 1.0f) - 0.5f);
        float recovery = smooth(Mth.clamp(distance / 0.10f, 0.0f, 1.0f));
        return Mth.lerp(recovery, 0.5f, 1.0f);
    }

    private static boolean isOccludedByBlock(ClientLevel level, Vec3 camera, Vec3 point) {
        if (level == null) return false;

        double targetDistanceSqr = camera.distanceToSqr(point);
        if (targetDistanceSqr <= 1.0e-5) return false;

        HitResult hit = level.clip(new ClipContext(camera, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
        if (hit.getType() == HitResult.Type.MISS) return false;

        return camera.distanceToSqr(hit.getLocation()) < targetDistanceSqr - 0.09;
    }

    private static void drawTaperedRibbon(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vec3 start, Vec3 end, Vec3 side, float width, int color, float startAlpha, float endAlpha) {
        Vec3 span = end.subtract(start);
        double length = span.length();
        if (length <= 1.0e-5) return;

        int segments = Math.max(4, Math.min(SlashCofig.WorldSlash.TAPER_SEGMENTS, Mth.ceil(length * 2.0)));
        for (int i = 0; i < segments; i++) {
            float t0 = i / (float) segments;
            float t1 = (i + 1) / (float) segments;
            Vec3 p0 = start.lerp(end, t0);
            Vec3 p1 = start.lerp(end, t1);
            float w0 = width * taperProfile(t0);
            float w1 = width * taperProfile(t1);
            float a0 = Mth.lerp(t0, startAlpha, endAlpha) * alphaProfile(t0);
            float a1 = Mth.lerp(t1, startAlpha, endAlpha) * alphaProfile(t1);
            drawRibbonSegment(poseStack, consumer, camera, p0, p1, side, w0, w1, color, a0, a1);
        }
    }

    private static void drawTaperedRibbonBand(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vec3 start, Vec3 end, Vec3 side, float outerWidth, float innerWidth, int color, float startAlpha, float endAlpha) {
        Vec3 span = end.subtract(start);
        double length = span.length();
        if (length <= 1.0e-5) return;

        int segments = Math.max(4, Math.min(SlashCofig.WorldSlash.TAPER_SEGMENTS, Mth.ceil(length * 2.0)));
        for (int i = 0; i < segments; i++) {
            float t0 = i / (float) segments;
            float t1 = (i + 1) / (float) segments;
            Vec3 p0 = start.lerp(end, t0);
            Vec3 p1 = start.lerp(end, t1);
            float profile0 = taperProfile(t0);
            float profile1 = taperProfile(t1);
            float outer0 = outerWidth * profile0;
            float outer1 = outerWidth * profile1;
            float inner0 = innerWidth * profile0;
            float inner1 = innerWidth * profile1;
            float a0 = Mth.lerp(t0, startAlpha, endAlpha) * alphaProfile(t0);
            float a1 = Mth.lerp(t1, startAlpha, endAlpha) * alphaProfile(t1);
            drawRibbonBandSegment(poseStack, consumer, camera, p0, p1, side, outer0, outer1, inner0, inner1, color, a0, a1);
        }
    }

    private static void drawFeatheredTaperedRibbon(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vec3 start, Vec3 end, Vec3 side, float width, int color, float startAlpha, float endAlpha, float softEdge) {
        Vec3 span = end.subtract(start);
        double length = span.length();
        if (length <= 1.0e-5) return;

        int segments = Math.max(4, Math.min(SlashCofig.WorldSlash.TAPER_SEGMENTS, Mth.ceil(length * 2.0)));
        for (int i = 0; i < segments; i++) {
            float t0 = i / (float) segments;
            float t1 = (i + 1) / (float) segments;
            Vec3 p0 = start.lerp(end, t0);
            Vec3 p1 = start.lerp(end, t1);
            float w0 = width * taperProfile(t0);
            float w1 = width * taperProfile(t1);
            float a0 = Mth.lerp(t0, startAlpha, endAlpha) * alphaProfile(t0);
            float a1 = Mth.lerp(t1, startAlpha, endAlpha) * alphaProfile(t1);
            drawFeatheredRibbonSegment(poseStack, consumer, camera, p0, p1, side, w0, w1, color, a0, a1, softEdge);
        }
    }

    private static void drawFeatheredTaperedRibbonBand(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vec3 start, Vec3 end, Vec3 side, float outerWidth, float innerWidth, int color, float startAlpha, float endAlpha) {
        Vec3 span = end.subtract(start);
        double length = span.length();
        if (length <= 1.0e-5) return;

        int segments = Math.max(4, Math.min(SlashCofig.WorldSlash.TAPER_SEGMENTS, Mth.ceil(length * 2.0)));
        for (int i = 0; i < segments; i++) {
            float t0 = i / (float) segments;
            float t1 = (i + 1) / (float) segments;
            Vec3 p0 = start.lerp(end, t0);
            Vec3 p1 = start.lerp(end, t1);
            float profile0 = taperProfile(t0);
            float profile1 = taperProfile(t1);
            float outer0 = outerWidth * profile0;
            float outer1 = outerWidth * profile1;
            float inner0 = innerWidth * profile0;
            float inner1 = innerWidth * profile1;
            float a0 = Mth.lerp(t0, startAlpha, endAlpha) * alphaProfile(t0);
            float a1 = Mth.lerp(t1, startAlpha, endAlpha) * alphaProfile(t1);
            drawFeatheredRibbonBandSegment(poseStack, consumer, camera, p0, p1, side, outer0, outer1, inner0, inner1, color, a0, a1);
        }
    }

    private static float taperProfile(float t) {
        t = Mth.clamp(t, 0.0f, 1.0f);
        float endTaper = Mth.sin(Mth.PI * t);
        endTaper = (float) Math.pow(Math.max(0.0f, endTaper), SlashCofig.WorldSlash.TAPER_POWER);
        float bladeBelly = SlashCofig.WorldSlash.TAPER_BELLY_MIN + (1.0f - SlashCofig.WorldSlash.TAPER_BELLY_MIN) * Mth.sin(Mth.PI * t);
        float midWidth = (float) Math.pow(Math.max(0.0f, Mth.sin(Mth.PI * t)), SlashCofig.WorldSlash.TAPER_MID_WIDTH_POWER);
        float midBoost = Mth.lerp(midWidth, 1.0f, SlashCofig.WorldSlash.TAPER_MID_WIDTH_BOOST);
        return endTaper * bladeBelly * midBoost;
    }

    private static float alphaProfile(float t) {
        t = Mth.clamp(t, 0.0f, 1.0f);
        float fade = Mth.sin(Mth.PI * t);
        return Mth.clamp((float) Math.pow(Math.max(0.0f, fade), SlashCofig.WorldSlash.ALPHA_POWER) * SlashCofig.WorldSlash.ALPHA_MULTIPLIER, 0.0f, 1.0f);
    }

    private static float smooth(float value) {
        value = Mth.clamp(value, 0.0f, 1.0f);
        return value * value * (3.0f - 2.0f * value);
    }

    private static void drawRibbonSegment(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vec3 start, Vec3 end, Vec3 side, float startWidth, float endWidth, int color, float startAlpha, float endAlpha) {
        Vec3 halfStart = side.scale(startWidth * 0.5f);
        Vec3 halfEnd = side.scale(endWidth * 0.5f);
        Vec3 p0 = start.add(halfStart).subtract(camera);
        Vec3 p1 = start.subtract(halfStart).subtract(camera);
        Vec3 p2 = end.subtract(halfEnd).subtract(camera);
        Vec3 p3 = end.add(halfEnd).subtract(camera);

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        Matrix4f matrix = poseStack.last().pose();
        consumer.vertex(matrix, (float) p0.x, (float) p0.y, (float) p0.z).color(r, g, b, Mth.clamp(startAlpha, 0.0f, 1.0f)).endVertex();
        consumer.vertex(matrix, (float) p1.x, (float) p1.y, (float) p1.z).color(r, g, b, Mth.clamp(startAlpha, 0.0f, 1.0f)).endVertex();
        consumer.vertex(matrix, (float) p2.x, (float) p2.y, (float) p2.z).color(r, g, b, Mth.clamp(endAlpha, 0.0f, 1.0f)).endVertex();
        consumer.vertex(matrix, (float) p3.x, (float) p3.y, (float) p3.z).color(r, g, b, Mth.clamp(endAlpha, 0.0f, 1.0f)).endVertex();
    }

    private static void drawRibbonBandSegment(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vec3 start, Vec3 end, Vec3 side, float startOuterWidth, float endOuterWidth, float startInnerWidth, float endInnerWidth, int color, float startAlpha, float endAlpha) {
        if (startOuterWidth <= startInnerWidth + 1.0e-5f && endOuterWidth <= endInnerWidth + 1.0e-5f) return;

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a0 = Mth.clamp(startAlpha, 0.0f, 1.0f);
        float a1 = Mth.clamp(endAlpha, 0.0f, 1.0f);
        Matrix4f matrix = poseStack.last().pose();

        emitRibbonBandQuad(matrix, consumer, camera, start, end, side, startInnerWidth * 0.5f, endInnerWidth * 0.5f, startOuterWidth * 0.5f, endOuterWidth * 0.5f, r, g, b, a0, a1, a0, a1);
        emitRibbonBandQuad(matrix, consumer, camera, start, end, side, -startInnerWidth * 0.5f, -endInnerWidth * 0.5f, -startOuterWidth * 0.5f, -endOuterWidth * 0.5f, r, g, b, a0, a1, a0, a1);
    }

    private static void drawFeatheredRibbonSegment(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vec3 start, Vec3 end, Vec3 side, float startWidth, float endWidth, int color, float startAlpha, float endAlpha, float softEdge) {
        if (startWidth <= 1.0e-5f && endWidth <= 1.0e-5f) return;

        float featherFraction = Mth.clamp(softEdge, 0.0f, 1.0f);
        if (featherFraction <= 1.0e-4f) {
            drawRibbonSegment(poseStack, consumer, camera, start, end, side, startWidth, endWidth, color, startAlpha, endAlpha);
            return;
        }

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        float solidFraction = 1.0f - featherFraction;
        Vec3 outerHalfStart = side.scale(startWidth * 0.5f);
        Vec3 outerHalfEnd = side.scale(endWidth * 0.5f);
        Vec3 innerHalfStart = side.scale(startWidth * solidFraction * 0.5f);
        Vec3 innerHalfEnd = side.scale(endWidth * solidFraction * 0.5f);

        Vec3 rightOuterStart = start.add(outerHalfStart).subtract(camera);
        Vec3 rightInnerStart = start.add(innerHalfStart).subtract(camera);
        Vec3 leftInnerStart = start.subtract(innerHalfStart).subtract(camera);
        Vec3 leftOuterStart = start.subtract(outerHalfStart).subtract(camera);

        Vec3 rightOuterEnd = end.add(outerHalfEnd).subtract(camera);
        Vec3 rightInnerEnd = end.add(innerHalfEnd).subtract(camera);
        Vec3 leftInnerEnd = end.subtract(innerHalfEnd).subtract(camera);
        Vec3 leftOuterEnd = end.subtract(outerHalfEnd).subtract(camera);

        float a0 = Mth.clamp(startAlpha, 0.0f, 1.0f);
        float a1 = Mth.clamp(endAlpha, 0.0f, 1.0f);
        Matrix4f matrix = poseStack.last().pose();

        if (solidFraction > 1.0e-4f) {
            consumer.vertex(matrix, (float) rightInnerStart.x, (float) rightInnerStart.y, (float) rightInnerStart.z).color(r, g, b, a0).endVertex();
            consumer.vertex(matrix, (float) leftInnerStart.x, (float) leftInnerStart.y, (float) leftInnerStart.z).color(r, g, b, a0).endVertex();
            consumer.vertex(matrix, (float) leftInnerEnd.x, (float) leftInnerEnd.y, (float) leftInnerEnd.z).color(r, g, b, a1).endVertex();
            consumer.vertex(matrix, (float) rightInnerEnd.x, (float) rightInnerEnd.y, (float) rightInnerEnd.z).color(r, g, b, a1).endVertex();
        }

        consumer.vertex(matrix, (float) rightOuterStart.x, (float) rightOuterStart.y, (float) rightOuterStart.z).color(r, g, b, 0.0f).endVertex();
        consumer.vertex(matrix, (float) rightInnerStart.x, (float) rightInnerStart.y, (float) rightInnerStart.z).color(r, g, b, a0).endVertex();
        consumer.vertex(matrix, (float) rightInnerEnd.x, (float) rightInnerEnd.y, (float) rightInnerEnd.z).color(r, g, b, a1).endVertex();
        consumer.vertex(matrix, (float) rightOuterEnd.x, (float) rightOuterEnd.y, (float) rightOuterEnd.z).color(r, g, b, 0.0f).endVertex();

        consumer.vertex(matrix, (float) leftInnerStart.x, (float) leftInnerStart.y, (float) leftInnerStart.z).color(r, g, b, a0).endVertex();
        consumer.vertex(matrix, (float) leftOuterStart.x, (float) leftOuterStart.y, (float) leftOuterStart.z).color(r, g, b, 0.0f).endVertex();
        consumer.vertex(matrix, (float) leftOuterEnd.x, (float) leftOuterEnd.y, (float) leftOuterEnd.z).color(r, g, b, 0.0f).endVertex();
        consumer.vertex(matrix, (float) leftInnerEnd.x, (float) leftInnerEnd.y, (float) leftInnerEnd.z).color(r, g, b, a1).endVertex();
    }

    private static void drawFeatheredRibbonBandSegment(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vec3 start, Vec3 end, Vec3 side, float startOuterWidth, float endOuterWidth, float startInnerWidth, float endInnerWidth, int color, float startAlpha, float endAlpha) {
        if (startOuterWidth <= startInnerWidth + 1.0e-5f && endOuterWidth <= endInnerWidth + 1.0e-5f) return;

        float featherFraction = Mth.clamp(SlashCofig.WorldSlash.MAIN_CORE_SOFT_EDGE, 0.0f, 1.0f);
        if (featherFraction <= 1.0e-4f) {
            drawRibbonBandSegment(poseStack, consumer, camera, start, end, side, startOuterWidth, endOuterWidth, startInnerWidth, endInnerWidth, color, startAlpha, endAlpha);
            return;
        }

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a0 = Mth.clamp(startAlpha, 0.0f, 1.0f);
        float a1 = Mth.clamp(endAlpha, 0.0f, 1.0f);
        float solidFraction = 1.0f - featherFraction;
        Matrix4f matrix = poseStack.last().pose();

        float startInnerHalf = startInnerWidth * 0.5f;
        float endInnerHalf = endInnerWidth * 0.5f;
        float startOuterHalf = startOuterWidth * 0.5f;
        float endOuterHalf = endOuterWidth * 0.5f;
        float startSolidHalf = Mth.lerp(solidFraction, startInnerHalf, startOuterHalf);
        float endSolidHalf = Mth.lerp(solidFraction, endInnerHalf, endOuterHalf);

        if (solidFraction > 1.0e-4f) {
            emitRibbonBandQuad(matrix, consumer, camera, start, end, side, startInnerHalf, endInnerHalf, startSolidHalf, endSolidHalf, r, g, b, a0, a1, a0, a1);
            emitRibbonBandQuad(matrix, consumer, camera, start, end, side, -startInnerHalf, -endInnerHalf, -startSolidHalf, -endSolidHalf, r, g, b, a0, a1, a0, a1);
        }

        emitRibbonBandQuad(matrix, consumer, camera, start, end, side, startSolidHalf, endSolidHalf, startOuterHalf, endOuterHalf, r, g, b, a0, a1, 0.0f, 0.0f);
        emitRibbonBandQuad(matrix, consumer, camera, start, end, side, -startSolidHalf, -endSolidHalf, -startOuterHalf, -endOuterHalf, r, g, b, a0, a1, 0.0f, 0.0f);
    }

    private static void emitRibbonBandQuad(Matrix4f matrix, VertexConsumer consumer, Vec3 camera, Vec3 start, Vec3 end, Vec3 side, float startA, float endA, float startB, float endB, float r, float g, float b, float startAlphaA, float endAlphaA, float startAlphaB, float endAlphaB) {
        Vec3 p0 = start.add(side.scale(startA)).subtract(camera);
        Vec3 p1 = start.add(side.scale(startB)).subtract(camera);
        Vec3 p2 = end.add(side.scale(endB)).subtract(camera);
        Vec3 p3 = end.add(side.scale(endA)).subtract(camera);

        consumer.vertex(matrix, (float) p0.x, (float) p0.y, (float) p0.z).color(r, g, b, Mth.clamp(startAlphaA, 0.0f, 1.0f)).endVertex();
        consumer.vertex(matrix, (float) p1.x, (float) p1.y, (float) p1.z).color(r, g, b, Mth.clamp(startAlphaB, 0.0f, 1.0f)).endVertex();
        consumer.vertex(matrix, (float) p2.x, (float) p2.y, (float) p2.z).color(r, g, b, Mth.clamp(endAlphaB, 0.0f, 1.0f)).endVertex();
        consumer.vertex(matrix, (float) p3.x, (float) p3.y, (float) p3.z).color(r, g, b, Mth.clamp(endAlphaA, 0.0f, 1.0f)).endVertex();
    }

    private static Vec3 cameraFacingSide(Vec3 start, Vec3 end, Vec3 camera, Vec3 fallback) {
        Vec3 direction = end.subtract(start);
        if (direction.lengthSqr() < 1.0e-8) return fallback.normalize();

        Vec3 midpoint = start.add(end).scale(0.5);
        Vec3 toCamera = camera.subtract(midpoint);
        Vec3 side = direction.normalize().cross(toCamera);
        if (side.lengthSqr() < 1.0e-8) side = fallback;
        if (side.lengthSqr() < 1.0e-8) side = new Vec3(0.0, 1.0, 0.0);
        return side.normalize();
    }

    private static int fixedOuterColor(int index) {
        return switch (Math.floorMod(index, 4)) {
            case 1 -> SlashCofig.WorldSlash.OUTER_COLOR_ALT_A;
            case 2 -> SlashCofig.WorldSlash.OUTER_COLOR_ALT_B;
            default -> SlashCofig.WorldSlash.OUTER_COLOR_PRIMARY;
        };
    }
}
