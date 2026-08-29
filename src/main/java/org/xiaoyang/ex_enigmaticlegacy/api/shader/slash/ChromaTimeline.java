package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import net.minecraft.util.Mth;

final class ChromaTimeline {
    private ChromaTimeline() {
    }

    static float envelope(float age, float startTick, float endTick) {
        float duration = endTick - startTick;
        if (duration <= 0.0001f || age <= startTick || age >= endTick) return 0.0f;

        float progress = Mth.clamp((age - startTick) / duration, 0.0f, 1.0f);
        float centerPeak = 1.0f - Math.abs(progress * 2.0f - 1.0f);
        return smooth(centerPeak);
    }

    private static float smooth(float value) {
        return value * value * (3.0f - 2.0f * value);
    }
}
