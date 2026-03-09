package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class LoppingPearl extends Item {
    public LoppingPearl() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM));
    }
}
