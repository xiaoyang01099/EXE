package net.xiaoyang010.ex_enigmaticlegacy.Item;


import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class WhiteDust extends Item {
    public WhiteDust() {
        super(new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.COMMON)
                    .tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM));
        }
}

