package net.xiaoyang010.ex_enigmaticlegacy.Recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.IShapedRecipe;

public class DeconRecipe {
    private static final Map<Item, List<ItemStack>> deconCache = new HashMap<>();
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
        if (recipe instanceof IShapedRecipe) {
            this.width = ((IShapedRecipe<?>)recipe).getRecipeWidth();
            this.height = ((IShapedRecipe<?>)recipe).getRecipeHeight();
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

            if (!stack.isEmpty()) {
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
                out[i] = in[i] != null && !in[i].isEmpty() ? in[i].copy() : ItemStack.EMPTY;
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
                if (dirty[i] != null && !dirty[i].isEmpty()) {
                    clean[i] = new ItemStack(dirty[i].getItem(), 1);
                    if (dirty[i].isDamageableItem()) {
                        int damage = dirty[i].getDamageValue();
                        if (damage == 32767) {
                            clean[i].setDamageValue(0);
                        } else {
                            clean[i].setDamageValue(damage);
                        }
                    }

                    Item remainingItem = dirty[i].getItem().getCraftingRemainingItem();
                    if (remainingItem != null && remainingItem != Items.AIR) {
                        clean[i] = ItemStack.EMPTY;
                    } else {
                        clean[i] = clean[i];
                    }
                }
            }

            return clean;
        }
    }

    private ItemStack[] convertOreDict(Object[] input) {
        try {
            List<ItemStack> list = new ArrayList<>();

            for(Object o : input) {
                if (o != null) {
                    if (o instanceof ItemStack) {
                        list.add((ItemStack)o);
                    } else if (o instanceof List) {
                        List<?> l = (List<?>)o;
                        if (!l.isEmpty()) {
                            Object o1 = l.get(0);
                            if (o1 instanceof ItemStack) {
                                ItemStack stack = (ItemStack)o1;
                                list.add(stack.copy());
                            }
                        }
                    }
                } else {
                    list.add(null);
                }
            }

            return list.toArray(new ItemStack[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void nullify() {
        this.ingredients = null;
        this.result = null;
        this.width = -1;
        this.height = -1;
        this.size = -1;
    }
}