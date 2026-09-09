package org.xiaoyang.ex_enigmaticlegacy.api.shader.bladeflash;

import net.minecraftforge.eventbus.api.Event;

public final class MaskRenderEvent extends Event {
    private final MaskCanvas canvas;
    private final double timeSeconds;

    MaskRenderEvent(MaskCanvas canvas, double timeSeconds) {
        this.canvas = canvas;
        this.timeSeconds = timeSeconds;
    }

    public MaskCanvas canvas() { return canvas; }
    public double timeSeconds() { return timeSeconds; }
}
