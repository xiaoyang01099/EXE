package org.xiaoyang.ex_enigmaticlegacy.Compat.JEI;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import org.xiaoyang.ex_enigmaticlegacy.ConfigHandler;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModItems;
import org.xiaoyang.ex_enigmaticlegacy.Recipe.StarlitSanctumRecipe;

import java.util.List;

public class StarlitSanctumCategory implements IRecipeCategory<StarlitSanctumRecipe> {

    public static final ResourceLocation UID =
            new ResourceLocation(Exe.MODID, "starlit_crafting");
    public static final RecipeType<StarlitSanctumRecipe> RECIPE_TYPE =
            new RecipeType<>(UID, StarlitSanctumRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;
    private final IDrawable fullSizeTexture;
    private final IDrawable emptyDrawable;

    private static final int TEX_WIDTH = 539;
    private static final int TEX_HEIGHT = 431;

    private float getScale() {
        if (ConfigHandler.starlitJeiScaleConfig != null) {
            return ConfigHandler.starlitJeiScaleConfig.get().floatValue();
        }
        return 0.7f;
    }

    private int getScaledWidth() {
        return (int) (TEX_WIDTH * getScale());
    }

    private int getScaledHeight() {
        return (int) (TEX_HEIGHT * getScale());
    }

    public StarlitSanctumCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(
                getScaledWidth(), getScaledHeight());

        ResourceLocation textureLoc = new ResourceLocation(
                Exe.MODID, "textures/gui/container/starlit_jei.png");
        this.fullSizeTexture = guiHelper.drawableBuilder(
                        textureLoc, 0, 0, TEX_WIDTH, TEX_HEIGHT)
                .setTextureSize(TEX_WIDTH, TEX_HEIGHT)
                .build();

        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModItems.STARLIT_SANCTUM.get()));
        this.localizedName = Component.translatable(
                "jei." + Exe.MODID + ".starlit_crafting");
        this.emptyDrawable = guiHelper.createBlankDrawable(0, 0);
    }

    @Override
    public RecipeType<StarlitSanctumRecipe> getRecipeType() {
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
    public void draw(StarlitSanctumRecipe recipe,
                     IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        RenderSystem.enableBlend();

        float scale = getScale();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0f);
        this.fullSizeTexture.draw(guiGraphics, 0, 0);
        guiGraphics.pose().popPose();

        long mana = recipe.getManaCost();
        Component manaComponent = Component.translatable(
                "gui.ex_enigmaticlegacy.mana_req", mana);
        String manaText = manaComponent.getString();

        Font font = Minecraft.getInstance().font;
        int scaledWidth = getScaledWidth();
        int scaledHeight = getScaledHeight();
        int textX = (scaledWidth - font.width(manaText)) / 2;
        int textY = scaledHeight - 2;

        guiGraphics.drawString(font, manaText, textX, textY, 0x00AAFF, false);

        RenderSystem.disableBlend();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder,
                          StarlitSanctumRecipe recipe,
                          IFocusGroup focuses) {
        float scale = getScale();

        int blockStartY = 85;
        int[] blockStartX = {23, 188, 355};

        List<NonNullList<Ingredient>> patternGroups = recipe.getPatternGroups();

        for (int row = 0; row < 18; row++) {
            for (int col = 0; col < 27; col++) {
                int drawX;
                int drawY = blockStartY + row * 18;

                if (col < 9) {
                    drawX = blockStartX[0] + col * 18;
                } else if (col < 18) {
                    drawX = blockStartX[1] + (col - 9) * 18;
                } else {
                    drawX = blockStartX[2] + (col - 18) * 18;
                }

                Ingredient ing = Ingredient.EMPTY;
                int blockIndex = col / 9;
                int colInBlock = col % 9;

                if (blockIndex < patternGroups.size()) {
                    List<Ingredient> pattern = patternGroups.get(blockIndex);
                    int indexInPattern = row * 9 + colInBlock;
                    if (indexInPattern < pattern.size()) {
                        ing = pattern.get(indexInPattern);
                    }
                }
                addScaledSlot(builder, ing, drawX, drawY,
                        RecipeIngredientRole.INPUT, scale);
            }
        }

        addScaledSlot(builder, recipe.getLeftInput(), 96, 32,
                RecipeIngredientRole.INPUT, scale)
                .addTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.translatable(
                                "jei.ex_enigmaticlegacy.starlit_crafting.count",
                                recipe.getLeftInputCount())));

        addScaledSlot(builder, recipe.getRightInput(), 425, 32,
                RecipeIngredientRole.INPUT, scale)
                .addTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.translatable(
                                "jei.ex_enigmaticlegacy.starlit_crafting.count",
                                recipe.getRightInputCount())));

        addScaledSlot(builder, Ingredient.of(recipe.getResultItem(
                        net.minecraft.core.RegistryAccess.EMPTY)),
                261, 32, RecipeIngredientRole.OUTPUT, scale);
    }

    private IRecipeSlotBuilder addScaledSlot(IRecipeLayoutBuilder builder,
                                             Ingredient ingredient,
                                             int originalX, int originalY,
                                             RecipeIngredientRole role,
                                             float scale) {
        int finalX = (int) (originalX * scale);
        int finalY = (int) (originalY * scale);

        IRecipeSlotBuilder slotBuilder = builder.addSlot(role, finalX, finalY)
                .setBackground(this.emptyDrawable, 0, 0)
                .setCustomRenderer(VanillaTypes.ITEM_STACK,
                        new ScaledItemRenderer(scale));

        if (ingredient != null && !ingredient.isEmpty()) {
            slotBuilder.addIngredients(ingredient);
        }
        return slotBuilder;
    }

    private static class ScaledItemRenderer implements IIngredientRenderer<ItemStack> {
        private final float scale;

        public ScaledItemRenderer(float scale) {
            this.scale = scale;
        }

        @Override
        public void render(GuiGraphics guiGraphics, ItemStack ingredient) {
            if (ingredient != null && !ingredient.isEmpty()) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(scale, scale, 1.0f);
                guiGraphics.renderItem(ingredient, 0, 0);
                guiGraphics.pose().popPose();
            }
        }

        @Override
        public List<Component> getTooltip(ItemStack ingredient,
                                          TooltipFlag tooltipFlag) {
            return ingredient.getTooltipLines(
                    Minecraft.getInstance().player, tooltipFlag);
        }

        @Override
        public int getWidth() {
            return (int) (16 * scale);
        }

        @Override
        public int getHeight() {
            return (int) (16 * scale);
        }
    }
}