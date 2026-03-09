package net.xiaoyang010.ex_enigmaticlegacy.Item.all;

import morph.avaritia.init.AvaritiaModContent;
import morph.avaritia.item.ImmortalItem;
import net.minecraft.world.item.Item;

import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;


public class ModSingularity extends ImmortalItem {
    public ModSingularity() {
        super((new Properties()).tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM).stacksTo(64).rarity(AvaritiaModContent.COSMIC_RARITY));
    }
}
