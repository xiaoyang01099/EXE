package net.xiaoyang010.ex_enigmaticlegacy.api;

import net.minecraft.world.item.ItemStack;

public interface IWaveName {
    WaveStyle getWaveStyle(ItemStack stack);

    enum WaveStyle {
        HOLY, //圣洁
        FALLEN, //堕落
        MIRACLE, //奇迹
        GLITCH, //故障
        RAINBOW, //彩虹
        TEAR, //撕裂
        DISSOLVE, //溶解
        GLOW_STAR, //发光之星
        SHATTER //镜面破碎
        }
}