package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.CelestialTransmuteRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("deprecation")
public class CelestialTransmuteRecipeCategory implements IRecipeCategory<CelestialTransmuteRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "celestial_transmute");
    public static final ResourceLocation TEXTURE = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/gui/celestial_holiness_transmute_jei.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public CelestialTransmuteRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE,0,0,176,88);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlockss.CELESTIAL_HOLINESS_TRANSMUTER.get()));
        this.title = new TranslatableComponent("gui." + ExEnigmaticlegacyMod.MODID + ".category.celestial_transmute");
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, CelestialTransmuteRecipe recipe, @NotNull IFocusGroup focuses) {

        List<Ingredient> ingredients = recipe.getRecipeItems();
        List<Integer> counts = recipe.getInputCounts();

        for (int i = 0; i < ingredients.size(); i++) {
            int x = (i == 0) ? 21 : (i == 1) ? 56 : (i == 2) ? 38 : 132;
            int y = (i < 2) ? 23 : 51;

            int finalI = i;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .addIngredients(ingredients.get(i))
                    .addTooltipCallback((recipeSlotView, tooltip) -> {
                        if (counts.get(finalI) > 1) {
                            tooltip.add(new TranslatableComponent("recipe.required_amount", counts.get(finalI)));
                        }
                    });
        }


        builder.addSlot(RecipeIngredientRole.OUTPUT, 132, 23)
                .addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(CelestialTransmuteRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull PoseStack stack, double mouseX, double mouseY) {
        for (int i = 0; i < recipe.getInputCounts().size(); i++) {
            int count = recipe.getInputCounts().get(i);
            if (count > 1) {
                int x = (i == 0) ? 21 : (i == 1) ? 56 : (i == 2) ? 38 : 132;
                int y = (i < 2) ? 23 : 51;
                String countText = String.valueOf(count);
                Minecraft.getInstance().font.draw(stack, countText, x, y - 10, 0xFFFFFF);
            }
        }
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
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    @NotNull
    public Class<? extends CelestialTransmuteRecipe> getRecipeClass() {
        return CelestialTransmuteRecipe.class;
    }
}