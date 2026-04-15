package org.xiaoyang.ex_enigmaticlegacy.Compat.JEI;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModItems;
import org.xiaoyang.ex_enigmaticlegacy.Recipe.AncientAlphirineRecipe;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class AncientAlphirineCategory implements IRecipeCategory<AncientAlphirineRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(Exe.MODID, "ancient_alphirine");
    public static final RecipeType<AncientAlphirineRecipe> RECIPE_TYPE =
            new RecipeType<>(UID, AncientAlphirineRecipe.class);

    private final IDrawable background;
    private final Component localizedName;
    private final IDrawable overlay;
    private final IDrawable icon;

    public AncientAlphirineCategory(IGuiHelper guiHelper) {
        background = guiHelper.createBlankDrawable(96, 44);
        localizedName = Component.translatable("jei." + Exe.MODID + ".ancient_alphirine");
        overlay = guiHelper.createDrawable(
                prefix("textures/gui/pure_daisy_overlay.png"), 0, 0, 64, 44);
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModItems.ANCIENT_ALPHIRINE.get()));
    }

    @Override
    public RecipeType<AncientAlphirineRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return localizedName;
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
    public void draw(AncientAlphirineRecipe recipe, IRecipeSlotsView slotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        overlay.draw(guiGraphics, 17, 0);
        RenderSystem.disableBlend();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AncientAlphirineRecipe recipe,
                          IFocusGroup focusGroup) {
        Ingredient input = recipe.getInput();
        ItemStack output = recipe.getResultItem(net.minecraft.core.RegistryAccess.EMPTY);

        IRecipeSlotBuilder inputSlotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, 9, 12);
        for (ItemStack stack : input.getItems()) {
            inputSlotBuilder.addItemStack(stack);
        }

        builder.addSlot(RecipeIngredientRole.CATALYST, 39, 12)
                .addItemStack(new ItemStack(ModItems.ANCIENT_ALPHIRINE.get()))
                .addTooltipCallback((view, tooltip) -> {
                    if (recipe.getChance() < 100) {
                        tooltip.add(Component.translatable(
                                "jei." + Exe.MODID + ".ancient_alphirine.chance",
                                recipe.getChance()));
                    }
                });

        builder.addSlot(RecipeIngredientRole.OUTPUT, 68, 12)
                .addItemStack(output);
    }
}