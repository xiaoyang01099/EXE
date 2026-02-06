package net.xiaoyang010.ex_enigmaticlegacy.api.test.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * 诅咒魔力接收器接口
 */
public interface ICursedManaReceiver {
    Level getCursedManaReceiverLevel();

    BlockPos getCursedManaReceiverPos();

    /**
     * 获取当前诅咒魔力量
     */
    int getCurrentCursedMana();

    /**
     * 是否已满
     */
    boolean isCursedManaFull();

    /**
     * 接收诅咒魔力
     */
    void receiveCursedMana(int mana);

    /**
     * 是否可以从诅咒魔力脉冲接收魔力
     */
    boolean canReceiveCursedManaFromBursts();
}
