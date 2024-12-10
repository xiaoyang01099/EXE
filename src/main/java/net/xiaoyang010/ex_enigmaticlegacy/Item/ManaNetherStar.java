package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class ManaNetherStar extends Item {
    public ManaNetherStar() {
        super(new Item.Properties()
                .stacksTo(64)
                .fireResistant()
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM)
                .rarity(Rarity.EPIC));
    }
}
