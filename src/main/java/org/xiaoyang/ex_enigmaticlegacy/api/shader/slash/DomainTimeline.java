package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

final class DomainTimeline {

    private DomainTimeline() {
    }

    static float entrance(float age, float introTicks) {
        return DomainRange.entrance(age, introTicks);
    }

    static float radius(float targetRadius, float entrance) {
        return Math.max(0.0f, targetRadius) * clamp01(entrance);
    }

    static float fadeAlpha(float age, float endTick, float fadeTicks) {
        float duration = Math.max(0.001f, fadeTicks);
        float fadeStart = endTick - duration;
        if (age <= fadeStart) return 1.0f;
        return smooth(1.0f - clamp01((age - fadeStart) / duration));
    }

    private static float smooth(float value) {
        return value * value * (3.0f - 2.0f * value);
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
