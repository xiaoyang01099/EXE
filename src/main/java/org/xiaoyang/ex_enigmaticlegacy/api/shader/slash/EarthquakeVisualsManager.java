package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Exe.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EarthquakeVisualsManager {

    private static volatile boolean ENABLED = true; // test
    private static final int MAX_DIMENSIONAL_SLASH_IMPACTS = 16;
    private static final float MAX_AMPLITUDE_NORMAL = 12f;
    private static final float DECAY_NORMAL = 0.92f;
    private static final float MAX_AMPLITUDE_EXTREME = 30f;
    private static final float DECAY_EXTREME = 0.965f;
    private static final float BASE_FREQ_EXTREME = 22f;
    private static final float DEFAULT_YAW_WEIGHT = 1.00f;
    private static final float DEFAULT_PITCH_WEIGHT = 0.85f;
    private static final float DEFAULT_ROLL_WEIGHT = 0.45f;
    private static boolean EXTREME_ENABLED = true;
    private static float amplitude = 0f;
    private static float frequency = 14f;
    private static int ticksLeft = 0;
    private static int ageTicks = 0;
    private static long seed = 0L;
    private static float trauma = 0f;
    private static float maxCap = MAX_AMPLITUDE_NORMAL;
    private static float yawWeight = DEFAULT_YAW_WEIGHT;
    private static float pitchWeight = DEFAULT_PITCH_WEIGHT;
    private static float rollWeight = DEFAULT_ROLL_WEIGHT;
    private static float roughness = 0.0f;
    private static boolean dimensionalSlashCurve = false;
    private static int dimensionalSlashDuration = 1;
    private static float dimensionalSlashAmplitude = 0.0f;
    private static long dimensionalSlashOwner = Long.MIN_VALUE;
    private static final List<SlashImpactPulse> dimensionalSlashImpacts = new ArrayList<>();
    private static final Map<Long, Float> dimensionalSlashOwnerVisibility = new HashMap<>();

    private EarthquakeVisualsManager() {}

    public static void trigger(float amp, int durationTicks, float freq) {
        if (!ENABLED) return;
        maxCap = EXTREME_ENABLED ? MAX_AMPLITUDE_EXTREME : MAX_AMPLITUDE_NORMAL;
        amplitude = Math.min(amp + amplitude * 0.5f, maxCap);
        frequency = freq;
        ticksLeft = Math.max(ticksLeft, durationTicks);
        ageTicks = 0;
        seed = System.nanoTime();
        trauma = Math.min(1f, trauma + (amp / maxCap) * 0.6f);
        dimensionalSlashCurve = false;
        resetShakeShape();
    }

    public static void triggerExtreme(float amp, int durationTicks) {
        if (!EXTREME_ENABLED) {
            trigger(amp, durationTicks, 18f); return;
        }
        maxCap = MAX_AMPLITUDE_EXTREME;
        float boosted = amp * 1.4f + 2.0f;
        amplitude = Math.min(boosted + amplitude * 0.5f, maxCap);
        frequency = BASE_FREQ_EXTREME;
        ticksLeft = Math.max(ticksLeft, durationTicks);
        ageTicks = 0;
        seed = System.nanoTime();
        trauma = Math.min(1f, trauma + (boosted / maxCap) * 0.8f);
        dimensionalSlashCurve = false;
        resetShakeShape();
    }

    public static void triggerDimensionalSlash(long owner, float amp, int durationTicks, float freq, float maxAngleDegrees, float shakeRoughness,
                                               float yawAxisWeight, float pitchAxisWeight, float rollAxisWeight) {
        triggerDimensionalSlash(owner, amp, durationTicks, freq, maxAngleDegrees, shakeRoughness,
                yawAxisWeight, pitchAxisWeight, rollAxisWeight, 0.0f);
    }

    public static void triggerDimensionalSlash(long owner, float amp, int durationTicks, float freq, float maxAngleDegrees, float shakeRoughness,
                                               float yawAxisWeight, float pitchAxisWeight, float rollAxisWeight, float startAge) {
        if (!ENABLED) return;
        int safeDuration = Math.max(1, durationTicks);
        int safeStartAge = Math.max(0, Math.min(safeDuration, Math.round(startAge)));
        if (safeStartAge >= safeDuration) {
            cancelDimensionalSlashCurve(owner);
            return;
        }
        dimensionalSlashCurve = true;
        dimensionalSlashOwner = owner;
        dimensionalSlashDuration = safeDuration;
        maxCap = Math.max(0.001f, maxAngleDegrees);
        dimensionalSlashAmplitude = Math.min(Math.max(0.0f, amp), maxCap);
        amplitude = dimensionalSlashAmplitude;
        frequency = Math.max(0.0f, freq);
        ticksLeft = dimensionalSlashDuration - safeStartAge;
        ageTicks = safeStartAge;
        seed = System.nanoTime();
        trauma = 0.0f;
        yawWeight = Math.max(0.0f, yawAxisWeight);
        pitchWeight = Math.max(0.0f, pitchAxisWeight);
        rollWeight = Math.max(0.0f, rollAxisWeight);
        roughness = clamp01(shakeRoughness);
    }

    public static void triggerDimensionalSlashImpact(long owner, long startGameTick, long impactSeed, float durationTicks, float attackTicks,
                                                      float amp, float maxAngleDegrees, float yawAxisWeight,
                                                      float pitchAxisWeight, float rollAxisWeight) {
        if (!ENABLED || durationTicks <= 0.0f || amp <= 0.0f) return;

        while (dimensionalSlashImpacts.size() >= MAX_DIMENSIONAL_SLASH_IMPACTS) {
            dimensionalSlashImpacts.remove(0);
        }
        dimensionalSlashImpacts.add(new SlashImpactPulse(
                owner,
                startGameTick,
                impactSeed,
                durationTicks,
                Math.max(0.001f, attackTicks),
                Math.max(0.0f, amp),
                Math.max(0.001f, maxAngleDegrees),
                Math.max(0.0f, yawAxisWeight),
                Math.max(0.0f, pitchAxisWeight),
                Math.max(0.0f, rollAxisWeight)
        ));
    }

    public static void clearDimensionalSlashImpacts() {
        dimensionalSlashImpacts.clear();
    }

    public static void clearSlashEffects() {
        dimensionalSlashImpacts.clear();
        dimensionalSlashOwnerVisibility.clear();
        clearDimensionalSlashCurve();
    }

    public static void updateDimensionalSlashOwnerVisibility(long owner, float visibility) {
        dimensionalSlashOwnerVisibility.put(owner, clamp01(visibility));
    }

    public static void removeDimensionalSlashOwner(long owner) {
        dimensionalSlashOwnerVisibility.remove(owner);
        dimensionalSlashImpacts.removeIf(pulse -> pulse.owner() == owner);
        cancelDimensionalSlashCurve(owner);
    }

    public static void cancelDimensionalSlashCurve(long owner) {
        if (dimensionalSlashCurve && dimensionalSlashOwner == owner) clearDimensionalSlashCurve();
    }

    private static void clearDimensionalSlashCurve() {
        if (!dimensionalSlashCurve) {
            dimensionalSlashOwner = Long.MIN_VALUE;
            return;
        }
        dimensionalSlashCurve = false;
        dimensionalSlashDuration = 1;
        dimensionalSlashAmplitude = 0.0f;
        dimensionalSlashOwner = Long.MIN_VALUE;
        amplitude = 0.0f;
        ticksLeft = 0;
        ageTicks = 0;
        trauma = 0.0f;
        resetShakeShape();
    }

    public static void enableExtreme(boolean enable) {
        EXTREME_ENABLED = enable;
        if (!enable && amplitude > MAX_AMPLITUDE_NORMAL) {
            amplitude = MAX_AMPLITUDE_NORMAL;
        }
        maxCap = enable ? MAX_AMPLITUDE_EXTREME : MAX_AMPLITUDE_NORMAL;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (!ENABLED || mc.level == null) {
            clearDimensionalSlashImpacts();
        } else {
            if (PauseRenderState.isFrozen()) return;
            long gameTick = mc.level.getGameTime();
            dimensionalSlashImpacts.removeIf(pulse -> gameTick - pulse.startGameTick() >= pulse.durationTicks());
        }

        if (ticksLeft > 0) {
            ageTicks++;
            ticksLeft--;

            if (dimensionalSlashCurve) {
                if (ticksLeft == 0) {
                    amplitude = 0f;
                    trauma = 0f;
                    ageTicks = 0;
                    dimensionalSlashCurve = false;
                    dimensionalSlashAmplitude = 0.0f;
                    dimensionalSlashOwner = Long.MIN_VALUE;
                }
                return;
            }

            float decay = amplitude > 8f ? DECAY_EXTREME : DECAY_NORMAL;
            amplitude *= decay;

            trauma *= (decay + 0.02f);

            if (ticksLeft == 0) {
                amplitude = 0f;
                trauma = 0f;
                ageTicks = 0;
            }
        }
    }

    @SubscribeEvent
    public static void apply(ViewportEvent.ComputeCameraAngles e) {
        float partialTick = PauseRenderState.partialTick((float) e.getPartialTick());
        ShakeAngles baseShake = currentShakeAngles(partialTick);
        Minecraft mc = Minecraft.getInstance();
        ShakeAngles slashImpact = mc.level == null
                ? ShakeAngles.ZERO
                : currentDimensionalSlashImpactAngles(PauseRenderState.gameTime(mc.level.getGameTime()), partialTick);
        ShakeAngles shake = new ShakeAngles(
                baseShake.yaw() + slashImpact.yaw(),
                baseShake.pitch() + slashImpact.pitch(),
                baseShake.roll() + slashImpact.roll()
        );
        if (!shake.hasShake()) return;

        e.setYaw(e.getYaw() + shake.yaw());
        e.setPitch(e.getPitch() + shake.pitch());
        e.setRoll(e.getRoll() + shake.roll());
    }

    public static ShakeAngles currentShakeAngles(float partialTick) {
        if (!ENABLED || ticksLeft <= 0) return ShakeAngles.ZERO;
        if (dimensionalSlashCurve) return currentDimensionalSlashAngles(partialTick);

        float traumaFactor = clamp01(trauma * trauma);
        float ampCap = maxCap;
        float amp = Math.min(amplitude + traumaFactor * ampCap * 0.35f, ampCap);
        if (amp <= 0.001f) return ShakeAngles.ZERO;

        double t = ageTicks + partialTick;

        float freq = frequency + traumaFactor * 8.0f;
        double base = t * (0.18 + (freq * 0.012));

        double s0 = Math.sin(base + (seed & 0xFF) * 0.01);
        double s1 = Math.sin(base * 1.37 + ((seed >> 8) & 0xFF) * 0.01);
        double s2 = Math.sin(base * 0.73 + ((seed >> 16) & 0xFF) * 0.01);

        if (roughness > 0.001f) {
            double highBase = t * (0.52 + freq * 0.052);
            double j0 = Math.sin(highBase * 1.11 + ((seed >> 24) & 0xFF) * 0.017) * 0.68
                    + Math.sin(highBase * 2.43 + ((seed >> 32) & 0xFF) * 0.013) * 0.32;
            double j1 = Math.sin(highBase * 1.37 + ((seed >> 28) & 0xFF) * 0.019) * 0.64
                    + Math.sin(highBase * 2.79 + ((seed >> 36) & 0xFF) * 0.011) * 0.36;
            double j2 = Math.sin(highBase * 1.83 + ((seed >> 20) & 0xFF) * 0.021) * 0.70
                    + Math.sin(highBase * 3.17 + ((seed >> 40) & 0xFF) * 0.015) * 0.30;
            s0 = lerp(s0, j0, roughness);
            s1 = lerp(s1, j1, roughness);
            s2 = lerp(s2, j2, roughness);
        }

        return new ShakeAngles(
                (float) (s0 * amp * yawWeight),
                (float) (s1 * amp * pitchWeight),
                (float) (s2 * amp * rollWeight)
        );
    }

    private static ShakeAngles currentDimensionalSlashAngles(float partialTick) {
        float duration = Math.max(1.0f, (float) dimensionalSlashDuration);
        float progress = clamp01((ageTicks + partialTick) / duration);
        double envelope = 1.0 - smoothstep(0.0, 1.0, progress);
        double amp = dimensionalSlashAmplitude * envelope * ownerVisibility(dimensionalSlashOwner);
        amp = Math.min(amp, maxCap);
        if (amp <= 0.001) return ShakeAngles.ZERO;

        double t = ageTicks + partialTick;
        double freq = Math.max(1.0, frequency);
        double base = t * (0.62 + freq * 0.035);

        double p0 = (seed & 0xFF) * 0.017;
        double p1 = ((seed >> 8) & 0xFF) * 0.013;
        double p2 = ((seed >> 16) & 0xFF) * 0.011;

        double low0 = Math.sin(base * 0.63 + p0);
        double low1 = Math.sin(base * 0.71 + p1);
        double low2 = Math.sin(base * 0.57 + p2);

        double high0 = Math.sin(base + p0) * 0.56 + Math.sin(base * 2.17 + p1) * 0.30 + Math.sin(base * 3.73 + p2) * 0.14;
        double high1 = Math.sin(base * 1.19 + p1) * 0.52 + Math.sin(base * 2.53 + p2) * 0.32 + Math.sin(base * 4.11 + p0) * 0.16;
        double high2 = Math.sin(base * 1.41 + p2) * 0.58 + Math.sin(base * 2.89 + p0) * 0.27 + Math.sin(base * 3.37 + p1) * 0.15;

        double s0 = lerp(low0, high0, roughness);
        double s1 = lerp(low1, high1, roughness);
        double s2 = lerp(low2, high2, roughness);

        return new ShakeAngles((float) (s0 * amp * yawWeight), (float) (s1 * amp * pitchWeight), (float) (s2 * amp * rollWeight));
    }

    private static ShakeAngles currentDimensionalSlashImpactAngles(long gameTick, float partialTick) {
        if (!ENABLED || dimensionalSlashImpacts.isEmpty()) return ShakeAngles.ZERO;

        float yaw = 0.0f;
        float pitch = 0.0f;
        float roll = 0.0f;
        float angleCap = 0.0f;
        for (SlashImpactPulse pulse : dimensionalSlashImpacts) {
            float visibility = ownerVisibility(pulse.owner());
            if (visibility <= 0.001f) continue;
            float age = Math.max(0.0f, gameTick - pulse.startGameTick() + partialTick);
            float envelope = ImpactTimeline.envelope(age, pulse.durationTicks(), pulse.attackTicks());
            if (envelope <= 0.001f) continue;

            float impulse = pulse.amplitudeDegrees()
                    * envelope
                    * visibility
                    * ImpactTimeline.kickAndReturn(age, pulse.durationTicks());
            float yawSign = (pulse.seed() & 1L) == 0L ? 1.0f : -1.0f;
            float pitchSign = (pulse.seed() & 2L) == 0L ? 1.0f : -1.0f;
            float rollSign = (pulse.seed() & 4L) == 0L ? 1.0f : -1.0f;
            yaw += impulse * pulse.yawWeight() * yawSign;
            pitch += impulse * pulse.pitchWeight() * pitchSign;
            roll += impulse * pulse.rollWeight() * rollSign;
            angleCap = Math.max(angleCap, pulse.maxAngleDegrees());
        }

        if (angleCap <= 0.0f) return ShakeAngles.ZERO;
        return new ShakeAngles(
                clamp(yaw, -angleCap, angleCap),
                clamp(pitch, -angleCap, angleCap),
                clamp(roll, -angleCap, angleCap)
        );
    }

    private static void resetShakeShape() {
        yawWeight = DEFAULT_YAW_WEIGHT;
        pitchWeight = DEFAULT_PITCH_WEIGHT;
        rollWeight = DEFAULT_ROLL_WEIGHT;
        roughness = 0.0f;
    }

    private static float ownerVisibility(long owner) {
        return dimensionalSlashOwnerVisibility.getOrDefault(owner, 1.0f);
    }

    private static float clamp01(float x) {
        return x < 0f ? 0f : (Math.min(x, 1f));
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static double lerp(double from, double to, float amount) {
        return from + (to - from) * amount;
    }

    private static double smoothstep(double edge0, double edge1, double x) {
        if (edge0 == edge1) return x < edge0 ? 0.0 : 1.0;
        double t = Math.max(0.0, Math.min(1.0, (x - edge0) / (edge1 - edge0)));
        return t * t * (3.0 - 2.0 * t);
    }

    // test
    public static void setEnabled(boolean enabled) {
        ENABLED = enabled;
        if (!enabled) clearDimensionalSlashImpacts();
    }

    // test
    public static boolean isEnabled() {
        return ENABLED;
    }

    public record ShakeAngles(float yaw, float pitch, float roll) {
        private static final ShakeAngles ZERO = new ShakeAngles(0.0f, 0.0f, 0.0f);

        public boolean hasShake() {
            return Math.abs(yaw) > 0.0001f || Math.abs(pitch) > 0.0001f || Math.abs(roll) > 0.0001f;
        }
    }

    private record SlashImpactPulse(long owner, long startGameTick, long seed, float durationTicks, float attackTicks,
                                               float amplitudeDegrees, float maxAngleDegrees, float yawWeight,
                                               float pitchWeight, float rollWeight) {}
}
