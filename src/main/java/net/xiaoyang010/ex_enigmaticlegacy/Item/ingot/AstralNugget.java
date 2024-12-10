package net.xiaoyang010.ex_enigmaticlegacy.Item.ingot;

import morph.avaritia.item.ImmortalItem;
import net.minecraft.world.item.Rarity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class AstralNugget extends ImmortalItem {
    public AstralNugget() {
        super(new Properties()
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_MINERAL)
                .stacksTo(64)
                .rarity(Rarity.EPIC)
        );
    }
}

