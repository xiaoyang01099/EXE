package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class BladeFallenStar extends SwordItem {
    public BladeFallenStar() {
        super(Tiers.NETHERITE, 120, -2.4F, new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR).rarity(ModRarities.MIRACLE));

    }
}
