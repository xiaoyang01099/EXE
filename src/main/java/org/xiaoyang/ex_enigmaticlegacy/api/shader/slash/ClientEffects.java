package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXELatePassState;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXERenderFrameState;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXERenderLifecycle;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.EXEShaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Exe.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientEffects {
    private static final int MAX_ACTIVE_EFFECTS_PER_OWNER = 4; // 每位施放者的并存轮数
    private static final int MAX_ACTIVE_EFFECTS_TOTAL = 32;
    private static final int MAX_SLASH_IMPACT_PULSES = 16;
    private static final float MIN_SCREEN_BREAK_LINE_LENGTH_PIXELS = 24.0f;
    private static final int SCREEN_CRACK_CENTER_CORE_LINES = 2;
    private static final float SCREEN_CRACK_SOFT_CENTER_MIN_RADIUS = 0.045f;
    private static final float SCREEN_CRACK_SOFT_CENTER_RANDOM_RADIUS = 0.105f;
    private static final ScreenEffects.ScreenBreakLine FALLBACK_SCREEN_BREAK_LINE = new ScreenEffects.ScreenBreakLine(0.0f, 0.5f, 1.0f, 0.5f);
    private static final List<SlashEffect> EFFECTS = new ArrayList<>();
    private static final List<SlashImpactPulse> SLASH_IMPACTS = new ArrayList<>();
    private static List<SlashLine> pendingScreenBreakWorldLines = List.of();
    private static List<ScreenEffects.ScreenBreakLine> lastScreenGlassCrackLines = List.of();
    private static List<DistortionRenderer.ScreenLine> currentDirectionalUvOffsetLines = List.of();
    private static boolean pendingScreenBreakProjection;
    private static long pendingScreenBreakCrackSeed;
    private static boolean currentDirectionalUvOffsetUsesBreakCracks;
    private static float currentVoronoiStrength;
    private static float currentVoronoiAge;
    private static long currentVoronoiSeed;
    private static SlashEffect pendingDesaturationAudioOwner;
    private static float currentSlashImpactStrength;
    private static float currentSlashImpactDirectionX = 1.0f;
    private static float currentSlashImpactDirectionY;
    private static SlashEffect activeScreenBreakOwner;
    private static ClientLevel activeLevel;
    private static LateWorldRenderState pendingLateWorldRender;
    private static boolean deferWorldEffectsThisFrame;
    private static boolean oculusPipelineStateInitialized;
    private static boolean shaderPackActiveLastFrame;
    private static ClientLevel oculusPipelineLevelLastFrame;
    private static Object oculusPipelineIdentityLastFrame;
    private static int oculusPipelineVersionLastFrame = Integer.MIN_VALUE;
    private static final int NO_PACK_TRANSITION_QUIET_FRAMES = 4;
    private static int noPackTransitionQuietFrames;
    private static boolean customRenderingSuppressedThisFrame;
    private static boolean frameShaderModeInitialized;
    private static boolean shaderPackActivePreviousFrame;

    private ClientEffects() {
    }

    public static void trigger(Vec3 center, int slashCount, float length, float radius, long seed, Vec3 facing, UUID ownerId) {
        if (!VisualToggle.areEffectsEnabled() || cannotReceiveEffects()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (activeLevel == null) {
            activeLevel = minecraft.level;
        } else if (activeLevel != minecraft.level) {
            EXERenderLifecycle.clearForWorldUnload();
            clearClientState(true);
            activeLevel = minecraft.level;
        }
        int evictionIndex;
        while ((evictionIndex = EffectCapacity.oldestIndexToEvict(
                EFFECTS.stream().map(SlashEffect::sourceOwnerId).toList(),
                ownerId,
                MAX_ACTIVE_EFFECTS_PER_OWNER
        )) >= 0) {
            SlashEffect removed = EFFECTS.remove(evictionIndex);
            onEffectRemoved(removed);
        }
        while (EFFECTS.size() >= MAX_ACTIVE_EFFECTS_TOTAL) {
            SlashEffect removed = EFFECTS.remove(0);
            onEffectRemoved(removed);
        }
        boolean allowsScreenEffects = AudiencePolicy.allowsScreenEffects(ownerId, Objects.requireNonNull(minecraft.player).getUUID());
        SlashEffect effect = SlashEffect.create(center, slashCount, length, radius, seed, facing, allowsScreenEffects, ownerId);
        effect.updateViewerPosition(minecraft.level, minecraft.player.position());
        EFFECTS.add(effect);
    }

    static void triggerSlashImpact(SlashEffect owner, SlashLine line, long seed, long gameTick) {
        if (!VisualToggle.areEffectsEnabled() || !SlashCofig.ScreenBreak.PerSlashImpact.ENABLED || cannotReceiveEffects() || owner == null || owner.suppressesScreenEffects() || !owner.isPresentationVisible()) return;

        while (SLASH_IMPACTS.size() >= MAX_SLASH_IMPACT_PULSES) {
            SLASH_IMPACTS.remove(0);
        }
        SLASH_IMPACTS.add(new SlashImpactPulse(owner, line, gameTick));
        float cameraImpactScale = line.isFinalStrike() ? SlashCofig.WorldSlash.FinalStrike.CAMERA_IMPACT_SCALE : 1.0f;
        EarthquakeVisualsManager.triggerDimensionalSlashImpact(
                owner.presentationOwnerId(),
                gameTick,
                seed,
                SlashCofig.ScreenBreak.PerSlashImpact.DURATION_TICKS,
                SlashCofig.ScreenBreak.PerSlashImpact.ATTACK_TICKS,
                SlashCofig.ScreenBreak.PerSlashImpact.CAMERA_AMPLITUDE_DEGREES * cameraImpactScale,
                SlashCofig.ScreenBreak.PerSlashImpact.CAMERA_MAX_ANGLE_DEGREES * cameraImpactScale,
                SlashCofig.ScreenBreak.PerSlashImpact.CAMERA_YAW_WEIGHT,
                SlashCofig.ScreenBreak.PerSlashImpact.CAMERA_PITCH_WEIGHT,
                SlashCofig.ScreenBreak.PerSlashImpact.CAMERA_ROLL_WEIGHT
        );
    }

    private static void activateFinalScreenBreak(SlashEffect owner) {
        if (!VisualToggle.areEffectsEnabled() || cannotReceiveEffects() || owner == null
                || owner.suppressesScreenEffects() || !owner.hasPresentationForFrame()) return;

        clearScreenPresentation();
        float screenAge = Math.max(0.0f, owner.screenBreakAge());
        activeScreenBreakOwner = owner;
        if (ScreenEffects.trigger(owner.screenBreakSeed(), owner.screenMotionBlurLeadTicks(), screenAge)) {
            pendingScreenBreakWorldLines = owner.screenBreakLines();
            pendingScreenBreakProjection = true;
            pendingScreenBreakCrackSeed = owner.roundId();
            if (screenAge < PresentationPolicy.screenFreezeReleaseAge()
                    && FreezeRenderer.requestFinalBreakStart()) {
                pendingDesaturationAudioOwner = owner.isAwaitingDesaturationAudio(1.0f) ? owner : null;
            }
        } else {
            pendingScreenBreakWorldLines = List.of();
            pendingScreenBreakProjection = false;
            pendingScreenBreakCrackSeed = 0L;
        }
        EarthquakeVisualsManager.triggerDimensionalSlash(
                owner.presentationOwnerId(),
                SlashCofig.ScreenBreak.CAMERA_SHAKE_AMPLITUDE_DEGREES,
                SlashCofig.ScreenBreak.CAMERA_SHAKE_DURATION_TICKS,
                SlashCofig.ScreenBreak.CAMERA_SHAKE_FREQUENCY,
                SlashCofig.ScreenBreak.CAMERA_SHAKE_MAX_ANGLE_DEGREES,
                SlashCofig.ScreenBreak.CAMERA_SHAKE_ROUGHNESS,
                SlashCofig.ScreenBreak.CAMERA_SHAKE_YAW_WEIGHT,
                SlashCofig.ScreenBreak.CAMERA_SHAKE_PITCH_WEIGHT,
                SlashCofig.ScreenBreak.CAMERA_SHAKE_ROLL_WEIGHT,
                screenAge
        );
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null || !VisualToggle.areEffectsEnabled()) {
            if (level == null && activeLevel != null) {
                EXERenderLifecycle.clearForWorldUnload();
            }
            clearClientState(level == null);
            if (level == null) activeLevel = null;
            return;
        }
        if (activeLevel != level) {
            EXERenderLifecycle.clearForWorldUnload();
            clearClientState(true);
            activeLevel = level;
        }
        if (PauseRenderState.isFrozen()) return;
        Sounds.tick();

        long gameTick = level.getGameTime();
        SLASH_IMPACTS.removeIf(pulse -> gameTick - pulse.startGameTick() >= SlashCofig.ScreenBreak.PerSlashImpact.DURATION_TICKS || !EFFECTS.contains(pulse.owner()));
        if (SLASH_IMPACTS.isEmpty()) {
            currentSlashImpactStrength = 0.0f;
            currentSlashImpactDirectionX = 1.0f;
            currentSlashImpactDirectionY = 0.0f;
        }

        EFFECTS.removeIf(dimensionalSlashEffect -> {
            dimensionalSlashEffect.updateViewerPosition(level, mc.player.position());
            if (dimensionalSlashEffect.tick(level)) return false;
            onEffectRemoved(dimensionalSlashEffect);
            return true;
        });
        syncFinalScreenBreakOwner();
        cleanupRangeHiddenPresentation();

        if (EFFECTS.isEmpty()) {
            lastScreenGlassCrackLines = List.of();
            currentDirectionalUvOffsetLines = List.of();
            currentDirectionalUvOffsetUsesBreakCracks = false;
            currentVoronoiStrength = 0.0f;
            currentVoronoiAge = 0.0f;
            currentVoronoiSeed = 0L;
            pendingDesaturationAudioOwner = null;
            activeScreenBreakOwner = null;
        }

        ScreenEffects.tick();
        if (ScreenEffects.shouldReleaseScreenFreeze()) {
            FreezeRenderer.release();
        } else if (EFFECTS.isEmpty() && !ScreenEffects.isActiveOrPending()) {
            FreezeRenderer.clear();
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void clientLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ClientLevel) {
            EXERenderLifecycle.clearForWorldUnload();
        }
        if (event.getLevel() != activeLevel) return;
        clearClientState(true);
        activeLevel = null;
        resetOculusPipelineObservation();
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void renderLevel(RenderLevelStageEvent event) {
        if (!VisualToggle.areEffectsEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (customRenderingSuppressedThisFrame) return;
        if (EFFECTS.isEmpty() && SLASH_IMPACTS.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        var mainTarget = mc.getMainRenderTarget();
        float effectPartialTick = PauseRenderState.partialTick(event.getPartialTick());
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            updateSlashImpactRenderState(poseStack, camera, mainTarget.width, mainTarget.height, effectPartialTick, PauseRenderState.gameTime(mc.level.getGameTime()));
            configurePendingScreenBreakLines(poseStack, camera, mainTarget.width, mainTarget.height);
            updateDirectionalUvOffsetLines(poseStack, camera, mainTarget, effectPartialTick);
            return;
        }

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (EFFECTS.isEmpty()) return;

        if (deferWorldEffectsThisFrame) {
            pendingLateWorldRender = new LateWorldRenderState(new Matrix4f(poseStack.last().pose()), new Matrix4f(RenderSystem.getModelViewMatrix()), new Matrix4f(RenderSystem.getProjectionMatrix()), camera, effectPartialTick);
            return;
        }

        renderWorldEffects(poseStack, camera, mainTarget, effectPartialTick);
    }

    private static void synchronizeOculusPipelineState(EXERenderFrameState.Snapshot frameState) {
        if (!frameState.oculusLoaded()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            resetOculusPipelineObservation();
            return;
        }

        if (!frameState.pipelineReady()) {
            synchronizeOculusShaderModeFallback(level, frameState);
            return;
        }

        boolean shaderPackActive = frameState.shaderPackActive();
        boolean firstObservation = !oculusPipelineStateInitialized;
        boolean levelChanged = oculusPipelineLevelLastFrame != level;
        boolean pipelineChanged = oculusPipelineIdentityLastFrame != frameState.pipelineIdentity();
        boolean generationChanged = oculusPipelineVersionLastFrame != frameState.pipelineVersion();
        boolean shaderModeChanged = shaderPackActiveLastFrame != shaderPackActive;
        if (!firstObservation && !levelChanged && !pipelineChanged && !generationChanged && !shaderModeChanged) {
            return;
        }

        String reason;
        if (firstObservation) {
            reason = "initial world pipeline";
        } else if (levelChanged) {
            reason = "client level changed";
        } else if (pipelineChanged) {
            reason = "pipeline instance replaced";
        } else if (generationChanged) {
            reason = "pipeline generation advanced";
        } else {
            reason = "shader-pack mode changed";
        }

        resetRenderResourcesForShaderPipelineChange(shaderPackActive, reason, frameState.pipelineVersion());
        shaderPackActiveLastFrame = shaderPackActive;
        oculusPipelineLevelLastFrame = level;
        oculusPipelineIdentityLastFrame = frameState.pipelineIdentity();
        oculusPipelineVersionLastFrame = frameState.pipelineVersion();
        oculusPipelineStateInitialized = true;
    }

    public static void renderAfterWorldPipeline() {
        synchronizeOculusPipelineState(EXERenderFrameState.capturePipelineAfterWorldRender());

        LateWorldRenderState lateState = pendingLateWorldRender;
        pendingLateWorldRender = null;
        if (customRenderingSuppressedThisFrame || lateState == null || EFFECTS.isEmpty() || !deferWorldEffectsThisFrame) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.last().pose().set(lateState.modelView());
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f(lateState.projection()), VertexSorting.DISTANCE_TO_ORIGIN);

        PoseStack worldPoseStack = new PoseStack();
        worldPoseStack.last().pose().set(lateState.pose());
        try {
            EXELatePassState.prepare();
            renderWorldEffects(worldPoseStack, lateState.camera(), mc.getMainRenderTarget(), lateState.partialTick());
        } finally {
            EXELatePassState.finish();
            RenderSystem.setProjectionMatrix(previousProjection, VertexSorting.DISTANCE_TO_ORIGIN);
            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    public static void beginWorldRenderFrame(EXERenderFrameState.Snapshot frameState) {
        pendingLateWorldRender = null;

        boolean shaderPackActive = frameState.shaderPackActive();

        boolean shaderPackJustDisabled = frameShaderModeInitialized && shaderPackActivePreviousFrame && !shaderPackActive;

        shaderPackActivePreviousFrame = shaderPackActive;
        frameShaderModeInitialized = true;

        if (shaderPackJustDisabled) {
            noPackTransitionQuietFrames = NO_PACK_TRANSITION_QUIET_FRAMES;
        }

        customRenderingSuppressedThisFrame =
                noPackTransitionQuietFrames > 0;

        if (noPackTransitionQuietFrames > 0) {
            noPackTransitionQuietFrames--;
        }

        deferWorldEffectsThisFrame = frameState.worldRenderActive() && shaderPackActive;
    }

    @Deprecated
    public static void beforeLevelWorldDraw() {
    }

    @Deprecated
    public static void validateFinalWindowOutput(int windowWidth, int windowHeight) {
    }

    private static void renderWorldEffects(PoseStack poseStack, Vec3 camera, RenderTarget mainTarget, float effectPartialTick) {
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        BloomRenderer.beginFrame(mainTarget);
        poseStack.pushPose();
        for (SlashEffect effect : EFFECTS) {
            effect.renderBloomMask(poseStack, buffers, camera, effectPartialTick, effect.worldVisibility(effectPartialTick));
        }
        buffers.endBatch(BloomRenderer.maskRenderType());
        poseStack.popPose();
        BloomRenderer.finishAndComposite(mainTarget);

        poseStack.pushPose();
        for (SlashEffect effect : EFFECTS) {
            effect.renderBody(poseStack, buffers, camera, effectPartialTick, effect.worldVisibility(effectPartialTick));
        }
        buffers.endBatch(EXEShaders.DIMENSIONAL_SLASH_CORE);
        for (SlashEffect effect : EFFECTS) {
            effect.renderWhiteCores(poseStack, buffers, camera, effectPartialTick, effect.worldVisibility(effectPartialTick));
        }
        buffers.endBatch(EXEShaders.DIMENSIONAL_SLASH_CORE);
        buffers.endBatch(EXEShaders.DIMENSIONAL_SLASH_INK_CORE);
        poseStack.popPose();

        poseStack.pushPose();
        WorldShardRenderer.render(poseStack, EFFECTS, camera, effectPartialTick, mainTarget);
        poseStack.popPose();
    }

    public static void renderScreenOverlay(float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (customRenderingSuppressedThisFrame || mc.level == null || mc.player == null || !VisualToggle.areEffectsEnabled()) {
            lastScreenGlassCrackLines = List.of();
            currentDirectionalUvOffsetLines = List.of();
            currentDirectionalUvOffsetUsesBreakCracks = false;
            currentVoronoiStrength = 0.0f;
            currentVoronoiAge = 0.0f;
            currentVoronoiSeed = 0L;
            currentSlashImpactStrength = 0.0f;
            currentSlashImpactDirectionX = 1.0f;
            currentSlashImpactDirectionY = 0.0f;
            return;
        }
        partialTick = PauseRenderState.partialTick(partialTick);

        float presentationAlpha = screenPresentationAlpha(partialTick);
        if (presentationAlpha <= 0.001f) return;

        RenderTarget mainTarget = mc.getMainRenderTarget();
        if (!currentDirectionalUvOffsetLines.isEmpty()) {
            FreezeRenderer.requestStart();
            handleFirstDesaturationFrame(FreezeRenderer.render(mainTarget, presentationAlpha), partialTick);
            ScreenEffects.capturePending(mainTarget);
            List<DistortionRenderer.ScreenLine> renderLines = currentDirectionalUvOffsetUsesBreakCracks ? createDirectionalUvOffsetLinesFromActiveGlassCracks(currentDirectionalUvOffsetLines, partialTick, mainTarget.width, mainTarget.height) : currentDirectionalUvOffsetLines;
            DistortionRenderer.render(mainTarget, renderLines, new DistortionRenderer.VoronoiState(currentVoronoiStrength, currentVoronoiAge, currentVoronoiSeed));
            currentDirectionalUvOffsetLines = List.of();
            currentDirectionalUvOffsetUsesBreakCracks = false;
            currentVoronoiStrength = 0.0f;
            currentVoronoiAge = 0.0f;
            currentVoronoiSeed = 0L;
        } else {
            handleFirstDesaturationFrame(FreezeRenderer.render(mainTarget, presentationAlpha), partialTick);
            ScreenEffects.capturePending(mainTarget);
        }

        ScreenEffects.renderSlashImpact(mainTarget, currentSlashImpactStrength, currentSlashImpactDirectionX, currentSlashImpactDirectionY);
        renderPreBreakScreenMotionBlur(mainTarget, partialTick);
        ScreenEffects.renderPost(partialTick, presentationAlpha);
    }

    public static void renderTopChroma(float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (customRenderingSuppressedThisFrame || mc.level == null || mc.player == null || !VisualToggle.areEffectsEnabled()) {
            return;
        }
        partialTick = PauseRenderState.partialTick(partialTick);

        float strength = 0.0f;
        for (SlashEffect effect : EFFECTS) {
            strength = Math.max(strength, effect.topChromaAlpha(partialTick));
        }
        if (strength <= 0.001f) {
            return;
        }
        RenderTarget mainTarget = mc.getMainRenderTarget();
        ChromaRenderer.render(mainTarget, Mth.clamp(strength, 0.0f, 1.0f));
    }

    static boolean needsScreenOutputGuard() {
        return !customRenderingSuppressedThisFrame && VisualToggle.areEffectsEnabled() && (!EFFECTS.isEmpty() || !SLASH_IMPACTS.isEmpty() || ScreenEffects.isActiveOrPending() || PressureOverlay.hasActive());
    }

    private static void updateSlashImpactRenderState(PoseStack poseStack, Vec3 camera, int width, int height, float partialTick, long gameTick) {
        currentSlashImpactStrength = 0.0f;
        currentSlashImpactDirectionX = 1.0f;
        currentSlashImpactDirectionY = 0.0f;
        if (SLASH_IMPACTS.isEmpty() || width <= 0 || height <= 0) return;

        float strongestProjectedStrength = 0.0f;
        ScreenEffects.ScreenBreakLine strongestProjectedLine = null;
        for (SlashImpactPulse pulse : SLASH_IMPACTS) {
            float rangeVisibility = pulse.owner().rangeVisibility(partialTick);
            if (rangeVisibility <= 0.001f) continue;
            float age = Math.max(0.0f, gameTick - pulse.startGameTick() + partialTick);
            float strength = ImpactTimeline.envelope(age, SlashCofig.ScreenBreak.PerSlashImpact.DURATION_TICKS, SlashCofig.ScreenBreak.PerSlashImpact.ATTACK_TICKS) * rangeVisibility * (pulse.line().isFinalStrike() ? SlashCofig.WorldSlash.FinalStrike.SCREEN_IMPACT_SCALE : 1.0f);
            if (strength <= 0.001f) continue;

            currentSlashImpactStrength = ImpactTimeline.combineSaturated(currentSlashImpactStrength, strength);
            ScreenEffects.ScreenBreakLine projected = projectLineToScreen(poseStack, camera, pulse.line());
            if (projected != null && strength > strongestProjectedStrength) {
                strongestProjectedStrength = strength;
                strongestProjectedLine = projected;
            }
        }

        if (strongestProjectedLine == null) return;
        float slashDirectionX = (strongestProjectedLine.x1() - strongestProjectedLine.x0()) * width;
        float slashDirectionY = (strongestProjectedLine.y1() - strongestProjectedLine.y0()) * height;
        ImpactTimeline.ImpactDirection impactDirection = ImpactTimeline.perpendicularUvDirection(slashDirectionX, slashDirectionY);
        currentSlashImpactDirectionX = impactDirection.x();
        currentSlashImpactDirectionY = impactDirection.y();
    }

    private static void clearSlashImpacts() {
        SLASH_IMPACTS.clear();
        currentSlashImpactStrength = 0.0f;
        currentSlashImpactDirectionX = 1.0f;
        currentSlashImpactDirectionY = 0.0f;
        EarthquakeVisualsManager.clearSlashEffects();
    }

    static boolean cannotReceiveEffects() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null || minecraft.player == null;
    }

    private static void clearScreenPresentation() {
        if (activeScreenBreakOwner != null) {
            EarthquakeVisualsManager.cancelDimensionalSlashCurve(activeScreenBreakOwner.presentationOwnerId());
        }
        DistortionRenderer.clear();
        FreezeRenderer.clear();
        ScreenEffects.clear();
        pendingScreenBreakWorldLines = List.of();
        lastScreenGlassCrackLines = List.of();
        currentDirectionalUvOffsetLines = List.of();
        pendingScreenBreakProjection = false;
        pendingScreenBreakCrackSeed = 0L;
        currentDirectionalUvOffsetUsesBreakCracks = false;
        currentVoronoiStrength = 0.0f;
        currentVoronoiAge = 0.0f;
        currentVoronoiSeed = 0L;
        pendingDesaturationAudioOwner = null;
        activeScreenBreakOwner = null;
    }

    private static void synchronizeOculusShaderModeFallback(ClientLevel level, EXERenderFrameState.Snapshot frameState) {
        boolean shaderPackActive = frameState.shaderPackActive();
        boolean firstObservation = !oculusPipelineStateInitialized;
        boolean levelChanged = oculusPipelineLevelLastFrame != level;
        boolean shaderModeChanged = shaderPackActiveLastFrame != shaderPackActive;
        if (!firstObservation && !levelChanged && !shaderModeChanged) {
            return;
        }

        String reason = firstObservation
                ? "initial world pipeline (generation unavailable)"
                : levelChanged
                ? "client level changed (generation unavailable)"
                : "shader-pack mode changed (generation unavailable)";
        resetRenderResourcesForShaderPipelineChange(shaderPackActive, reason, Integer.MIN_VALUE);
        shaderPackActiveLastFrame = shaderPackActive;
        oculusPipelineLevelLastFrame = level;
        oculusPipelineIdentityLastFrame = null;
        oculusPipelineVersionLastFrame = Integer.MIN_VALUE;
        oculusPipelineStateInitialized = true;
    }

    private static void resetOculusPipelineObservation() {
        oculusPipelineStateInitialized = false;
        shaderPackActiveLastFrame = false;
        oculusPipelineLevelLastFrame = null;
        oculusPipelineIdentityLastFrame = null;
        oculusPipelineVersionLastFrame = Integer.MIN_VALUE;
        noPackTransitionQuietFrames = 0;
        customRenderingSuppressedThisFrame = false;
    }

    private static void resetRenderResourcesForShaderPipelineChange(boolean shaderPackActive, String reason, int pipelineGeneration) {
        pendingLateWorldRender = null;

        BloomRenderer.clear();
        WorldShardRenderer.clear();
        ChromaRenderer.clear();
        ScreenOutputGuard.clear();
        WindowOutputGuard.clear();
        clearScreenPresentation();
        if (shaderPackActive) {
            EXELatePassState.repairAfterPipelineSwitch();
        }
        Exe.LOGGER.info("[DimensionalSlash] Synchronized Oculus {} ({}, generation={}); rebuilt owned render targets; left the no-pack pipeline untouched={}", shaderPackActive ? "enabled" : "disabled", reason, pipelineGeneration, !shaderPackActive);
    }

    private static void cleanupRangeHiddenPresentation() {
        if (pendingDesaturationAudioOwner != null && !pendingDesaturationAudioOwner.isPresentationVisible()) {
            pendingDesaturationAudioOwner = null;
        }
        if (EFFECTS.stream().noneMatch(SlashEffect::hasPresentationForFrame)) {
            clearSlashImpacts();
        }
    }

    private static void syncFinalScreenBreakOwner() {
        SlashEffect candidate = null;
        float candidateAge = Float.POSITIVE_INFINITY;
        for (SlashEffect effect : EFFECTS) {
            if (!effect.hasPresentationForFrame() || !effect.isFinalScreenTimelineActive()) continue;

            float screenAge = effect.screenBreakAge();
            if (PresentationPolicy.prefersNewer(screenAge, candidateAge)) {
                candidate = effect;
                candidateAge = screenAge;
            }
        }

        if (candidate == activeScreenBreakOwner) return;
        if (candidate == null) {
            clearScreenPresentation();
            return;
        }
        activateFinalScreenBreak(candidate);
    }

    private static void onEffectRemoved(SlashEffect effect) {
        if (pendingDesaturationAudioOwner == effect) pendingDesaturationAudioOwner = null;
        if (activeScreenBreakOwner == effect) clearScreenPresentation();
        SLASH_IMPACTS.removeIf(pulse -> pulse.owner() == effect);
        EarthquakeVisualsManager.removeDimensionalSlashOwner(effect.presentationOwnerId());
        effect.stopSounds();
    }

    static float combinedRangeVisibility(float partialTick) {
        float visibility = 0.0f;
        for (SlashEffect effect : EFFECTS) {
            if (effect.suppressesScreenEffects()) continue;
            visibility = Math.max(visibility, effect.rangeVisibility(partialTick));
        }
        return Mth.clamp(visibility, 0.0f, 1.0f);
    }

    public static float rangeVisibility(long roundId, float partialTick) {
        for (SlashEffect effect : EFFECTS) {
            if (effect.roundId() == roundId) return effect.rangeVisibility(partialTick);
        }
        return 0.0f;
    }

    public static float worldVisibility(long roundId, float partialTick) {
        for (SlashEffect effect : EFFECTS) {
            if (effect.roundId() == roundId) return effect.worldVisibility(partialTick);
        }
        return 0.0f;
    }

    static boolean hasRound(long roundId) {
        for (SlashEffect effect : EFFECTS) {
            if (effect.roundId() == roundId) return true;
        }
        return false;
    }

    private static float screenPresentationAlpha(float partialTick) {
        if (activeScreenBreakOwner != null && EFFECTS.contains(activeScreenBreakOwner)) {
            return activeScreenBreakOwner.rangeVisibility(partialTick);
        }
        return combinedRangeVisibility(partialTick);
    }

    private static void clearClientState(boolean clearAllFreezeState) {
        for (SlashEffect effect : EFFECTS) {
            effect.stopSounds();
        }
        EFFECTS.clear();
        Sounds.clear();
        CorrosionEffects.clear();
        PressureOverlay.clear();
        BloomRenderer.clear();
        DistortionRenderer.clear();
        if (clearAllFreezeState) {
            FreezeRenderer.clearAll();
        } else {
            FreezeRenderer.clear();
        }
        WorldShardRenderer.clear();
        ScreenEffects.clear();
        ChromaRenderer.clear();
        ScreenOutputGuard.clear();
        WindowOutputGuard.clear();
        PauseRenderState.clear();
        noPackTransitionQuietFrames = 0;
        customRenderingSuppressedThisFrame = false;
        pendingScreenBreakWorldLines = List.of();
        lastScreenGlassCrackLines = List.of();
        currentDirectionalUvOffsetLines = List.of();
        pendingScreenBreakProjection = false;
        pendingScreenBreakCrackSeed = 0L;
        currentDirectionalUvOffsetUsesBreakCracks = false;
        currentVoronoiStrength = 0.0f;
        currentVoronoiAge = 0.0f;
        currentVoronoiSeed = 0L;
        pendingDesaturationAudioOwner = null;
        activeScreenBreakOwner = null;
        pendingLateWorldRender = null;
        clearSlashImpacts();
    }

    private record LateWorldRenderState(Matrix4f pose, Matrix4f modelView, Matrix4f projection, Vec3 camera, float partialTick) {}

    private static void handleFirstDesaturationFrame(boolean firstFrameRendered, float partialTick) {
        if (!firstFrameRendered) return;

        SlashEffect owner = pendingDesaturationAudioOwner;
        pendingDesaturationAudioOwner = null;
        if (owner == null || !EFFECTS.contains(owner) || !owner.isPresentationVisible() || !owner.isAwaitingDesaturationAudio(partialTick)) {
            owner = EFFECTS.stream().filter(effect -> activeScreenBreakOwner == null || effect == activeScreenBreakOwner).filter(SlashEffect::isPresentationVisible).filter(effect -> effect.isAwaitingDesaturationAudio(partialTick)).findFirst().orElse(null);
        }
        if (owner != null) {
            owner.onDesaturationShaderRendered();
        }
    }

    private static void renderPreBreakScreenMotionBlur(RenderTarget mainTarget, float partialTick) {
        if (ScreenEffects.isActiveOrPending()) return;

        float bestVisibility = 0.0f;
        float bestTimelineAge = 0.0f;
        float bestTimelineDuration = 1.0f;
        float bestRangeVisibility = 0.0f;
        for (SlashEffect effect : EFFECTS) {
            if (effect.suppressesScreenEffects()) continue;
            if (!effect.shouldRenderPreBreakMotionBlur(partialTick)) continue;

            float timelineAge = effect.screenMotionBlurTimelineAge(partialTick);
            float timelineDuration = effect.screenMotionBlurTimelineDuration();
            float rangeVisibility = effect.rangeVisibility(partialTick);
            float visibility = ScreenEffects.motionBlurVisibility(timelineAge, timelineDuration) * rangeVisibility;
            if (visibility > bestVisibility) {
                bestVisibility = visibility;
                bestTimelineAge = timelineAge;
                bestTimelineDuration = timelineDuration;
                bestRangeVisibility = rangeVisibility;
            }
        }

        if (bestVisibility > 0.001f) {
            ScreenEffects.renderPreBreakMotionBlur(mainTarget, bestTimelineAge, bestTimelineDuration, bestRangeVisibility);
        }
    }

    private static void configurePendingScreenBreakLines(PoseStack poseStack, Vec3 camera, int width, int height) {
        if (!pendingScreenBreakProjection || width <= 0 || height <= 0) return;

        List<ScreenEffects.ScreenBreakLine> projectedWorldLines = projectLinesToScreen(poseStack, camera, pendingScreenBreakWorldLines, width, height);
        List<ScreenEffects.ScreenBreakLine> glassCrackLines = createScreenGlassCrackLines(projectedWorldLines, width, height, Long.hashCode(pendingScreenBreakCrackSeed));
        lastScreenGlassCrackLines = glassCrackLines.isEmpty() ? List.of() : List.copyOf(glassCrackLines);
        List<ScreenEffects.ScreenBreakLine> fixedLines = lastScreenGlassCrackLines.isEmpty() ? List.of(FALLBACK_SCREEN_BREAK_LINE) : lastScreenGlassCrackLines;
        ScreenEffects.configurePendingBreakLines(fixedLines);
        pendingScreenBreakWorldLines = List.of();
        pendingScreenBreakProjection = false;
        pendingScreenBreakCrackSeed = 0L;
    }

    private static void updateDirectionalUvOffsetLines(PoseStack poseStack, Vec3 camera, RenderTarget mainTarget, float partialTick) {
        if (mainTarget.width <= 0 || mainTarget.height <= 0) {
            currentDirectionalUvOffsetLines = List.of();
            currentVoronoiStrength = 0.0f;
            currentVoronoiAge = 0.0f;
            currentVoronoiSeed = 0L;
            return;
        }

        float combinedAlpha = 0.0f;
        float combinedVoronoiStrength = 0.0f;
        float strongestVoronoiAge = 0.0f;
        float strongestVoronoiStrength = 0.0f;
        boolean useBreakTuning = false;
        SlashEffect desaturationAudioOwner = null;
        boolean holdScreenFreeze = false;
        int liveCrackSeed = 0x6D2B79F5;
        int voronoiSeed = 0x4B1D5EA5;
        List<ScreenEffects.ScreenBreakLine> liveScreenLines = new ArrayList<>();
        for (SlashEffect effect : EFFECTS) {
            if (effect.suppressesScreenEffects()) continue;
            if (activeScreenBreakOwner != null && effect != activeScreenBreakOwner) continue;

            float rangeVisibility = effect.rangeVisibility(partialTick);
            if (rangeVisibility <= 0.001f) continue;
            holdScreenFreeze |= effect.shouldHoldScreenFreeze(partialTick);
            if (desaturationAudioOwner == null && effect.isAwaitingDesaturationAudio(partialTick)) {
                desaturationAudioOwner = effect;
            }

            float alpha = effect.screenDistortionAlpha(partialTick) * rangeVisibility;
            if (alpha <= 0.001f) continue;

            float voronoiStrength = effect.screenVoronoiStrength(partialTick) * rangeVisibility;
            liveCrackSeed = mixHash(liveCrackSeed, Long.hashCode(effect.screenCrackSeed()));
            voronoiSeed = mixHash(voronoiSeed, Long.hashCode(effect.screenCrackSeed()));
            float effectAge = effect.renderAge(partialTick);
            boolean postBurst = effectAge >= effect.finalBurstTick();
            if (postBurst) {
                useBreakTuning = true;
            } else {
                liveScreenLines.addAll(projectLinesToScreen(poseStack, camera, effect.screenBreakLines(), mainTarget.width, mainTarget.height));
            }
            combinedAlpha = 1.0f - (1.0f - combinedAlpha) * (1.0f - alpha);
            if (voronoiStrength > 0.001f) {
                combinedVoronoiStrength = 1.0f - (1.0f - combinedVoronoiStrength) * (1.0f - voronoiStrength);
                if (voronoiStrength > strongestVoronoiStrength) {
                    strongestVoronoiStrength = voronoiStrength;
                    strongestVoronoiAge = effect.screenVoronoiAge(partialTick);
                }
            }
        }

        if (holdScreenFreeze) {
            if (FreezeRenderer.requestStart()) {
                pendingDesaturationAudioOwner = desaturationAudioOwner;
            }
        } else {
            FreezeRenderer.release();
        }

        if (combinedAlpha > 0.001f) {
            float width = useBreakTuning ? SlashCofig.ScreenBreak.DISTORTION_BREAK_WIDTH_PIXELS : SlashCofig.ScreenBreak.DISTORTION_PRE_WIDTH_PIXELS;
            float strength = useBreakTuning ? SlashCofig.ScreenBreak.DISTORTION_BREAK_STRENGTH_PIXELS : SlashCofig.ScreenBreak.DISTORTION_PRE_STRENGTH_PIXELS;
            List<ScreenEffects.ScreenBreakLine> sourceLines = useBreakTuning && !lastScreenGlassCrackLines.isEmpty() ? lastScreenGlassCrackLines : createScreenGlassCrackLines(liveScreenLines, mainTarget.width, mainTarget.height, liveCrackSeed);
            currentDirectionalUvOffsetLines = createDirectionalUvOffsetLinesFromScreenBreakLines(sourceLines, mainTarget.width, mainTarget.height, width, strength, combinedAlpha);
            currentDirectionalUvOffsetUsesBreakCracks = useBreakTuning;
            if (currentDirectionalUvOffsetLines.isEmpty()) {
                currentVoronoiStrength = 0.0f;
                currentVoronoiAge = 0.0f;
                currentVoronoiSeed = 0L;
            } else {
                currentVoronoiStrength = combinedVoronoiStrength;
                currentVoronoiAge = strongestVoronoiAge;
                currentVoronoiSeed = voronoiSeed;
            }
        } else {
            currentDirectionalUvOffsetLines = List.of();
            currentDirectionalUvOffsetUsesBreakCracks = false;
            currentVoronoiStrength = 0.0f;
            currentVoronoiAge = 0.0f;
            currentVoronoiSeed = 0L;
        }
    }

    private static List<ScreenEffects.ScreenBreakLine> projectLinesToScreen(PoseStack poseStack, Vec3 camera, List<SlashLine> lines, int width, int height) {
        if (lines == null || lines.isEmpty() || width <= 0 || height <= 0) {
            return List.of();
        }

        List<ScreenEffects.ScreenBreakLine> screenLines = new ArrayList<>();
        for (SlashLine line : lines) {
            ScreenEffects.ScreenBreakLine screenLine = projectLineToScreen(poseStack, camera, line);
            if (!isUsableScreenLine(screenLine, width, height)) {
                continue;
            }
            screenLines.add(screenLine);
        }
        return screenLines;
    }

    private static List<ScreenEffects.ScreenBreakLine> createScreenGlassCrackLines(List<ScreenEffects.ScreenBreakLine> referenceLines, int width, int height, int stableSeed) {
        if (width <= 0 || height <= 0) {
            return List.of();
        }

        int targetCount = Mth.clamp(SlashCofig.ScreenBreak.DISTORTION_LINE_COUNT, 1, SlashCofig.WorldSlash.MAX_SLASHES);
        int seed = stableSeed == 0 ? 0x811C9DC5 : stableSeed;
        float baseAngle = dominantReferenceAngle(referenceLines, width, height);
        float angleStep = 6.2831855f / targetCount;
        List<ScreenEffects.ScreenBreakLine> crackLines = new ArrayList<>(targetCount);
        for (int segment = 0; segment < targetCount; segment++) {
            float angleJitter = (hash01(seed + segment * 977 + 17) - 0.5f) * 0.22f;
            float angle = baseAngle + segment * angleStep + angleJitter;
            addDistributedScreenGlassCrackSegment(crackLines, angle, segment, targetCount, seed, width, height);
        }
        return crackLines.isEmpty() ? List.of(FALLBACK_SCREEN_BREAK_LINE) : crackLines;
    }

    private static void addDistributedScreenGlassCrackSegment(List<ScreenEffects.ScreenBreakLine> crackLines, float angle, int segment, int targetCount, int seed, int width, int height) {
        float centerX = width * 0.5f;
        float centerY = height * 0.5f;
        float minDimension = Math.max(1.0f, Math.min(width, height));
        float directionX = Mth.cos(angle);
        float directionY = Mth.sin(angle);
        boolean centerCore = isScreenCrackCenterCoreSegment(segment, targetCount);
        float originX = centerX;
        float originY = centerY;
        if (!centerCore) {
            float originAngle = angle + (hash01(seed + segment * 353 + 229) - 0.5f) * 1.30f;
            float originRadius = minDimension * (SCREEN_CRACK_SOFT_CENTER_MIN_RADIUS + hash01(seed + segment * 367 + 241) * SCREEN_CRACK_SOFT_CENTER_RANDOM_RADIUS);
            originX += Mth.cos(originAngle) * originRadius;
            originY += Mth.sin(originAngle) * originRadius;
        }

        float edgeDistance = distanceToScreenEdge(originX, originY, directionX, directionY, width, height);
        if (edgeDistance <= 1.0f) {
            return;
        }

        float normalX = -directionY;
        int bandOffset = Math.min(2, (int) (hash01(seed + segment * 409 + 211) * 3.0f));
        int band = centerCore ? 0 : Math.floorMod(segment + bandOffset, 3);
        float startT;
        float endT;
        if (centerCore) {
            startT = 0.05f + hash01(seed + segment * 131 + 41) * 0.09f;
            endT = 0.26f + hash01(seed + segment * 173 + 73) * 0.16f;
        } else if (band == 0) {
            startT = 0.16f + hash01(seed + segment * 131 + 41) * 0.14f;
            endT = 0.42f + hash01(seed + segment * 173 + 73) * 0.16f;
        } else if (band == 1) {
            startT = 0.40f + hash01(seed + segment * 191 + 89) * 0.14f;
            endT = 0.64f + hash01(seed + segment * 211 + 109) * 0.17f;
        } else {
            startT = 0.64f + hash01(seed + segment * 233 + 127) * 0.13f;
            endT = 0.88f + hash01(seed + segment * 251 + 151) * 0.16f;
        }
        endT = Math.min(1.08f, Math.max(startT + 0.18f, endT));

        float lateralScale = centerCore ? 0.10f : band == 0 ? 0.13f : band == 1 ? 0.16f : 0.14f;
        float startOffset = (hash01(seed + segment * 283 + 181) - 0.5f) * minDimension * lateralScale;
        float endOffset = startOffset + (hash01(seed + segment * 307 + 199) - 0.5f) * minDimension * (lateralScale + 0.045f);
        float startX = originX + directionX * edgeDistance * startT + normalX * startOffset;
        float startY = originY + directionY * edgeDistance * startT + directionX * startOffset;
        float endX = originX + directionX * edgeDistance * endT + normalX * endOffset;
        float endY = originY + directionY * edgeDistance * endT + directionX * endOffset;

        addScreenGlassCrackSegment(crackLines, startX, startY, endX, endY, width, height);
    }

    private static boolean isScreenCrackCenterCoreSegment(int segment, int targetCount) {
        int coreCount = Math.min(SCREEN_CRACK_CENTER_CORE_LINES, targetCount);
        for (int core = 0; core < coreCount; core++) {
            int coreSegment = Math.min(targetCount - 1, Math.round(core * targetCount / (float) coreCount));
            if (segment == coreSegment) {
                return true;
            }
        }
        return false;
    }

    private static void addScreenGlassCrackSegment(List<ScreenEffects.ScreenBreakLine> crackLines, float x0, float y0, float x1, float y1, int width, int height) {
        ScreenEffects.ScreenBreakLine line = new ScreenEffects.ScreenBreakLine(
                Mth.clamp(x0 / Math.max(1, width), -0.12f, 1.12f),
                Mth.clamp(y0 / Math.max(1, height), -0.12f, 1.12f),
                Mth.clamp(x1 / Math.max(1, width), -0.12f, 1.12f),
                Mth.clamp(y1 / Math.max(1, height), -0.12f, 1.12f)
        );
        if (isUsableScreenLine(line, width, height)) {
            crackLines.add(line);
        }
    }

    private static float distanceToScreenEdge(float centerX, float centerY, float directionX, float directionY, int width, int height) {
        float edgeDistance = Float.MAX_VALUE;
        if (Math.abs(directionX) > 0.0001f) {
            float targetX = directionX > 0.0f ? width : 0.0f;
            float distance = (targetX - centerX) / directionX;
            if (distance > 0.0f) {
                edgeDistance = Math.min(edgeDistance, distance);
            }
        }
        if (Math.abs(directionY) > 0.0001f) {
            float targetY = directionY > 0.0f ? height : 0.0f;
            float distance = (targetY - centerY) / directionY;
            if (distance > 0.0f) {
                edgeDistance = Math.min(edgeDistance, distance);
            }
        }
        return edgeDistance == Float.MAX_VALUE ? 0.0f : edgeDistance;
    }

    private static float dominantReferenceAngle(List<ScreenEffects.ScreenBreakLine> referenceLines, int width, int height) {
        if (referenceLines == null || referenceLines.isEmpty()) {
            return -0.38f;
        }

        ScreenEffects.ScreenBreakLine dominant = null;
        float bestScore = Float.MAX_VALUE;
        for (ScreenEffects.ScreenBreakLine line : referenceLines) {
            if (!isUsableScreenLine(line, width, height)) {
                continue;
            }
            float score = distanceFromScreenCenterToLine(line, width, height) + midpointDistanceToScreenCenter(line, width, height) * 0.16f;
            if (score < bestScore) {
                bestScore = score;
                dominant = line;
            }
        }
        if (dominant == null) {
            return -0.38f;
        }

        float dx = (dominant.x1() - dominant.x0()) * width;
        float dy = (dominant.y1() - dominant.y0()) * height;
        if (dx * dx + dy * dy <= 1.0f) {
            return -0.38f;
        }
        return (float) Math.atan2(dy, dx);
    }

    private static int mixHash(int hash, int value) {
        hash ^= value;
        hash *= 0x01000193;
        hash ^= hash >>> 13;
        return hash;
    }

    private static float hash01(int value) {
        int hash = mixHash(0x811C9DC5, value);
        hash ^= hash >>> 16;
        return (hash & 0x00FFFFFF) / 16777215.0f;
    }

    private static float screenLineNoise(ScreenEffects.ScreenBreakLine line) {
        int midpointX = Math.round((line.x0() + line.x1()) * 4096.0f);
        int midpointY = Math.round((line.y0() + line.y1()) * 4096.0f);
        int directionX = Math.round((line.x1() - line.x0()) * 4096.0f);
        int directionY = Math.round((line.y1() - line.y0()) * 4096.0f);
        int hash = mixHash(0x4F1BBCDC, midpointX * 73428767 ^ midpointY * 912931);
        hash = mixHash(hash, directionX * 1640531527 ^ directionY * 374761393);
        hash ^= hash >>> 16;
        return (hash & 0x00FFFFFF) / 16777215.0f;
    }

    private static List<DistortionRenderer.ScreenLine> createDirectionalUvOffsetLinesFromActiveGlassCracks(List<DistortionRenderer.ScreenLine> fallbackLines, float partialTick, int width, int height) {
        if (fallbackLines == null || fallbackLines.isEmpty() || width <= 0 || height <= 0) {
            return fallbackLines == null ? List.of() : fallbackLines;
        }

        List<ScreenEffects.ScreenBreakLine> activeCrackLines = ScreenEffects.activeGlassCrackPathLines(partialTick, width, height);
        if (activeCrackLines.isEmpty()) {
            return fallbackLines;
        }

        float lineWidth = fallbackLines.get(0).widthPixels();
        float strength = fallbackLines.get(0).strengthPixels();
        float alpha = 0.0f;
        for (DistortionRenderer.ScreenLine line : fallbackLines) {
            alpha = Math.max(alpha, line.alpha());
        }

        List<DistortionRenderer.ScreenLine> realCrackLines = createDirectionalUvOffsetLinesFromScreenBreakLines(activeCrackLines, width, height, lineWidth, strength, alpha, true);
        return realCrackLines.isEmpty() ? fallbackLines : realCrackLines;
    }

    private static List<DistortionRenderer.ScreenLine> createDirectionalUvOffsetLinesFromScreenBreakLines(List<ScreenEffects.ScreenBreakLine> screenLines, int width, int height, float lineWidth, float strength, float alpha) {
        return createDirectionalUvOffsetLinesFromScreenBreakLines(screenLines, width, height, lineWidth, strength, alpha, false);
    }

    private static List<DistortionRenderer.ScreenLine> createDirectionalUvOffsetLinesFromScreenBreakLines(List<ScreenEffects.ScreenBreakLine> screenLines, int width, int height, float lineWidth, float strength, float alpha, boolean preserveOrder) {
        if (screenLines == null || screenLines.isEmpty() || width <= 0 || height <= 0) {
            return List.of();
        }

        List<ScreenEffects.ScreenBreakLine> orderedLines = preserveOrder ? usableScreenBreakLines(screenLines, width, height) : orderScreenBreakLinesCenterOut(screenLines, width, height);
        if (orderedLines.isEmpty()) {
            return List.of();
        }

        int lineLimit = Math.min(orderedLines.size(), Math.max(1, SlashCofig.ScreenBreak.DISTORTION_LINE_COUNT));
        List<ScreenEffects.ScreenBreakLine> selectedLines = preserveOrder ? orderedLines.subList(0, lineLimit) : selectDistributedScreenBreakLines(orderedLines, width, height, lineLimit);
        List<DistortionRenderer.ScreenLine> uvOffsetLines = new ArrayList<>(lineLimit);
        for (int i = 0; i < selectedLines.size() && uvOffsetLines.size() < lineLimit; i++) {
            ScreenEffects.ScreenBreakLine line = selectedLines.get(i);
            if (!isUsableScreenLine(line, width, height)) {
                continue;
            }

            float x0 = line.x0();
            float y0 = line.y0();
            float x1 = line.x1();
            float y1 = line.y1();
            if ((uvOffsetLines.size() & 1) == 1) {
                float swapX = x0;
                float swapY = y0;
                x0 = x1;
                y0 = y1;
                x1 = swapX;
                y1 = swapY;
            }

            int rank = uvOffsetLines.size();
            float alphaScale = Mth.clamp(1.0f - rank * 0.055f, 0.62f, 1.0f);
            float directionFlip = (rank & 1) == 1 ? -1.0f : 1.0f;
            uvOffsetLines.add(new DistortionRenderer.ScreenLine(x0, y0, x1, y1, lineWidth, strength, alpha * alphaScale, directionFlip, true));
        }
        return uvOffsetLines;
    }

    private static List<ScreenEffects.ScreenBreakLine> selectDistributedScreenBreakLines(List<ScreenEffects.ScreenBreakLine> lines, int width, int height, int limit) {
        List<ScreenEffects.ScreenBreakLine> centerLines = new ArrayList<>();
        List<ScreenEffects.ScreenBreakLine> innerLines = new ArrayList<>();
        List<ScreenEffects.ScreenBreakLine> outerLines = new ArrayList<>();
        List<ScreenEffects.ScreenBreakLine> edgeLines = new ArrayList<>();

        for (ScreenEffects.ScreenBreakLine line : lines) {
            float radial = midpointRadial01(line, width, height);
            if (radial < 0.30f) {
                centerLines.add(line);
            } else if (radial < 0.55f) {
                innerLines.add(line);
            } else if (radial < 0.78f) {
                outerLines.add(line);
            } else {
                edgeLines.add(line);
            }
        }

        sortRadialBucket(centerLines, width, height, 0.035f);
        sortRadialBucket(innerLines, width, height, 0.060f);
        sortRadialBucket(outerLines, width, height, 0.075f);
        sortRadialBucket(edgeLines, width, height, 0.090f);

        List<ScreenEffects.ScreenBreakLine> selected = new ArrayList<>(limit);
        int centerIndex = 0;
        int innerIndex = 0;
        int outerIndex = 0;
        int edgeIndex = 0;
        if (centerIndex < centerLines.size()) {
            selected.add(centerLines.get(centerIndex++));
        }
        while (selected.size() < limit) {
            boolean picked = false;
            if (innerIndex < innerLines.size()) {
                selected.add(innerLines.get(innerIndex++));
                picked = true;
            }
            if (outerIndex < outerLines.size() && selected.size() < limit) {
                selected.add(outerLines.get(outerIndex++));
                picked = true;
            }
            if (edgeIndex < edgeLines.size() && selected.size() < limit) {
                selected.add(edgeLines.get(edgeIndex++));
                picked = true;
            }
            if (centerIndex < centerLines.size() && selected.size() < limit) {
                selected.add(centerLines.get(centerIndex++));
                picked = true;
            }
            if (!picked) {
                break;
            }
        }
        return selected;
    }

    private static void sortRadialBucket(List<ScreenEffects.ScreenBreakLine> lines, int width, int height, float jitterScale) {
        lines.sort((left, right) -> Float.compare(radialOrderScore(left, width, height, jitterScale), radialOrderScore(right, width, height, jitterScale)));
    }

    private static float radialOrderScore(ScreenEffects.ScreenBreakLine line, int width, int height, float jitterScale) {
        return midpointRadial01(line, width, height) + (screenLineNoise(line) - 0.5f) * jitterScale;
    }

    private static List<ScreenEffects.ScreenBreakLine> usableScreenBreakLines(List<ScreenEffects.ScreenBreakLine> screenLines, int width, int height) {
        List<ScreenEffects.ScreenBreakLine> usableLines = new ArrayList<>();
        for (ScreenEffects.ScreenBreakLine line : screenLines) {
            if (isUsableScreenLine(line, width, height)) {
                usableLines.add(line);
            }
        }
        return usableLines;
    }

    private static List<ScreenEffects.ScreenBreakLine> orderScreenBreakLinesCenterOut(List<ScreenEffects.ScreenBreakLine> screenLines, int width, int height) {
        List<ScreenEffects.ScreenBreakLine> usableLines = usableScreenBreakLines(screenLines, width, height);
        if (usableLines.size() <= 1) {
            return usableLines;
        }

        float minDimension = Math.max(1.0f, Math.min(width, height));
        usableLines.sort((left, right) -> {
            int centerCompare = Float.compare(centerOutOrderScore(left, width, height, minDimension), centerOutOrderScore(right, width, height, minDimension));
            if (centerCompare != 0) {
                return centerCompare;
            }
            return Float.compare(distanceFromScreenCenterToLine(left, width, height), distanceFromScreenCenterToLine(right, width, height));
        });
        return usableLines;
    }

    private static float centerOutOrderScore(ScreenEffects.ScreenBreakLine line, int width, int height, float minDimension) {
        return midpointDistanceToScreenCenter(line, width, height) + (screenLineNoise(line) - 0.5f) * minDimension * 0.055f;
    }

    private static boolean isUsableScreenLine(ScreenEffects.ScreenBreakLine line, int width, int height) {
        if (line == null || hasNonFiniteCoordinates(line)) {
            return false;
        }

        float dx = (line.x1() - line.x0()) * width;
        float dy = (line.y1() - line.y0()) * height;
        float minLength = MIN_SCREEN_BREAK_LINE_LENGTH_PIXELS;
        return dx * dx + dy * dy >= minLength * minLength;
    }

    private static float distanceFromScreenCenterToLine(ScreenEffects.ScreenBreakLine line, int width, int height) {
        float x0 = line.x0() * width;
        float y0 = line.y0() * height;
        float x1 = line.x1() * width;
        float y1 = line.y1() * height;
        float dx = x1 - x0;
        float dy = y1 - y0;
        float length = Mth.sqrt(dx * dx + dy * dy);
        if (length <= 1.0f) {
            return midpointDistanceToScreenCenter(line, width, height);
        }
        float centerX = width * 0.5f;
        float centerY = height * 0.5f;
        return Math.abs(dx * (centerY - y0) - dy * (centerX - x0)) / length;
    }

    private static float midpointDistanceToScreenCenter(ScreenEffects.ScreenBreakLine line, int width, int height) {
        float midpointX = (line.x0() + line.x1()) * 0.5f * width;
        float midpointY = (line.y0() + line.y1()) * 0.5f * height;
        float centerX = width * 0.5f;
        float centerY = height * 0.5f;
        float dx = midpointX - centerX;
        float dy = midpointY - centerY;
        return Mth.sqrt(dx * dx + dy * dy);
    }

    private static float midpointRadial01(ScreenEffects.ScreenBreakLine line, int width, int height) {
        float maxDistance = Mth.sqrt(width * width + height * height) * 0.5f;
        return Mth.clamp(midpointDistanceToScreenCenter(line, width, height) / Math.max(1.0f, maxDistance), 0.0f, 1.0f);
    }

    private static boolean hasNonFiniteCoordinates(ScreenEffects.ScreenBreakLine line) {
        return !Float.isFinite(line.x0()) || !Float.isFinite(line.y0()) || !Float.isFinite(line.x1()) || !Float.isFinite(line.y1());
    }

    private static ScreenEffects.ScreenBreakLine projectLineToScreen(PoseStack poseStack, Vec3 camera, SlashLine line) {
        ScreenPoint first = null;
        ScreenPoint last = null;
        int samples = 9;
        for (int i = 0; i < samples; i++) {
            float t = i / (float) (samples - 1);
            ScreenPoint point = projectToScreen(poseStack, camera, line.start().lerp(line.end(), t));
            if (point == null) continue;
            if (first == null) first = point;
            last = point;
        }

        if (first == null) return null;
        return new ScreenEffects.ScreenBreakLine(first.x, first.y, last.x, last.y);
    }

    private static ScreenPoint projectToScreen(PoseStack poseStack, Vec3 camera, Vec3 worldPoint) {
        Matrix4f modelView = poseStack.last().pose();
        Matrix4f projection = RenderSystem.getProjectionMatrix();
        Vector4f view = new Vector4f((float) (worldPoint.x - camera.x), (float) (worldPoint.y - camera.y), (float) (worldPoint.z - camera.z), 1.0f);
        modelView.transform(view);

        Vector4f clip = new Vector4f(view);
        projection.transform(clip);
        if (clip.w <= 0.00001f) return null;

        float invW = 1.0f / clip.w;
        float ndcX = clip.x * invW;
        float ndcY = clip.y * invW;
        if (!Float.isFinite(ndcX) || !Float.isFinite(ndcY)) return null;

        return new ScreenPoint(ndcX * 0.5f + 0.5f, 1.0f - (ndcY * 0.5f + 0.5f));
    }

    private record SlashImpactPulse(SlashEffect owner, SlashLine line, long startGameTick) {}

    private record ScreenPoint(float x, float y) {
    }
}
