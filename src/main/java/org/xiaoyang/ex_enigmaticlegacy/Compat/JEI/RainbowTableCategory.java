package org.xiaoyang.ex_enigmaticlegacy.Compat.JEI;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
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
import net.minecraft.world.item.crafting.Ingredient;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModBlocks;
import org.xiaoyang.ex_enigmaticlegacy.Recipe.RainbowTableRecipe;

import java.util.List;

public class RainbowTableCategory implements IRecipeCategory<RainbowTableRecipe> {
    public static final ResourceLocation UID =
            new ResourceLocation(Exe.MODID, "rainbow_table");
    public static final RecipeType<RainbowTableRecipe> RECIPE_TYPE =
            new RecipeType<>(UID, RainbowTableRecipe.class);
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(Exe.MODID,
                    "textures/gui/container/rainbow_nature.png");

    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;

    public RainbowTableCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 83);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.RAINBOW_TABLE.get()));
        this.title = Component.translatable(
                "gui." + Exe.MODID + ".category.rainbow_table");
    }

    @Override
    public RecipeType<RainbowTableRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder,
                          RainbowTableRecipe recipe,
                          IFocusGroup focuses) {
        List<Ingredient> recipeItems = recipe.getRecipeItems();
        NonNullList<Integer> inputCounts = recipe.getInputCounts();

        if (recipeItems.size() > 0) {
            ItemStack stack1 = new ItemStack(recipeItems.get(0).getItems()[0].getItem());
            stack1.setCount(inputCounts.get(0));
            builder.addSlot(RecipeIngredientRole.INPUT, 26, 36)
                    .addItemStack(stack1);
        }

        if (recipeItems.size() > 1) {
            ItemStack stack2 = new ItemStack(recipeItems.get(1).getItems()[0].getItem());
            stack2.setCount(inputCounts.get(1));
            builder.addSlot(RecipeIngredientRole.INPUT, 26, 54)
                    .addItemStack(stack2);
        }

        if (recipeItems.size() > 2) {
            ItemStack stack3 = new ItemStack(recipeItems.get(2).getItems()[0].getItem());
            stack3.setCount(inputCounts.get(2));
            builder.addSlot(RecipeIngredientRole.INPUT, 78, 36)
                    .addItemStack(stack3);
        }

        if (recipeItems.size() > 3) {
            ItemStack stack4 = new ItemStack(recipeItems.get(3).getItems()[0].getItem());
            stack4.setCount(inputCounts.get(3));
            builder.addSlot(RecipeIngredientRole.INPUT, 78, 54)
                    .addItemStack(stack4);
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 137, 44)
                .addItemStack(recipe.getResultItem(
                        net.minecraft.core.RegistryAccess.EMPTY));
    }
}