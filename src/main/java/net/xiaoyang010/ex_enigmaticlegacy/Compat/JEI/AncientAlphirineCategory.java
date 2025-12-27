package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.AncientAlphirineRecipe;

import javax.annotation.Nonnull;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class AncientAlphirineCategory implements IRecipeCategory<AncientAlphirineRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "ancient_alphirine");
    private final IDrawable background;
    private final Component localizedName;
    private final IDrawable overlay;
    private final IDrawable icon;

    public AncientAlphirineCategory(IGuiHelper guiHelper) {
        background = guiHelper.createBlankDrawable(96, 44);
        localizedName = new TranslatableComponent("jei." + ExEnigmaticlegacyMod.MODID + ".ancient_alphirine");
        overlay = guiHelper.createDrawable(prefix("textures/gui/pure_daisy_overlay.png"),
                0, 0, 64, 44);
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.ANCIENT_ALPHIRINE.get()));
    }

    @Nonnull
    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Nonnull
    @Override
    public Class<? extends AncientAlphirineRecipe>getRecipeClass() {
        return AncientAlphirineRecipe.class;
    }

    @Nonnull
    @Override
    public Component getTitle() {
        return localizedName;
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Nonnull
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(AncientAlphirineRecipe recipe, IRecipeSlotsView slotsView, PoseStack ms, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        overlay.draw(ms, 17, 0);
        RenderSystem.disableBlend();
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull AncientAlphirineRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        Ingredient input = recipe.getInput();
        ItemStack output = recipe.getResultItem();

        IRecipeSlotBuilder inputSlotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, 9, 12)
                .setFluidRenderer(1000, false, 16, 16);
        for (ItemStack stack : input.getItems()) {
            inputSlotBuilder.addItemStack(stack);
        }

        builder.addSlot(RecipeIngredientRole.CATALYST, 39, 12)
                .addItemStack(new ItemStack(ModItems.ANCIENT_ALPHIRINE.get()))
                .addTooltipCallback((view, tooltip) -> {
                    if (recipe.getChance() < 100) {
                        tooltip.add(new TranslatableComponent("jei." + ExEnigmaticlegacyMod.MODID + ".ancient_alphirine.chance", recipe.getChance()));
                    }
                });

        builder.addSlot(RecipeIngredientRole.OUTPUT, 68, 12)
                .setFluidRenderer(1000, false, 16, 16)
                .addItemStack(output);
    }
}