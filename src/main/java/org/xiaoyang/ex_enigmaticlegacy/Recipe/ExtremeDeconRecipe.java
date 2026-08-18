package org.xiaoyang.ex_enigmaticlegacy.Recipe;

import com.yuo.endless.Recipe.ExtremeCraftRecipe;
import com.yuo.endless.Recipe.ExtremeCraftShapeRecipe;
import com.yuo.endless.Recipe.IExtremeCraftRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.util.Arrays;

public class ExtremeDeconRecipe {
    private final ResourceLocation recipeId;
    private final ItemStack result;
    private final ItemStack[] ingredients;
    private final int width;
    private final int height;
    private final String group;
    private final boolean shapeless;

    public ExtremeDeconRecipe(ResourceLocation recipeId, String group, ItemStack result,
                              ItemStack[] ingredients, int width, int height, boolean shapeless) {
        this.recipeId = recipeId;
        this.group = group;
        this.result = result;
        this.ingredients = ingredients;
        this.width = width;
        this.height = height;
        this.shapeless = shapeless;

        if (ingredients.length != 81) {
            throw new IllegalArgumentException("Extreme decon recipe must have exactly 81 ingredient slots!");
        }
    }

    public static ExtremeDeconRecipe fromEndlessRecipe(IExtremeCraftRecipe recipe) {
        try {
            ResourceLocation recipeId = recipe.getId();
            String group = "";

            ItemStack result;
            if (recipe instanceof ExtremeCraftRecipe shapedRecipe) {
                result = shapedRecipe.getResultItem().copy();
            } else if (recipe instanceof ExtremeCraftShapeRecipe shapeRecipe) {
                result = shapeRecipe.getResultItem().copy();
            } else {
                Exe.LOGGER.warn("Unknown IExtremeCraftRecipe type: {}", recipe.getClass().getName());
                return null;
            }

            ItemStack[] ingredients = new ItemStack[81];
            Arrays.fill(ingredients, ItemStack.EMPTY);

            int width = 0;
            int height = 0;
            boolean shapeless = false;

            if (recipe instanceof ExtremeCraftShapeRecipe shapeRecipe) {
                shapeless = true;
                NonNullList<Ingredient> list = shapeRecipe.getIngredients();
                int size = list.size();
                width = Math.min(size, 9);
                height = (int) Math.ceil(size / 9.0);
                setIngredients(ingredients, list);

            } else if (recipe instanceof ExtremeCraftRecipe shapedRecipe) {
                width = shapedRecipe.getWidth();
                height = shapedRecipe.getHeight();
                NonNullList<Ingredient> list = shapedRecipe.getIngredients();

                if (width < 9) {
                    NonNullList<Ingredient> expandedList = NonNullList.withSize(81, Ingredient.EMPTY);
                    for (int h = 0; h < height; h++) {
                        for (int w = 0; w < width; w++) {
                            int recipeSlot = h * width + w;
                            int expandedSlot = h * 9 + w;
                            if (recipeSlot < list.size()) {
                                expandedList.set(expandedSlot, list.get(recipeSlot));
                            }
                        }
                    }
                    setIngredients(ingredients, expandedList);
                } else {
                    setIngredients(ingredients, list);
                }
            }

            return new ExtremeDeconRecipe(recipeId, group, result, ingredients, width, height, shapeless);

        } catch (Exception e) {
            Exe.LOGGER.error("Failed to convert endless recipe to decon recipe: {}", recipe.getId(), e);
            return null;
        }
    }

    private static void setIngredients(ItemStack[] ingredients, NonNullList<Ingredient> list) {
        for (int slot = 0; slot < list.size() && slot < 81; slot++) {
            Ingredient ingredient = list.get(slot);
            if (ingredient != null && !ingredient.isEmpty()) {
                ItemStack[] items = ingredient.getItems();
                if (items.length > 0) {
                    ingredients[slot] = items[0].copy();
                } else {
                    ingredients[slot] = ItemStack.EMPTY;
                }
            } else {
                ingredients[slot] = ItemStack.EMPTY;
            }
        }
    }

    public ResourceLocation getRecipeId() { return recipeId; }
    public String getGroup() { return group; }
    public ItemStack getResult() { return result.copy(); }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isShapeless() { return shapeless; }

    public ItemStack[] getIngredients() {
        ItemStack[] copy = new ItemStack[81];
        for (int i = 0; i < 81; i++) {
            copy[i] = ingredients[i] != null ? ingredients[i].copy() : ItemStack.EMPTY;
        }
        return copy;
    }

    public ItemStack getIngredientAt(int x, int y) {
        if (x < 0 || x >= 9 || y < 0 || y >= 9) return ItemStack.EMPTY;
        int index = y * 9 + x;
        return ingredients[index] != null ? ingredients[index].copy() : ItemStack.EMPTY;
    }

    public NonNullList<ItemStack> getActualIngredients() {
        NonNullList<ItemStack> actual = NonNullList.create();
        for (ItemStack stack : ingredients) {
            if (stack != null && !stack.isEmpty()) {
                actual.add(stack.copy());
            }
        }
        return actual;
    }

    public int getNonEmptyIngredientCount() {
        return (int) Arrays.stream(ingredients)
                .filter(s -> s != null && !s.isEmpty())
                .count();
    }

    public boolean isValid() {
        if (result.isEmpty()) return false;
        if (getNonEmptyIngredientCount() == 0) return false;
        if (width < 1 || width > 9 || height < 1 || height > 9) return false;
        for (ItemStack ingredient : ingredients) {
            if (ingredient != null && !ingredient.isEmpty()) {
                if (ItemStack.isSameItemSameTags(ingredient, result)) return false;
            }
        }
        return true;
    }

    public boolean matchesResult(ItemStack stack) {
        if (stack.isEmpty() || result.isEmpty()) return false;
        if (!stack.is(result.getItem())) return false;
        if (stack.getCount() < result.getCount()) return false;
        if (result.isDamageableItem() && stack.getDamageValue() != result.getDamageValue()) return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format(
                "ExtremeDeconRecipe{id=%s, result=%s, ingredients=%d/81, size=%dx%d, shapeless=%s}",
                recipeId, result.getHoverName().getString(),
                getNonEmptyIngredientCount(), width, height, shapeless
        );
    }
}