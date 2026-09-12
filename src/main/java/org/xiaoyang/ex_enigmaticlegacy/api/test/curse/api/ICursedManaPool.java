package org.xiaoyang.ex_enigmaticlegacy.api.test.curse.api;

import net.minecraft.world.item.DyeColor;

public interface ICursedManaPool extends ICursedManaCollector {
    boolean isOutputtingCursedPower();
    DyeColor getCursedColor();
    void setCursedColor(DyeColor color);
}
