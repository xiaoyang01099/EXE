package net.xiaoyang010.ex_enigmaticlegacy.Compat.Patchouli;

import morph.avaritia.recipe.ExtremeShapelessRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

public class ExtremeCraftingShapelessProcessor implements IComponentProcessor {
    private ExtremeShapelessRecipe recipe;

    @Override
    public void setup(IVariableProvider variables) {
        String recipeId = variables.get("recipe").asString();

        if (Minecraft.getInstance().level == null) {
            return;
        }

        RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
        Recipe<?> recipe = manager.byKey(new ResourceLocation(recipeId)).orElse(null);

        if (recipe instanceof ExtremeShapelessRecipe shapelessRecipe) {
            this.recipe = shapelessRecipe;
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

        if (key.equals("recipe_name")) {
            return IVariable.wrap(recipe.getResultItem().getHoverName().getString());
        }
        
        if (key.startsWith("input")) {
            try {
                int index = Integer.parseInt(key.substring(5));

                if (index < recipe.getIngredients().size()) {
                    Ingredient ingredient = recipe.getIngredients().get(index);
                    ItemStack[] stacks = ingredient.getItems();

                    if (stacks.length > 0) {
                        return IVariable.from(stacks[0]);
                    }
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }
}
