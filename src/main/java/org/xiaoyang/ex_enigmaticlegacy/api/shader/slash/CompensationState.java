package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

final class CompensationState {
    private final int pressureDelayTicks;
    private boolean desaturationFrameConsumed;
    private int pressureTicksRemaining = -1;

    CompensationState(int pressureDelayTicks) {
        this.pressureDelayTicks = Math.max(0, pressureDelayTicks);
    }

    boolean isAwaitingDesaturationFrame() {
        return !desaturationFrameConsumed;
    }

    boolean consumeDesaturationFrame() {
        if (desaturationFrameConsumed) return false;

        desaturationFrameConsumed = true;
        pressureTicksRemaining = pressureDelayTicks;
        return true;
    }

    boolean consumePressureStartTick() {
        if (!desaturationFrameConsumed || pressureTicksRemaining < 0) return false;

        if (pressureTicksRemaining > 0) {
            pressureTicksRemaining--;
        }
        if (pressureTicksRemaining > 0) return false;

        pressureTicksRemaining = -1;
        return true;
    }
}
