package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.util.Mth;

public final class PauseRenderState {
    private static boolean frozen;
    private static boolean partialTickCaptured;
    private static float frozenPartialTick;
    private static boolean gameTimeCaptured;
    private static long frozenGameTime;

    private PauseRenderState() {
    }

    public static boolean isFrozen() {
        Minecraft minecraft = Minecraft.getInstance();
        return update(minecraft.isPaused() || minecraft.screen instanceof PauseScreen);
    }

    public static float partialTick(float currentPartialTick) {
        return samplePartialTick(isFrozen(), currentPartialTick);
    }

    public static long gameTime(long currentGameTime) {
        return sampleGameTime(isFrozen(), currentGameTime);
    }

    public static void clear() {
        update(false);
    }

    static float samplePartialTick(boolean shouldFreeze, float currentPartialTick) {
        float safePartialTick = Mth.clamp(currentPartialTick, 0.0f, 1.0f);
        if (!update(shouldFreeze)) return safePartialTick;
        if (!partialTickCaptured) {
            frozenPartialTick = safePartialTick;
            partialTickCaptured = true;
        }
        return frozenPartialTick;
    }

    static long sampleGameTime(boolean shouldFreeze, long currentGameTime) {
        if (!update(shouldFreeze)) return currentGameTime;
        if (!gameTimeCaptured) {
            frozenGameTime = currentGameTime;
            gameTimeCaptured = true;
        }
        return frozenGameTime;
    }

    private static boolean update(boolean shouldFreeze) {
        if (frozen != shouldFreeze) {
            frozen = shouldFreeze;
            partialTickCaptured = false;
            gameTimeCaptured = false;
        }
        return frozen;
    }
}
