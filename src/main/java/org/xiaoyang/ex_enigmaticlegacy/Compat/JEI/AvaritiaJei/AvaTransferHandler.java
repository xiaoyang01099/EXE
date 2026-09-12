package org.xiaoyang.ex_enigmaticlegacy.Compat.JEI.AvaritiaJei;

import com.yuo.endless.compat.jei.ExtremeCraftRecipeCategory;
import com.yuo.endless.recipe.ExtremeCraftRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xiaoyang.ex_enigmaticlegacy.Container.ContainerExtremeAutoCrafter;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModMenus;

import java.util.Optional;

public class AvaTransferHandler implements IRecipeTransferHandler<ContainerExtremeAutoCrafter, ExtremeCraftRecipe> {
    @Override
    public Class<ContainerExtremeAutoCrafter> getContainerClass() {
        return ContainerExtremeAutoCrafter.class;
    }

    @Override
    public @NotNull Optional<MenuType<ContainerExtremeAutoCrafter>> getMenuType() {
        return Optional.of(ModMenus.EXTREME_AUTO_CRAFTER_MENU.get());
    }

    @Override
    public @NotNull RecipeType<ExtremeCraftRecipe> getRecipeType() {
        return ExtremeCraftRecipeCategory.RECIPE_TYPE;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(@NotNull ContainerExtremeAutoCrafter container, @NotNull ExtremeCraftRecipe recipe, @NotNull IRecipeSlotsView slots, @NotNull Player player, boolean maxTransfer, boolean doTransfer) {
        if (!doTransfer) return null;
        NonNullList<Ingredient> ingredients = NonNullList.withSize(81, Ingredient.EMPTY);
        var source = recipe.getIngredients();
        for (int row = 0; row < recipe.getHeight() && row < 9; row++) for (int col = 0; col < recipe.getWidth() && col < 9; col++) {
            int index = row * recipe.getWidth() + col;
            if (index < source.size()) ingredients.set(row * 9 + col, source.get(index));
        }
        for (int i = 0; i < 81; i++) {
            Ingredient ingredient = ingredients.get(i);
            container.setItem(81 + i, ingredient.isEmpty() ? ItemStack.EMPTY : ingredient.getItems()[0].copy());
        }
        container.setItem(162, recipe.getResultItem().copy());
        container.setRecipe(recipe);
        return null;
    }
}
