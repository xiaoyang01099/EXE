package net.xiaoyang010.ex_enigmaticlegacy.api.test;

import com.integral.enigmaticlegacy.EnigmaticLegacy;
import com.integral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.api.ICurseAbility;

import javax.annotation.Nullable;

public class CurseAbilityHandler implements ICurseAbility {
    public static final CurseAbilityHandler INSTANCE = new CurseAbilityHandler();
    public static final int BASE_CURSE_COUNT = 7;

    public CurseAbilityHandler() {}

    @Override
    public boolean isCursed(Player player) {
        return SuperpositionHandler.isTheCursedOne(player);
    }

    @Override
    public int getCurseLevel(Player player) {
        if (!isCursed(player)) {
            return 0;
        }

        return SuperpositionHandler.getCurseAmount(player);
    }

    public int getRingCurseCount(Player player) {
        ItemStack ring = getCursedRing(player);
        if (ring == null || ring.isEmpty()) {
            return 0;
        }
        return BASE_CURSE_COUNT;
    }

    public int getOtherCurseCount(Player player) {
        if (!isCursed(player)) {
            return 0;
        }

        int totalCurses = getCurseLevel(player);
        int ringCurses = getRingCurseCount(player);

        return Math.max(0, totalCurses - ringCurses);
    }

    @Override
    public int getMissingCurses(Player player) {
        if (!isCursed(player)) {
            return BASE_CURSE_COUNT;
        }

        int ringCurses = getRingCurseCount(player);
        return Math.max(0, BASE_CURSE_COUNT - ringCurses);
    }

    @Override
    public boolean hasFullCurses(Player player) {
        return isCursed(player) && getRingCurseCount(player) >= BASE_CURSE_COUNT;
    }

    @Override
    @Nullable
    public ItemStack getCursedRing(Player player) {
        return SuperpositionHandler.getCurioStack(player, EnigmaticLegacy.cursedRing);
    }

    public float getEfficiencyMultiplier(Player player) {
        if (!isCursed(player)) {
            return 0.0f;
        }
        int missingCurses = getMissingCurses(player);

        float penalty = missingCurses * 0.14f;

        return Math.max(0.0f, 1.0f - penalty);
    }

    /**
     * 获取诅咒强度系数（0.0 - 1.0）
     * 可用于计算魔力生成/消耗的倍率
     */
    public float getCurseStrength(Player player) {
        if (!isCursed(player)) {
            return 0.0f;
        }
        int curseLevel = getCurseLevel(player);
        return Math.min(1.0f, (float) curseLevel / BASE_CURSE_COUNT);
    }

    /**
     * 根据诅咒等级计算魔力倍率
     * @param baseMana 基础魔力值
     * @param multiplierPerCurse 每个诅咒的倍率（例如0.1表示每个诅咒+10%）
     */
    public int calculateCursedMana(Player player, int baseMana, float multiplierPerCurse) {
        if (!isCursed(player)) {
            return 0;
        }
        int curseLevel = getCurseLevel(player);
        float cursedMana = baseMana * (1.0f + curseLevel * multiplierPerCurse);
        float efficiency = getEfficiencyMultiplier(player);

        return (int) (cursedMana * efficiency);
    }
}