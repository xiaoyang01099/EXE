package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

public final class ImpactTimeline {
    private ImpactTimeline() {}

    public static float envelope(float ageTicks, float durationTicks, float attackTicks) {
        float duration = Math.max(0.001f, durationTicks);
        if (ageTicks < 0.0f || ageTicks >= duration) return 0.0f;

        float attack = Math.min(Math.max(0.001f, attackTicks), duration);
        float attackProgress = clamp01(ageTicks / attack);
        float releaseProgress = clamp01((ageTicks - attack) / Math.max(0.001f, duration - attack));
        return smooth(attackProgress) * (1.0f - smooth(releaseProgress));
    }

    public static float combineSaturated(float currentStrength, float addedStrength) {
        float current = clamp01(currentStrength);
        float added = clamp01(addedStrength);
        return 1.0f - (1.0f - current) * (1.0f - added);
    }

    public static float kickAndReturn(float ageTicks, float durationTicks) {
        float progress = clamp01(ageTicks / Math.max(0.001f, durationTicks));
        float wave = (float) Math.cos(progress * Math.PI / 0.74f);
        return wave < 0.0f ? wave * 0.35f : wave;
    }

    public static ImpactDirection perpendicularUvDirection(float screenDirectionX, float screenDirectionY) {
        float length = (float) Math.sqrt(screenDirectionX * screenDirectionX + screenDirectionY * screenDirectionY);
        if (length <= 0.001f) return new ImpactDirection(1.0f, 0.0f);

        float directionX = screenDirectionY / length;
        float directionY = screenDirectionX / length;
        if (directionX < 0.0f || (Math.abs(directionX) <= 0.0001f && directionY < 0.0f)) {
            directionX = -directionX;
            directionY = -directionY;
        }
        return new ImpactDirection(directionX, directionY);
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static float smooth(float value) {
        float clamped = clamp01(value);
        return clamped * clamped * (3.0f - 2.0f * clamped);
    }

    public record ImpactDirection(float x, float y) {}
}
