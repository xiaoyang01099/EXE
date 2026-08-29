package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class VisualToggle {
    private static volatile boolean effectsEnabled = true;

    private VisualToggle() {}

    public static boolean areEffectsEnabled() {
        return effectsEnabled;
    }

    public static void setEffectsEnabled(boolean enabled) {
        effectsEnabled = enabled;

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            try {
                EarthquakeVisualsManager.setEnabled(enabled);
            } catch (Throwable ignored) {}
        });
    }
}
