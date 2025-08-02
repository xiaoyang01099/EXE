package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.api.EXEAPI;

public class HornstoneSword extends SwordItem {
    public HornstoneSword() {
        super(EXEAPI.MIRACLE_ITEM_TIER, 99, -2.4F, new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR).rarity(ModRarities.MIRACLE));
    }
}
