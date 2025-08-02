package net.xiaoyang010.ex_enigmaticlegacy.Item.all;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


public class ModIngot extends Item {
    public ModIngot(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.getDamageValue() == 2;
    }
}
