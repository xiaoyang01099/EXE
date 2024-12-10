package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class NebulousCore extends Item {
    public NebulousCore() {
        super(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM).stacksTo(1).fireResistant().rarity(Rarity.EPIC));
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
