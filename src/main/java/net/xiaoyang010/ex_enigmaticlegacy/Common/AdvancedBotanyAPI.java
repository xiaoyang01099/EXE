package net.xiaoyang010.ex_enigmaticlegacy.Common;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class AdvancedBotanyAPI {
    // Lists for Board of Fate functionality
    public static List<ItemStack> relicList = new ArrayList<>();
    public static List<ItemStack> diceList = new ArrayList<>();

    // Custom rarities (replacing EnumHelper usage)
    public static final Rarity RARITY_NEBULA = Rarity.create("NEBULA", ChatFormatting.LIGHT_PURPLE);
    public static final Rarity RARITY_WILD_HUNT = Rarity.create("WILD_HUNT", ChatFormatting.AQUA);

    /**
     * Register a relic for the Board of Fate
     * @param relic The relic item stack to register
     */
    public static void registerRelic(ItemStack relic) {
        if (!relic.isEmpty() && !relicList.contains(relic)) {
            relicList.add(relic);
        }
    }

    /**
     * Register a dice for the Board of Fate
     * @param dice The dice item stack to register
     */
    public static void registerDice(ItemStack dice) {
        if (!dice.isEmpty() && !diceList.contains(dice)) {
            diceList.add(dice);
        }
    }

    /**
     * Get all registered relics
     * @return List of relic item stacks
     */
    public static List<ItemStack> getRelicList() {
        return new ArrayList<>(relicList);
    }

    /**
     * Get all registered dice
     * @return List of dice item stacks
     */
    public static List<ItemStack> getDiceList() {
        return new ArrayList<>(diceList);
    }

    /**
     * Check if an item stack is a registered relic
     * @param stack The item stack to check
     * @return true if the item is a registered relic
     */
    public static boolean isRelic(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return relicList.stream().anyMatch(relic ->
                relic.getItem() == stack.getItem() &&
                        (relic.getDamageValue() == stack.getDamageValue() || relic.getDamageValue() == 32767));
    }

    /**
     * Check if an item stack is a registered dice
     * @param stack The item stack to check
     * @return true if the item is a registered dice
     */
    public static boolean isDice(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return diceList.stream().anyMatch(dice ->
                dice.getItem() == stack.getItem() &&
                        (dice.getDamageValue() == stack.getDamageValue() || dice.getDamageValue() == 32767));
    }

    /**
     * Clear all registered relics
     */
    public static void clearRelics() {
        relicList.clear();
    }

    /**
     * Clear all registered dice
     */
    public static void clearDice() {
        diceList.clear();
    }
}