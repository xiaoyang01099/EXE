package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class MercurialEye extends Item {
    public MercurialEye() {
        super(new Properties().stacksTo(1).rarity(ModRarities.MIRACLE).tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM));
    }
}
