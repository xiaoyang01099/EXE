package net.xiaoyang010.ex_enigmaticlegacy.Compat.Patchouli;

import morph.avaritia.recipe.ExtremeShapedRecipe;
import morph.avaritia.recipe.ExtremeShapelessRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

public class ExtremeCraftingProcessor implements IComponentProcessor {

    private Recipe<?> recipe;
    private boolean shapeless = false;
    private int width = 9;
    private int height = 9;

    @Override
    public void setup(IVariableProvider variables) {
        String recipeId = variables.get("recipe").asString();

        RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
        ResourceLocation id = new ResourceLocation(recipeId);

        this.recipe = manager.byKey(id).orElse(null);

        if (recipe instanceof ExtremeShapelessRecipe) {
            shapeless = true;
        } else if (recipe instanceof ExtremeShapedRecipe) {
            ExtremeShapedRecipe shaped = (ExtremeShapedRecipe) recipe;
            this.width = shaped.getWidth();
            this.height = shaped.getHeight();
        }
    }

    @Override
    public IVariable process(String key) {
        if (recipe == null) {
            return null;
        }

        if (key.equals("output")) {
            return IVariable.from(recipe.getResultItem());
        }

        if (key.equals("recipe_type")) {
            return IVariable.wrap(shapeless ? "extreme_shapeless" : "extreme_shaped");
        }

        if (key.equals("width")) {
            return IVariable.wrap(width);
        }

        if (key.equals("height")) {
            return IVariable.wrap(height);
        }


        if (key.startsWith("item")) {
            String[] parts = key.substring(4).split("_");
            if (parts.length == 2) {
                try {
                    int row = Integer.parseInt(parts[0]) - 1;
                    int col = Integer.parseInt(parts[1]) - 1;

                    if (row >= 0 && row < 9 && col >= 0 && col < 9) {
                        int index = row * 9 + col;
                        NonNullList<Ingredient> ingredients = recipe.getIngredients();

                        if (shapeless) {
                            if (index < ingredients.size()) {
                                Ingredient ingredient = ingredients.get(index);
                                ItemStack[] stacks = ingredient.getItems();
                                if (stacks.length > 0) {
                                    return IVariable.from(stacks);
                                }
                            }
                        } else if (recipe instanceof ExtremeShapedRecipe) {
                            ExtremeShapedRecipe shaped = (ExtremeShapedRecipe) recipe;

                            if (row < height && col < width) {
                                int recipeIndex = row * width + col;
                                if (recipeIndex < ingredients.size()) {
                                    Ingredient ingredient = ingredients.get(recipeIndex);
                                    ItemStack[] stacks = ingredient.getItems();
                                    if (stacks.length > 0) {
                                        return IVariable.from(stacks);
                                    }
                                }
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                }
            }

            return IVariable.from(ItemStack.EMPTY);
        }

        if (key.startsWith("input")) {
            try {
                int index = Integer.parseInt(key.substring(5)) - 1;
                NonNullList<Ingredient> ingredients = recipe.getIngredients();

                if (index >= 0 && index < ingredients.size()) {
                    Ingredient ingredient = ingredients.get(index);
                    ItemStack[] stacks = ingredient.getItems();

                    if (stacks.length > 0) {
                        return IVariable.from(stacks);
                    }
                }
            } catch (NumberFormatException e) {
            }

            return IVariable.from(ItemStack.EMPTY);
        }

        return null;
    }
}