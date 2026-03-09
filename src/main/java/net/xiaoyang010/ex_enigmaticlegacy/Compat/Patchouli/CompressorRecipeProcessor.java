package net.xiaoyang010.ex_enigmaticlegacy.Compat.Patchouli;

import morph.avaritia.recipe.DefaultCompressorRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;


public class CompressorRecipeProcessor implements IComponentProcessor {

    private DefaultCompressorRecipe recipe;

    @Override
    public void setup(IVariableProvider variables) {
        String recipeId = variables.get("recipe").asString();

        RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
        ResourceLocation id = new ResourceLocation(recipeId);

        this.recipe = (DefaultCompressorRecipe) manager.byKey(id).orElse(null);
    }

    @Override
    public IVariable process(String key) {
        if (recipe == null) {
            return null;
        }

        if (key.equals("output")) {
            return IVariable.from(recipe.getResultItem());
        }

        if (key.equals("cost")) {
            return IVariable.wrap(String.valueOf(recipe.getCost()));
        }

        if (key.startsWith("input")) {
            int index = Integer.parseInt(key.substring(5)) - 1;
            NonNullList<Ingredient> ingredients = recipe.getIngredients();

            if (index >= 0 && index < ingredients.size()) {
                Ingredient ingredient = ingredients.get(index);
                ItemStack[] stacks = ingredient.getItems();

                if (stacks.length > 0) {
                    return IVariable.from(stacks);
                }
            }

            return IVariable.from(ItemStack.EMPTY);
        }

        return null;
    }
}