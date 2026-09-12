package org.xiaoyang.ex_enigmaticlegacy.api.test.curse;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.xiaoyang.ex_enigmaticlegacy.api.test.curse.api.ICurseAbility;

import javax.annotation.Nullable;

public class CurseAbilityHandler implements ICurseAbility {
    public static final CurseAbilityHandler INSTANCE = new CurseAbilityHandler();
    public static final int BASE_CURSE_COUNT = 7;

    public CurseAbilityHandler() {}

    @Override
    public boolean isCursed(Player player) {
        ItemStack ring = getCursedRing(player);
        return ring != null && !ring.isEmpty();
    }

    @Override
    public int getCurseLevel(Player player) {
        if (!isCursed(player)) {
            return 0;
        }

        return Math.max(BASE_CURSE_COUNT, SuperpositionHandler.getCurseAmount(player));
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
        return SuperpositionHandler.getCurioStack(player, EnigmaticItems.CURSED_RING);
    }

    public float getEfficiencyMultiplier(Player player) {
        if (!isCursed(player)) {
            return 0.0f;
        }
        int missingCurses = getMissingCurses(player);

        float penalty = missingCurses * 0.14f;

        return Math.max(0.0f, 1.0f - penalty);
    }

    public float getCurseStrength(Player player) {
        if (!isCursed(player)) {
            return 0.0f;
        }
        int curseLevel = getCurseLevel(player);
        return Math.min(1.0f, (float) curseLevel / BASE_CURSE_COUNT);
    }

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
