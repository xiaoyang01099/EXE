package net.xiaoyang010.ex_enigmaticlegacy.Item.ingot;

import morph.avaritia.item.ImmortalItem;
import net.minecraft.world.item.Rarity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class AstralPile extends ImmortalItem {
    public AstralPile() {
        super(new Properties()
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_MINERAL)
                .stacksTo(64)
                .rarity(Rarity.EPIC)
        );
    }
}
