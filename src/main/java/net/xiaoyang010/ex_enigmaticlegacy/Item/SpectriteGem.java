package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;


public class SpectriteGem extends Item {
    public SpectriteGem() {
        super(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM).stacksTo(64).rarity(Rarity.EPIC));
    }

}