package org.xiaoyang.ex_enigmaticlegacy.Item.res;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RainbowManaita extends Item {

    public RainbowManaita() {
        super(new Properties().stacksTo(64).fireResistant());
    }

    @Override
    public boolean hasCraftingRemainingItem() {
        return true;
    }

}
