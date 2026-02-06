package net.xiaoyang010.ex_enigmaticlegacy.api.test.api;

import net.minecraft.world.item.DyeColor;

/**
 * 诅咒魔力池接口
 */
public interface ICursedManaPool extends ICursedManaCollector {

    /**
     * 是否正在输出魔力
     */
    boolean isOutputtingCursedPower();

    /**
     * 获取颜色
     */
    DyeColor getCursedColor();

    /**
     * 设置颜色
     */
    void setCursedColor(DyeColor color);
}
