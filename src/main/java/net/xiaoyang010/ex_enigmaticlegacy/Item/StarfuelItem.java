package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeType;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

import javax.annotation.Nullable;

public class StarfuelItem extends Item {

    public StarfuelItem() {
        // 设置物品属性：物品类别、最大堆叠数量、抗火性和稀有度
        super(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM)
                .stacksTo(64)
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return 114748364;
    }
}
