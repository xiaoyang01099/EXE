package net.xiaoyang010.ex_enigmaticlegacy.Item.ingot;

import morph.avaritia.item.ImmortalItem;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class AstralInogt extends ImmortalItem {
    public AstralInogt() {
        super(new Properties()
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_MINERAL)
                .stacksTo(64)
                .rarity(ModRarities.MIRACLE)
        );
    }
}
