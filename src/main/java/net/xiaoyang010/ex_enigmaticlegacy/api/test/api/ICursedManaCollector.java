package net.xiaoyang010.ex_enigmaticlegacy.api.test.api;

/**
 * 诅咒魔力收集器
 */
public interface ICursedManaCollector extends ICursedManaReceiver {

    /**
     * 客户端显示时调用
     */
    void onClientDisplayTick();

    /**
     * 获取魔力接收倍率
     */
    float getCursedManaYieldMultiplier(ICursedManaBurst burst);

    /**
     * 获取最大诅咒魔力容量
     */
    int getMaxCursedMana();
}
