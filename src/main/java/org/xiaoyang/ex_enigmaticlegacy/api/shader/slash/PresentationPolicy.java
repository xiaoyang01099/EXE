package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import net.minecraft.util.Mth;

final class PresentationPolicy {
    private PresentationPolicy() {
    }

    static boolean isScreenCandidate(boolean hasPresentation, boolean finalBurstStarted, float screenAge) {
        return hasPresentation && finalBurstStarted && screenAge >= 0.0f && screenAge < screenVisualDuration();
    }

    static boolean prefersNewer(float candidateAge, float selectedAge) {
        return candidateAge <= selectedAge;
    }

    static int resumeAge(float screenAge) {
        int maximumAge = Math.max(0, SlashCofig.ScreenBreak.SHARD_MAX_LIFETIME_TICKS - 1);
        return Mth.clamp(Mth.floor(screenAge), 0, maximumAge);
    }

    static boolean holdsScreenFreeze(float renderAge, float lastRevealTick, float finalBurstTick) {
        if (renderAge < lastRevealTick) return false;
        if (renderAge < finalBurstTick) return true;
        return renderAge - finalBurstTick < screenFreezeReleaseAge();
    }

    static float screenFreezeReleaseAge() {
        return SlashCofig.ScreenBreak.SHARD_LAUNCH_START_TICKS
                + SlashCofig.ScreenBreak.SCREEN_FREEZE_RELEASE_AFTER_GLASS_BREAK_TICKS;
    }

    static float screenVisualDuration() {
        float visibleDuration = Math.max(
                SlashCofig.ScreenBreak.DURATION_TICKS,
                SlashCofig.ScreenBreak.SHARD_FADE_START_TICKS
                        + SlashCofig.ScreenBreak.SHARD_FADE_TICKS
        );
        return Math.min(SlashCofig.ScreenBreak.SHARD_MAX_LIFETIME_TICKS, visibleDuration);
    }
}
