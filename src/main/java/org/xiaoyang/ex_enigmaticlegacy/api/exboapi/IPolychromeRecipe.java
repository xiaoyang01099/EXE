package org.xiaoyang.ex_enigmaticlegacy.api.exboapi;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

@SuppressWarnings("removal")
public interface IPolychromeRecipe extends Recipe<Container> {
    ResourceLocation POLY_ID = new ResourceLocation(Exe.MODID, "polychrome");
    ResourceLocation TYPE_ID = POLY_ID;

    default @NotNull ItemStack getToastSymbol() {
        return BuiltInRegistries.ITEM.getOptional(POLY_ID).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    default RecipeType<?> getType() {
        return BuiltInRegistries.RECIPE_TYPE.get(TYPE_ID);
    }

    @Override
    default boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    default boolean isSpecial() {
        return true;
    }

    int getManaUsage();
}