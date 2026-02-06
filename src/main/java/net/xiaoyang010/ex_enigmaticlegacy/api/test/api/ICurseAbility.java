package net.xiaoyang010.ex_enigmaticlegacy.api.test.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

/**
 * 诅咒能力系统的核心接口
 * 用于检测和管理玩家的诅咒状态
 */
public interface ICurseAbility {

    /**
     * 检查玩家是否被诅咒（是否佩戴诅咒戒指）
     */
    boolean isCursed(Player player);

    /**
     * 获取玩家当前的诅咒等级（诅咒数量）
     */
    int getCurseLevel(Player player);

    /**
     * 获取玩家缺失的基础诅咒数量
     * @return 缺失的诅咒数（7 - 当前诅咒数）
     */
    int getMissingCurses(Player player);

    /**
     * 检查玩家是否拥有完整的七重诅咒
     */
    boolean hasFullCurses(Player player);

    /**
     * 获取诅咒戒指物品（如果佩戴）
     */
    @Nullable
    ItemStack getCursedRing(Player player);
}