package org.xiaoyang.ex_enigmaticlegacy.Item.res;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nullable;

public class StarfuelItem extends Item {

    public StarfuelItem() {
        super(new Properties().stacksTo(64).fireResistant().rarity(Rarity.EPIC));
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return 114748364;
    }
}
