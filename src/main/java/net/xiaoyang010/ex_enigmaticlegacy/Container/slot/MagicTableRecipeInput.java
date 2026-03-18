package net.xiaoyang010.ex_enigmaticlegacy.Container.slot;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class MagicTableRecipeInput extends SimpleContainer {

    public MagicTableRecipeInput(ItemStack input) {
        super(1);
        this.setItem(0, input.copy());
    }

    public ItemStack getInput() {
        return this.getItem(0);
    }
}