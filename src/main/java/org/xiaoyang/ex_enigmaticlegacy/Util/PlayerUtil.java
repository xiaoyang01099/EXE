package org.xiaoyang.ex_enigmaticlegacy.Util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

// PlayerUtil 提供玩家饱食度扣除工具方法。
public class PlayerUtil {
    // 按固定数值扣除玩家饱食度，并跳过创造和旁观模式。
    public static void deductFood(Entity entity, int amount) {
        if (!(entity instanceof Player player)) return;
        if (amount <= 0) return;
        if (player.isCreative() || player.isSpectator()) return;
        int newFood = Math.max(0, player.getFoodData().getFoodLevel() - amount);
        player.getFoodData().setFoodLevel(newFood);
    }

    // 按最大饱食度比例扣除，实际扣除逻辑统一复用 deductFood。
    public static void deductFoodByMaxRatio(Entity entity, float ratio) {
        if (!(entity instanceof Player)) return;
        float clampedRatio = Math.max(0.0F, Math.min(1.0F, ratio));
        deductFood(entity, Math.round(20.0F * clampedRatio));
    }
}
