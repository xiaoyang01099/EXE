package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.xiaoyang010.ex_enigmaticlegacy.Client.gui.DoubleCraftingScreen;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Util.DBHelper;
import org.jetbrains.annotations.NotNull;

public class DoubleCraftingRecipeCategory implements IRecipeCategory<CraftingRecipe> {
    private final IDrawable background;
    private final IDrawable icon;
    public static final ResourceLocation UID = new ResourceLocation("minecraft", "crafting_table");

    public DoubleCraftingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(DoubleCraftingScreen.CRAFTING_TABLE_LOCATION, 29, 16, 116, 108);
        this.icon = helper.createDrawableItemStack(new ItemStack(Items.CRAFTING_TABLE));
    }

    public @NotNull RecipeType<CraftingRecipe> getRecipeType() {
        return JEIPlugin.DOUBLE_CRAFTING_RECIPE_TYPE;
    }

    public @NotNull Component getTitle() {
        return DBHelper.CONTAINER_TITLE;
    }

    public @NotNull IDrawable getBackground() {
        return this.background;
    }

    public @NotNull IDrawable getIcon() {
        return this.icon;
    }

    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull CraftingRecipe recipe, @NotNull IFocusGroup focusGroup) {
        if (recipe instanceof IShapedRecipe<?> shapedRecipe) {
            int width = shapedRecipe.getRecipeWidth();
            int height = shapedRecipe.getRecipeHeight();
            int index = 0;
            NonNullList<Ingredient> ingredients = recipe.getIngredients();

            for(int i = 0; i < 6; ++i) {
                for(int j = 0; j < 3; ++j) {
                    int x = 30 + j * 18 - 29;
                    int y = -10 + i * 18 + 11;
                    IRecipeSlotBuilder recipeSlot = builder.addSlot(RecipeIngredientRole.INPUT, x, y);
                    boolean flag = j == 1 && i + 1 <= height;
                    boolean flag2 = width > 1 && j + 1 <= width && i + 1 <= height;
                    if (index < ingredients.size() && (flag || flag2)) {
                        recipeSlot.addIngredients(ingredients.get(index++));
                    }
                }
            }

            builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 47).addItemStack(recipe.getResultItem());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends CraftingRecipe> getRecipeClass() {
        return CraftingRecipe.class;
    }
}
