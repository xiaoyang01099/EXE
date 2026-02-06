package net.xiaoyang010.ex_enigmaticlegacy.api.test.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * 魔力转换器接口
 * 用于诅咒魔力和原版魔力之间的转换
 */
public interface IManaConverter {

    /**
     * 获取转换器所在世界
     */
    Level getConverterLevel();

    /**
     * 获取转换器位置
     */
    BlockPos getConverterPos();

    /**
     * 将原版魔力转换为诅咒魔力
     * @param normalMana 原版魔力量
     * @return 实际转换的魔力量
     */
    int convertNormalToCursed(int normalMana);

    /**
     * 将诅咒魔力转换为原版魔力
     * @param cursedMana 诅咒魔力量
     * @return 实际转换的魔力量
     */
    int convertCursedToNormal(int cursedMana);

    /**
     * 获取转换效率（0.0 - 1.0）
     * 例如：0.8 表示转换时损失20%
     */
    float getConversionEfficiency();

    /**
     * 是否正在转换
     */
    boolean isConverting();

    /**
     * 获取污染等级（0-100）
     */
    int getCorruptionLevel();

    /**
     * 增加污染等级
     */
    void addCorruption(int amount);

    /**
     * 减少污染等级
     */
    void reduceCorruption(int amount);
}
