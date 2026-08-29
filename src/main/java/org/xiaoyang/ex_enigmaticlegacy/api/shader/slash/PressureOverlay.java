package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Exe.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PressureOverlay {
    private static final int CIRCLE_SEGMENTS = 64;
    private static final int MAX_ACTIVE_PRESSURES = 32;
    private static final FullscreenBufferBuilder VIGNETTE_BUFFER = new FullscreenBufferBuilder(8192);
    private static final Map<Long, PressureState> PRESSURES = new LinkedHashMap<>();

    private PressureOverlay() {
    }

    public static void begin(long roundId, double centerX, double centerZ, float radius, int durationTicks) {
        if (durationTicks <= 0 || ClientEffects.cannotReceiveEffects()) return;
        if (!PRESSURES.containsKey(roundId) && PRESSURES.size() >= MAX_ACTIVE_PRESSURES) {
            Iterator<Long> iterator = PRESSURES.keySet().iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
        PRESSURES.compute(roundId, (ignored, current) -> {
            if (current == null) return new PressureState(centerX, centerZ, radius, durationTicks);
            current.extend(durationTicks);
            return current;
        });
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !VisualToggle.areEffectsEnabled()) {
            clear();
            return;
        }
        if (PauseRenderState.isFrozen()) return;

        Iterator<PressureState> iterator = PRESSURES.values().iterator();
        while (iterator.hasNext()) {
            PressureState pressure = iterator.next();
            pressure.tick(minecraft.player.position());
            if (pressure.isFinished()) iterator.remove();
        }
    }

    static void render(GuiGraphics graphics, float partialTick, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            clear();
            return;
        }
        if (width <= 0 || height <= 0 || !VisualToggle.areEffectsEnabled()) return;
        partialTick = PauseRenderState.partialTick(partialTick);

        float strength = 0.0f;
        for (Map.Entry<Long, PressureState> entry : PRESSURES.entrySet()) {
            float temporalVisibility = entry.getValue().visibility(partialTick);
            float rangeVisibility = ClientEffects.hasRound(entry.getKey())
                    ? ClientEffects.rangeVisibility(entry.getKey(), partialTick)
                    : entry.getValue().rangeVisibility(partialTick);
            strength = Math.max(strength, temporalVisibility * rangeVisibility);
        }
        if (strength <= 0.001f) return;

        float innerRadius = Math.max(0.0f, SlashCofig.WorldSlash.PlayerPressure.VIGNETTE_INNER_RADIUS);
        float outerRadius = SlashCofig.WorldSlash.PlayerPressure.VIGNETTE_OUTER_RADIUS;
        float outerAlpha = Mth.clamp(SlashCofig.WorldSlash.PlayerPressure.VIGNETTE_MAX_ALPHA * strength, 0.0f, 1.0f);
        float centerX = width * 0.5f;
        float centerY = height * 0.5f;
        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;
        Matrix4f pose = graphics.pose().last().pose();

        DriverBlendState.enableDefault();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder builder = VIGNETTE_BUFFER.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int index = 0; index <= CIRCLE_SEGMENTS; index++) {
            float angle = (float) (Math.PI * 2.0 * index / CIRCLE_SEGMENTS);
            float cosine = Mth.cos(angle);
            float sine = Mth.sin(angle);
            builder.vertex(pose, centerX + cosine * halfWidth * outerRadius, centerY + sine * halfHeight * outerRadius, 0.0f).color(0.0f, 0.0f, 0.0f, outerAlpha).endVertex();
            builder.vertex(pose, centerX + cosine * halfWidth * innerRadius, centerY + sine * halfHeight * innerRadius, 0.0f).color(0.0f, 0.0f, 0.0f, 0.0f).endVertex();
        }
        BufferUploader.drawWithShader(builder.end());

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        // This overlay runs below every vanilla HUD layer.  Vanilla's vignette
        // assumes GUI blending remains enabled and otherwise overwrites the
        // entire scene with its mostly-black texture.
        DriverBlendState.enableDefault();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    static void clear() {
        PRESSURES.clear();
    }

    static boolean hasActive() {
        return !PRESSURES.isEmpty();
    }

    private static final class PressureState {
        private int ticksRemaining;
        private final double centerX;
        private final double centerZ;
        private final float radius;
        private int age;
        private float previousVisibility;
        private float visibility;
        private float previousRangeVisibility;
        private float rangeVisibility;

        private PressureState(double centerX, double centerZ, float radius, int durationTicks) {
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.radius = Math.max(0.0f, radius);
            ticksRemaining = Math.max(0, durationTicks);
        }

        private void extend(int durationTicks) {
            ticksRemaining = Math.max(ticksRemaining, Math.max(0, durationTicks));
        }

        private void tick(net.minecraft.world.phys.Vec3 viewerPosition) {
            previousVisibility = visibility;
            previousRangeVisibility = rangeVisibility;
            boolean pressured = ticksRemaining > 0;
            if (pressured) ticksRemaining--;
            visibility = PressureOverlayTimeline.advance(visibility, pressured);
            age++;
            float entrance = DomainTimeline.entrance(Math.max(0, age - 1), SlashCofig.WorldSlash.Domain.INTRO_TICKS);
            float currentRadius = DomainTimeline.radius(radius, entrance);
            double deltaX = viewerPosition.x - centerX;
            double deltaZ = viewerPosition.z - centerZ;
            rangeVisibility = RangeVisibility.atDistance(
                    (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ),
                    currentRadius,
                    SlashCofig.WorldSlash.Domain.VISIBILITY_EDGE_FADE_FRACTION
            );
        }

        private float visibility(float partialTick) {
            float interpolated = Mth.lerp(Mth.clamp(partialTick, 0.0f, 1.0f), previousVisibility, visibility);
            return PressureOverlayTimeline.easedVisibility(interpolated);
        }

        private boolean isFinished() {
            return ticksRemaining <= 0 && previousVisibility <= 0.001f && visibility <= 0.001f;
        }

        private float rangeVisibility(float partialTick) {
            return Mth.lerp(Mth.clamp(partialTick, 0.0f, 1.0f), previousRangeVisibility, rangeVisibility);
        }
    }
}
