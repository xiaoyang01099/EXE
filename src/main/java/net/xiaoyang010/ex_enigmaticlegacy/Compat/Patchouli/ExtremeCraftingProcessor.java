package net.xiaoyang010.ex_enigmaticlegacy.Compat.Patchouli;

import morph.avaritia.recipe.ExtremeShapedRecipe;
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

public class ExtremeCraftingProcessor implements IComponentProcessor {
    private Recipe<?> recipe;
    private boolean isShaped = false;
    private int width = 9;
    private int height = 9;

    @Override
    public void setup(IVariableProvider variables) {
        String recipeId = variables.get("recipe").asString();

        if (Minecraft.getInstance().level == null) {
            return;
        }

        RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
        this.recipe = manager.byKey(new ResourceLocation(recipeId)).orElse(null);

        if (recipe instanceof ExtremeShapedRecipe shapedRecipe) {
            this.isShaped = true;
            this.width = shapedRecipe.getWidth();
            this.height = shapedRecipe.getHeight();
        } else if (recipe instanceof ExtremeShapelessRecipe) {
            this.isShaped = false;
        }
    }

    @Override
    public IVariable process(String key) {
        if (recipe == null) {
            return null;
        }

        // 处理输出物品
        if (key.equals("output")) {
            if (recipe instanceof ExtremeShapedRecipe shapedRecipe) {
                return IVariable.from(shapedRecipe.getResultItem());
            } else if (recipe instanceof ExtremeShapelessRecipe shapelessRecipe) {
                return IVariable.from(shapelessRecipe.getResultItem());
            }
        }

        // 处理配方标题
        if (key.equals("recipe_name")) {
            ItemStack result = ItemStack.EMPTY;
            if (recipe instanceof ExtremeShapedRecipe shapedRecipe) {
                result = shapedRecipe.getResultItem();
            } else if (recipe instanceof ExtremeShapelessRecipe shapelessRecipe) {
                result = shapelessRecipe.getResultItem();
            }
            return result.isEmpty() ? null : IVariable.wrap(result.getHoverName().getString());
        }

        // 处理配方类型文本
        if (key.equals("recipe_type")) {
            return IVariable.wrap(isShaped ? "有序配方" : "无序配方");
        }

        // 处理 9x9 网格 (grid0-grid80)
        if (key.startsWith("grid")) {
            try {
                int index = Integer.parseInt(key.substring(4));

                if (recipe instanceof ExtremeShapedRecipe shapedRecipe) {
                    return getShapedIngredient(shapedRecipe, index);
                } else if (recipe instanceof ExtremeShapelessRecipe shapelessRecipe) {
                    return getShapelessIngredient(shapelessRecipe, index);
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    /**
     * 获取有序配方的材料
     */
    private IVariable getShapedIngredient(ExtremeShapedRecipe recipe, int gridIndex) {
        int row = gridIndex / 9;
        int col = gridIndex % 9;

        // 检查是否在配方范围内
        if (col >= width || row >= height) {
            return IVariable.from(ItemStack.EMPTY);
        }

        // 计算配方中的实际索引
        int recipeIndex = col + row * width;

        if (recipeIndex < recipe.getIngredients().size()) {
            Ingredient ingredient = recipe.getIngredients().get(recipeIndex);
            ItemStack[] stacks = ingredient.getItems();

            if (stacks.length > 0) {
                return IVariable.from(stacks[0]);
            }
        }

        return IVariable.from(ItemStack.EMPTY);
    }

    /**
     * 获取无序配方的材料（以螺旋形式排列）
     */
    private IVariable getShapelessIngredient(ExtremeShapelessRecipe recipe, int gridIndex) {
        if (gridIndex < recipe.getIngredients().size()) {
            Ingredient ingredient = recipe.getIngredients().get(gridIndex);
            ItemStack[] stacks = ingredient.getItems();

            if (stacks.length > 0) {
                return IVariable.from(stacks[0]);
            }
        }

        return IVariable.from(ItemStack.EMPTY);
    }
}
