package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import net.minecraft.util.Mth;

import java.util.UUID;

public final class PressureTimeline {
    public static final UUID MOVEMENT_SPEED_MODIFIER_ID = UUID.fromString("679e9e0d-94fb-421b-a3cc-8c7c277b3176");

    private PressureTimeline() {}

    public static int worldSlashFadeCompletionTick(int slashCount) {
        return Geometry.slashSequenceDurationTicks(slashCount)
                + Math.max(0, SlashCofig.WorldSlash.FINAL_BURST_DELAY_TICKS)
                + Math.max(0, SlashCofig.WorldSlash.FINAL_FADE_TICKS);
    }

    public static int screenBreakCompletionTick(int slashCount) {
        return Geometry.slashSequenceDurationTicks(slashCount)
                + Math.max(0, SlashCofig.WorldSlash.FINAL_BURST_DELAY_TICKS)
                + screenBreakCompletionDurationTicks();
    }

    public static int movementRecoveryStartTick(int slashCount) {
        return Geometry.slashSequenceDurationTicks(slashCount)
                + Math.max(0, SlashCofig.WorldSlash.FINAL_BURST_DELAY_TICKS)
                + Math.max(0, Math.round(SlashCofig.ScreenBreak.SHARD_LAUNCH_START_TICKS));
    }

    public static int pressureEndTick(int slashCount) {
        return movementRecoveryStartTick(slashCount)
                + Math.max(0, SlashCofig.WorldSlash.PlayerPressure.MOVEMENT_SPEED_RECOVERY_TICKS);
    }

    public static int presentationEndTick(int slashCount) {
        int slashSequenceEndTick = Geometry.slashSequenceDurationTicks(slashCount);
        int finalBurstTick = slashSequenceEndTick
                + Math.max(0, SlashCofig.WorldSlash.FINAL_BURST_DELAY_TICKS);
        return Math.max(
                finalBurstTick + Math.max(0, SlashCofig.WorldSlash.FINAL_FADE_TICKS),
                Math.max(
                        finalBurstTick + Math.max(0, SlashCofig.ScreenBreak.DURATION_TICKS),
                        screenBreakCompletionTick(slashCount)
                                + Math.max(0, SlashCofig.WorldSlash.PlayerPressure.VIGNETTE_FADE_OUT_TICKS)
                )
        );
    }

    public static float movementSpeedMultiplier(float age, int movementRecoveryStartTick) {
        float targetMultiplier = Mth.clamp(
                SlashCofig.WorldSlash.PlayerPressure.MOVEMENT_SPEED_MULTIPLIER,
                0.01f,
                1.0f
        );
        float fadeInTicks = Math.max(0.0f, SlashCofig.WorldSlash.PlayerPressure.MOVEMENT_SPEED_FADE_IN_TICKS);
        float attack = fadeInTicks <= 0.0f ? 1.0f : smooth(Mth.clamp(age / fadeInTicks, 0.0f, 1.0f));
        if (age <= movementRecoveryStartTick) {
            return Mth.lerp(attack, 1.0f, targetMultiplier);
        }

        float recoveryTicks = Math.max(0.0f, SlashCofig.WorldSlash.PlayerPressure.MOVEMENT_SPEED_RECOVERY_TICKS);
        if (recoveryTicks <= 0.0f) return 1.0f;
        float recovery = smooth(Mth.clamp((age - movementRecoveryStartTick) / recoveryTicks, 0.0f, 1.0f));
        return Mth.lerp(recovery, targetMultiplier, 1.0f);
    }

    private static int screenBreakCompletionDurationTicks() {
        float visualCompletion = Math.max(
                SlashCofig.ScreenBreak.DURATION_TICKS,
                SlashCofig.ScreenBreak.SHARD_FADE_START_TICKS
                        + SlashCofig.ScreenBreak.SHARD_FADE_TICKS
        );
        return Math.min(
                Math.max(1, SlashCofig.ScreenBreak.SHARD_MAX_LIFETIME_TICKS),
                Math.max(1, Mth.ceil(visualCompletion))
        );
    }

    private static float smooth(float value) {
        return value * value * (3.0f - 2.0f * value);
    }
}
