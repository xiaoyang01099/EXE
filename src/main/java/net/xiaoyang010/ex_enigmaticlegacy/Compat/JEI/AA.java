package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import morph.avaritia.api.ExtremeCraftingRecipe;
import morph.avaritia.compat.jei.AvaritiaJEIPlugin;
import morph.avaritia.recipe.ExtremeShapedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;

import java.util.ArrayList;
import java.util.List;

public class AA implements IRecipeCategory<ExtremeCraftingRecipe> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation("avaritia", "textures/gui/extreme_crafting_jei.png");
    private static final Component TITLE = new TranslatableComponent("AAAAAAAAA");
    private final IDrawable background;
    private final IDrawable icon;

    public AA(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(BACKGROUND, 0, 0, 189, 163);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlockss.EXTREME_AUTO_CRAFTER.get()));
    }

    public void setRecipe(IRecipeLayoutBuilder builder, ExtremeCraftingRecipe recipe, IFocusGroup focuses) {
        List<List<ItemStack>> inputs = recipe.getIngredients().stream().map((ingredient) -> List.of(ingredient.getItems())).toList();
        ItemStack resultItem = recipe.getResultItem();
        builder.addSlot(RecipeIngredientRole.OUTPUT, 168, 74).addIngredients(VanillaTypes.ITEM_STACK, List.of(resultItem));
        List<IRecipeSlotBuilder> inputSlots = new ArrayList(81);

        int w;
        for (int y = 0; y < 9; ++y) {
            for (w = 0; w < 9; ++w) {
                inputSlots.add(builder.addSlot(RecipeIngredientRole.INPUT, w * 18 + 2, y * 18 + 2));
            }
        }

        if (recipe instanceof ExtremeShapedRecipe r) {
            for (w = 0; w < r.getWidth(); ++w) {
                for (int h = 0; h < r.getHeight(); ++h) {
                    int idx = w + h * 9;
                    int idx2 = w + h * r.getWidth();
                    ((IRecipeSlotBuilder) inputSlots.get(idx)).addIngredients(VanillaTypes.ITEM_STACK, (List) inputs.get(idx2));
                }
            }
        } else {
            builder.setShapeless();

            for (w = 0; w < inputs.size(); ++w) {
                inputSlots.get(w).addIngredients(VanillaTypes.ITEM_STACK, (List) inputs.get(w));
            }
        }

    }

    public Component getTitle() {
        return TITLE;
    }

    public IDrawable getBackground() {
        return this.background;
    }

    public IDrawable getIcon() {
        return this.icon;
    }

    public ResourceLocation getUid() {
        return this.getRecipeType().getUid();
    }

    public Class<? extends ExtremeCraftingRecipe> getRecipeClass() {
        return this.getRecipeType().getRecipeClass();
    }

    public RecipeType<ExtremeCraftingRecipe> getRecipeType() {
        return AvaritiaJEIPlugin.EXTREME_CRAFTING_TYPE;
    }
}