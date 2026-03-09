package net.xiaoyang010.ex_enigmaticlegacy.Recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import morph.avaritia.api.ExtremeCraftingRecipe;
import morph.avaritia.recipe.ExtremeShapedRecipe;
import morph.avaritia.recipe.ExtremeShapelessRecipe;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

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

    public static ExtremeDeconRecipe fromExtremeRecipe(ExtremeCraftingRecipe recipe) {
        try {
            ResourceLocation recipeId = recipe.getId();
            String group = recipe.getGroup();
            ItemStack result = recipe.getResultItem().copy();

            ItemStack[] ingredients = new ItemStack[81];
            int width = 0;
            int height = 0;
            boolean shapeless = false;

            if (recipe instanceof ExtremeShapelessRecipe shapelessRecipe){
                NonNullList<Ingredient> list = shapelessRecipe.getIngredients();
                shapeless = true;
                width = 9;
                height = (int) Math.ceil(list.size() / 9.0f);
                setIngredients(ingredients, list);
            }else if (recipe instanceof ExtremeShapedRecipe shapedRecipe){
                width = shapedRecipe.getWidth();
                height = shapedRecipe.getHeight();
                NonNullList<Ingredient> list = shapedRecipe.getIngredients();
                if (width < 9){
                    NonNullList<Ingredient> nullList = NonNullList.withSize(81, Ingredient.EMPTY);
                    int size = list.size();
                    for (int h = 0; h < 9; h++){
                        for (int w = 0; w < 9; w++){
                            int slot = h * 9 + w;
                            int recipeSlot = h * width + w;
                            if (w >= width){
                                nullList.set(slot, Ingredient.EMPTY);
                            }else {
                                if (recipeSlot > size - 1) nullList.set(slot, Ingredient.EMPTY);
                                else nullList.set(slot, list.get(recipeSlot));
                            }
                        }
                    }

                    setIngredients(ingredients, nullList);
                }else {
                    setIngredients(ingredients, list);
                }
            }


            return new ExtremeDeconRecipe(recipeId, group, result, ingredients, width, height, shapeless);

        } catch (Exception e) {
            ExEnigmaticlegacyMod.LOGGER.error("Failed to convert extreme recipe to decon recipe: {}",
                    recipe.getId(), e);
            return null;
        }
    }

    private static void setIngredients(ItemStack[] ingredients, NonNullList<Ingredient> list){
        int slot = 0;
        for (Ingredient ingredient : list) {
            if (!ingredient.isEmpty() && slot < list.size()) {
                ingredients[slot] = ingredient.getItems()[0];
            }
            slot++;
        }
    }

    private static void processIngredient(Ingredient ingredient, ItemStack[] ingredients, int index) {
        if (ingredient == null || ingredient.isEmpty() || index >= ingredients.length) {
            return;
        }

        ItemStack[] possibleItems = ingredient.getItems();
        if (possibleItems.length > 0) {
            ItemStack item = possibleItems[0].copy();
            item.setCount(1);

            if (item.getItem().getCraftingRemainingItem() == null) {
                ingredients[index] = normalizeItemStack(item);
            } else {
                ingredients[index] = ItemStack.EMPTY;
            }
        }
    }

    private static ItemStack normalizeItemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack normalized = new ItemStack(stack.getItem(), 1);

        if (stack.isDamageableItem()) {
            int damage = stack.getDamageValue();
            normalized.setDamageValue(damage == 32767 ? 0 : damage);
        }

        if (stack.hasTag()) {
            normalized.setTag(stack.getTag().copy());
        }

        return normalized;
    }

    public ResourceLocation getRecipeId() {
        return recipeId;
    }

    public String getGroup() {
        return group;
    }

    public ItemStack getResult() {
        return result.copy();
    }

    public ItemStack[] getIngredients() {
        ItemStack[] copy = new ItemStack[81];
        for (int i = 0; i < 81; i++) {
            copy[i] = ingredients[i] != null ? ingredients[i].copy() : ItemStack.EMPTY;
        }
        return copy;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isShapeless() {
        return shapeless;
    }

    public ItemStack getIngredientAt(int x, int y) {
        if (x < 0 || x >= 9 || y < 0 || y >= 9) {
            return ItemStack.EMPTY;
        }
        int index = y * 9 + x;
        return ingredients[index] != null ? ingredients[index].copy() : ItemStack.EMPTY;
    }

    public NonNullList<ItemStack> getActualIngredients() {
        NonNullList<ItemStack> actualIngredients = NonNullList.create();
        for (ItemStack stack : ingredients) {
            if (stack != null && !stack.isEmpty()) {
                actualIngredients.add(stack.copy());
            }
        }
        return actualIngredients;
    }

    public int getNonEmptyIngredientCount() {
        return (int) Arrays.stream(ingredients)
                .filter(stack -> stack != null && !stack.isEmpty())
                .count();
    }

    public boolean isValid() {
        if (result.isEmpty()) {
            return false;
        }

        if (getNonEmptyIngredientCount() == 0) {
            return false;
        }

        if (width < 1 || width > 9 || height < 1 || height > 9) {
            return false;
        }

        for (ItemStack ingredient : ingredients) {
            if (ingredient != null && !ingredient.isEmpty()) {
                if (ItemStack.isSameItemSameTags(ingredient, result)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean matchesResult(ItemStack stack) {
        if (stack.isEmpty() || result.isEmpty()) {
            return false;
        }

        if (!stack.is(result.getItem())) {
            return false;
        }

        if (stack.getCount() < result.getCount()) {
            return false;
        }

        if (result.isDamageableItem() && stack.getDamageValue() != result.getDamageValue()) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return String.format("ExtremeDeconRecipe{id=%s, result=%s, ingredients=%d/%d, size=%dx%d, shapeless=%s}",
                recipeId,
                result.getHoverName().getString(),
                getNonEmptyIngredientCount(),
                81,
                width,
                height,
                shapeless);
    }
}