package net.xiaoyang010.ex_enigmaticlegacy.api;

public interface INoEMCItem {
    /**
     * 标记接口，用于表示该物品不应该有EMC值
     * 实现此接口的物品将：
     * 1. 在EMC计算阶段被排除
     * 2. 无法被添加到玩家知识中
     * 3. 无法通过任何方式获得EMC值
     * 4. 不参与炼金术相关计算
     */
}