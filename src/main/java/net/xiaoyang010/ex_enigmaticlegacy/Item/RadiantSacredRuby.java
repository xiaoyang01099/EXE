package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.world.item.Item;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class RadiantSacredRuby extends Item {
    public RadiantSacredRuby() {
        super(new Properties().stacksTo(1).rarity(ModRarities.MIRACLE).tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM));
    }
}
