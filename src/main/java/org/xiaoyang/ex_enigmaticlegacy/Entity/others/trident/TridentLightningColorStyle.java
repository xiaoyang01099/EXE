package org.xiaoyang.ex_enigmaticlegacy.Entity.others.trident;

import net.minecraft.util.RandomSource;

// TridentLightningColorStyle 集中管理天雷战戟路径闪电和地面雷圈的颜色样式。
public enum TridentLightningColorStyle {
    BLUE(1F, 1F, 1F, 0.3F, 0.6F, 1.0F,
            1F, 1F, 1.0F, 0.3F, 0.15F, 1.0F),
    RED(1.0F, 1F, 1F, 1.0F, 0.0F, 0.0F,
            1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F),
    PURPLE(1.0F, 1.0F, 1.0F, 0.62F, 0.1F, 0.7F,
            1.0F, 1.0F, 1.0F, 0.70F, 0.30F, 1.0F),
    GOLD(1.0F, 1.0F, 1.0F, 1.0F, 0.72F, 0.12F,
            1.0F, 1.0F, 1.0F, 1.0F, 0.58F, 0.05F);


    public final float pathCoreR; // 路径闪电主体红色。
    public final float pathCoreG; // 路径闪电主体绿色。
    public final float pathCoreB; // 路径闪电主体蓝色。
    public final float pathBloomR; // 路径闪电 Bloom 红色。
    public final float pathBloomG; // 路径闪电 Bloom 绿色。
    public final float pathBloomB; // 路径闪电 Bloom 蓝色。
    public final float ringCoreR; // 地面雷圈主体红色。
    public final float ringCoreG; // 地面雷圈主体绿色。
    public final float ringCoreB; // 地面雷圈主体蓝色。
    public final float ringBloomR; // 地面雷圈 Bloom 红色。
    public final float ringBloomG; // 地面雷圈 Bloom 绿色。
    public final float ringBloomB; // 地面雷圈 Bloom 蓝色。

    TridentLightningColorStyle(float pathCoreR, float pathCoreG, float pathCoreB,
                               float pathBloomR, float pathBloomG, float pathBloomB,
                               float ringCoreR, float ringCoreG, float ringCoreB,
                               float ringBloomR, float ringBloomG, float ringBloomB) {
        this.pathCoreR = pathCoreR;
        this.pathCoreG = pathCoreG;
        this.pathCoreB = pathCoreB;
        this.pathBloomR = pathBloomR;
        this.pathBloomG = pathBloomG;
        this.pathBloomB = pathBloomB;
        this.ringCoreR = ringCoreR;
        this.ringCoreG = ringCoreG;
        this.ringCoreB = ringCoreB;
        this.ringBloomR = ringBloomR;
        this.ringBloomG = ringBloomG;
        this.ringBloomB = ringBloomB;
    }

    // 根据强化状态随机选择颜色，普通模式保持稳定蓝色，强化模式加入紫色、红色和金色点缀。
    public static TridentLightningColorStyle pick(RandomSource random, boolean enhanced) {
        if (!enhanced) return BLUE;
        float roll = random.nextFloat();
        if (roll < 0.45F) return BLUE;
        if (roll < 0.70F) return PURPLE;
        if (roll < 0.85F) return RED;
        return GOLD;
    }
}
