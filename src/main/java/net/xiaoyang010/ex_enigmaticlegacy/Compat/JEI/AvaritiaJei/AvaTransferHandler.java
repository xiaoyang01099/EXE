package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI.AvaritiaJei;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.common.gui.recipes.IOnClickHandler;
import mezz.jei.common.transfer.RecipeTransferHandlerHelper;
import morph.avaritia.api.ExtremeCraftingRecipe;
import morph.avaritia.recipe.ExtremeShapedRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ContainerExtremeAutoCrafter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AvaTransferHandler implements IRecipeTransferHandler<ContainerExtremeAutoCrafter, ExtremeCraftingRecipe>, IOnClickHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private final IRecipeTransferHandlerHelper handlerHelper;
    private final IRecipeTransferInfo<ContainerExtremeAutoCrafter, ExtremeCraftingRecipe> transferInfo;
    private boolean isRecipe;

    public AvaTransferHandler() {
        this.transferInfo = new ExtremeAutoTransferInfo();
        this.handlerHelper = new RecipeTransferHandlerHelper();
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(ContainerExtremeAutoCrafter autoCrafter, ExtremeCraftingRecipe recipe, IRecipeSlotsView recipeSlotsView, Player player, boolean maxTransfer, boolean doTransfer) {
        if (this.transferInfo.canHandle(autoCrafter, recipe)) {
            List<Slot> craftingSlots = Collections.unmodifiableList(this.transferInfo.getRecipeSlots(autoCrafter, recipe));
            List<Slot> inventorySlots = Collections.unmodifiableList(this.transferInfo.getInventorySlots(autoCrafter, recipe));
            if (!recipe.getIngredients().isEmpty() && !recipe.getResultItem().isEmpty()){
                for (int i = 81; i <= 161; ++i) {
                    autoCrafter.setItem(i, ItemStack.EMPTY);
                }

                if (recipe instanceof ExtremeShapedRecipe shapedRecipe) {
                    int width = shapedRecipe.getWidth();
                    if (width < 9) {
                        NonNullList<Ingredient> ingredients = NonNullList.withSize(81, Ingredient.EMPTY);
                        NonNullList<Ingredient> list = shapedRecipe.getIngredients();
                        int size = list.size();
                        for (int h = 0; h < 9; h++) {
                            for (int w = 0; w < 9; w++) {
                                int slot = h * 9 + w;
                                int recipeSlot = h * width + w;
                                if (w >= width) {
                                    ingredients.set(slot, Ingredient.EMPTY);
                                } else {
                                    if (recipeSlot > size - 1) ingredients.set(slot, Ingredient.EMPTY);
                                    else ingredients.set(slot, list.get(recipeSlot));
                                }
                            }
                        }
                        setRecipe(shapedRecipe, autoCrafter, ingredients);
                    }else setRecipe(shapedRecipe, autoCrafter, shapedRecipe.getIngredients());
                }else setRecipe(recipe, autoCrafter, recipe.getIngredients());

                return null;
            }


            return this.handlerHelper.createInternalError();
        }

        return IRecipeTransferHandler.super.transferRecipe(autoCrafter, recipe, recipeSlotsView, player, maxTransfer, doTransfer);
    }

    /**
     * 设置无序配方
     */
    private static void setRecipe(ExtremeCraftingRecipe recipe, ContainerExtremeAutoCrafter autoCrafter, NonNullList<Ingredient> ingredients){
        int starSlot = 81;
        int endSlot = 161;
        int outSlot = 162;

        for (Ingredient ingredient : ingredients) {
            if (starSlot > endSlot) {
                System.out.println("Error Info:starSlot > endSlot");
                break;
            }
            if (ingredient.isEmpty()) autoCrafter.getSlot(starSlot).set(ItemStack.EMPTY);
            else {
                ItemStack stack = ingredient.getItems()[0];
                ItemStack copy = stack.copy();
                if (copy.getCount() > 1) copy.setCount(1);
                autoCrafter.setItem(starSlot, copy);
            }
            starSlot++;
        }

        ItemStack copy = recipe.getResultItem().copy();
        autoCrafter.setItem(outSlot, copy);
        autoCrafter.setRecipe(recipe);
    }

    @Override
    public @NotNull Class getContainerClass() {
        return ContainerExtremeAutoCrafter.class;
    }

    @Override
    public @NotNull Class getRecipeClass() {
        return ExtremeCraftingRecipe.class;
    }

    @Override
    public void onClick(double v, double v1) {
        isRecipe = true;
    }
}
