package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.MagicTableRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MagicTableRecipeCategory implements IRecipeCategory<MagicTableRecipe> {
    public static final ResourceLocation UID = new ResourceLocation("ex_enigmaticlegacy", "magic_table");
    public static final RecipeType<MagicTableRecipe> RECIPE_TYPE = new RecipeType<>(UID, MagicTableRecipe.class);
    private static final ResourceLocation TEXTURE = new ResourceLocation("ex_enigmaticlegacy", "textures/gui/container/magic_table_gui.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;
    private final IDrawableAnimated arrow;

    private static final int BG_WIDTH = 116;
    private static final int BG_HEIGHT = 36;

    private static final int INPUT_SLOT_X = 1;
    private static final int INPUT_SLOT_Y = 10;

    private static final int OUTPUT_SLOT_X = 95;
    private static final int OUTPUT_SLOT_Y = 10;

    private static final int ARROW_X = 40;
    private static final int ARROW_Y = 10;

    private static final int ARROW_TEX_U = 0;
    private static final int ARROW_TEX_V = 234;
    private static final int ARROW_WIDTH = 40;
    private static final int ARROW_HEIGHT = 15;

    public MagicTableRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(BG_WIDTH, BG_HEIGHT);
        this.title = new TranslatableComponent("gui." + ExEnigmaticlegacyMod.MODID + ".category.magic_table");
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlockss.MAGIC_TABLE.get()));

        IDrawableStatic arrowStatic = guiHelper.drawableBuilder(TEXTURE, ARROW_TEX_U, ARROW_TEX_V, ARROW_WIDTH, ARROW_HEIGHT)
                .setTextureSize(256, 256)
                .build();
        this.arrow = guiHelper.createAnimatedDrawable(arrowStatic, 60, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public @NotNull RecipeType<MagicTableRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(@NotNull MagicTableRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull PoseStack stack, double mouseX, double mouseY) {
        arrow.draw(stack, ARROW_X, ARROW_Y);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull MagicTableRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_SLOT_X, INPUT_SLOT_Y)
                .addIngredients(recipe.getInput());

        ItemStack outputDisplay = recipe.getOutput().copy();
        outputDisplay.setCount((int) Math.min(recipe.getOutputCount(), 64));
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_SLOT_X, OUTPUT_SLOT_Y)
                .addItemStack(outputDisplay);
    }

    @SuppressWarnings("removal")
    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull MagicTableRecipe recipe, double mouseX, double mouseY) {
        if (mouseX >= ARROW_X && mouseX <= ARROW_X + ARROW_WIDTH
                && mouseY >= ARROW_Y && mouseY <= ARROW_Y + ARROW_HEIGHT) {
            return List.of(
                    new TranslatableComponent("gui.ex_enigmaticlegacy.magic_table.tooltip.emc_cost",
                            recipe.getEmcCost().toString()).withStyle(ChatFormatting.GOLD),
                    new TranslatableComponent("gui.ex_enigmaticlegacy.magic_table.tooltip.output",
                            formatCount(recipe.getOutputCount())).withStyle(ChatFormatting.AQUA),
                    new TranslatableComponent("gui.ex_enigmaticlegacy.magic_table.tooltip.time",
                            recipe.getCraftTicks(),
                            String.format("%.1f", recipe.getCraftTicks() / 20.0)).withStyle(ChatFormatting.GREEN)
            );
        }
        return List.of();
    }

    private static String formatCount(long count) {
        if (count == Long.MAX_VALUE) return "MAX (Long)";
        if (count == Integer.MAX_VALUE) return "MAX (Int)";
        if (count < 1000) return String.valueOf(count);
        if (count < 1_000_000) return String.format("%.1fK", count / 1000.0);
        if (count < 1_000_000_000L) return String.format("%.1fM", count / 1_000_000.0);
        if (count < 1_000_000_000_000L) return String.format("%.1fB", count / 1_000_000_000.0);
        return String.format("%.1fT", count / 1_000_000_000_000.0);
    }

    @SuppressWarnings("removal")
    @Override
    public @NotNull ResourceLocation getUid() {
        return UID;
    }

    @SuppressWarnings("removal")
    @Override
    public @NotNull Class<? extends MagicTableRecipe> getRecipeClass() {
        return MagicTableRecipe.class;
    }
}