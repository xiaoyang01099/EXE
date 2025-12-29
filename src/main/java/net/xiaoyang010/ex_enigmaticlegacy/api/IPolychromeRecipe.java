package net.xiaoyang010.ex_enigmaticlegacy.api;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;


import javax.annotation.Nonnull;

public interface IPolychromeRecipe extends Recipe<Container> {
    ResourceLocation POLY_ID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "polychrome");
    ResourceLocation TYPE_ID = POLY_ID;

    @Nonnull
    @Override
    default ItemStack getToastSymbol() {
        return Registry.ITEM.getOptional(POLY_ID).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    @Override
    default RecipeType<?> getType() {
        return Registry.RECIPE_TYPE.getOptional(TYPE_ID).get();
    }

    @Override
    default boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    default boolean isSpecial() {
        return true;
    }

    @Override
    ItemStack assemble(Container inventory);

    int getManaUsage();
}