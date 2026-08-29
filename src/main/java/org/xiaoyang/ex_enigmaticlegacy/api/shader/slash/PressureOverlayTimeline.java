package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import net.minecraft.util.Mth;

final class PressureOverlayTimeline {
    private PressureOverlayTimeline() {
    }

    static float advance(float visibility, boolean pressured) {
        int transitionTicks = pressured ? SlashCofig.WorldSlash.PlayerPressure.VIGNETTE_FADE_IN_TICKS : SlashCofig.WorldSlash.PlayerPressure.VIGNETTE_FADE_OUT_TICKS;

        float step = 1.0f / transitionTicks;
        return Mth.clamp(visibility + (pressured ? step : -step), 0.0f, 1.0f);
    }

    static float easedVisibility(float visibility) {
        float clamped = Mth.clamp(visibility, 0.0f, 1.0f);
        return clamped * clamped * (3.0f - 2.0f * clamped);
    }
}
