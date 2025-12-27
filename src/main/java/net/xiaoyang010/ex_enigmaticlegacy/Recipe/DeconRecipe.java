package net.xiaoyang010.ex_enigmaticlegacy.Recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class DeconRecipe {
    private ItemStack result;
    private ItemStack[] ingredients;
    public boolean shapeless;
    public int size;
    public int width;
    public int height;

    public DeconRecipe(ItemStack result, ItemStack[] ingredients, int width, int height, boolean shapeless) {
        this.result = result;
        this.ingredients = ingredients;
        this.shapeless = shapeless;
        this.width = width;
        this.height = height;
        this.size = ingredients.length;
    }

    public DeconRecipe(Recipe<?> recipe) {
        this.size = recipe.getIngredients().size();
        this.width = this.size;
        this.height = 1;

        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
            this.width = shapedRecipe.getWidth();
            this.height = shapedRecipe.getHeight();
        }

        this.result = recipe.getResultItem();
        this.ingredients = this.normalizeItems(this.getFromList(recipe.getIngredients()));
        this.shapeless = recipe instanceof ShapelessRecipe;
    }

    private ItemStack[] getFromList(NonNullList<Ingredient> recipeItems) {
        ItemStack[] toReturn = new ItemStack[recipeItems.size()];

        for(int i = 0; i < recipeItems.size(); ++i) {
            ItemStack stack = ItemStack.EMPTY;

            try {
                ItemStack[] arr = recipeItems.get(i).getItems();
                if (arr.length != 0) {
                    stack = arr[0];
                }
            } catch (ArrayIndexOutOfBoundsException var6) {
            }

            if (stack != null) {
                toReturn[i] = stack.copy();
            }
        }

        return toReturn;
    }

    public ItemStack getResult() {
        return this.result.copy();
    }

    public ItemStack[] getIngredients() {
        return this.copyItemStackArr(this.ingredients);
    }

    private ItemStack[] copyItemStackArr(ItemStack[] in) {
        if (in != null) {
            ItemStack[] out = new ItemStack[in.length];

            for(int i = 0; i < in.length; ++i) {
                out[i] = in[i] != null ? in[i].copy() : ItemStack.EMPTY;
            }

            return out;
        } else {
            return null;
        }
    }

    private ItemStack[] normalizeItems(ItemStack[] dirty) {
        if (dirty == null) {
            return new ItemStack[9];
        } else {
            ItemStack[] clean = new ItemStack[dirty.length];

            for(int i = 0; i < clean.length; ++i) {
                if (dirty[i] != null && dirty[i] != ItemStack.EMPTY) {
                    clean[i] = new ItemStack(dirty[i].getItem(), 1);
                    clean[i].setDamageValue(dirty[i].getDamageValue());
                    if (clean[i].getDamageValue() == 32767) {
                        clean[i].setDamageValue(0);
                    }

                    if (dirty[i].getItem().getCraftingRemainingItem() != null) {
                        clean[i] = ItemStack.EMPTY;
                    }
                }
            }

            return clean;
        }
    }
}