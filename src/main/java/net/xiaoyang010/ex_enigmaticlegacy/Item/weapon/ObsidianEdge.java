package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class ObsidianEdge extends SwordItem {
    public ObsidianEdge() {
        super(Tiers.NETHERITE, 10, -2.4F, new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR).rarity(ModRarities.MIRACLE));
    }
}
