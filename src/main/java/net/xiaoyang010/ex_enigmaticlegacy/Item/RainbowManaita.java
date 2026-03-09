package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class RainbowManaita extends Item {

    public RainbowManaita() {
        super(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM).stacksTo(64).fireResistant().rarity(ModRarities.MIRACLE));
    }

    @Override
    public boolean hasCraftingRemainingItem() {
        return true;
    }


    @Override
    public ItemStack getContainerItem(ItemStack itemstack) {
        return new ItemStack(this);
    }
}
