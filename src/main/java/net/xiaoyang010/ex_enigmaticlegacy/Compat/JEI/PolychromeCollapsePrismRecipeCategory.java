package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
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
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.PolychromeRecipe;
import vazkii.botania.client.gui.HUDHandler;

import javax.annotation.Nonnull;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;


public class PolychromeCollapsePrismRecipeCategory implements IRecipeCategory<PolychromeRecipe> {
    public static final ResourceLocation UID = new ResourceLocation("ex_enigmaticlegacy", "polychrome_collapse_prism");

    private final Component localizedName;
    private final IDrawable background;
    private final IDrawable overlay;
    private final IDrawable icon;
    private final IDrawable fullStructure;

    public PolychromeCollapsePrismRecipeCategory(IGuiHelper guiHelper) {
        // 创建更大的背景以容纳完整的15x15结构
        ResourceLocation location = prefix("textures/gui/terrasteel_jei_overlay.png");
        background = guiHelper.createBlankDrawable(300, 400);
        overlay = guiHelper.createDrawable(location, 0, 0, 300, 400);
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlockss.POLYCHROME_COLLAPSE_PRISM.get()));
        localizedName = new TranslatableComponent("jei.ex_enigmaticlegacy.category.polychrome_collapse_prism");

        // 创建完整的多方块结构绘制
        fullStructure = new PolychromeCollapsePrismDrawable(guiHelper);
    }

    @Nonnull
    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Nonnull
    @Override
    public Class<? extends PolychromeRecipe> getRecipeClass() {
        return PolychromeRecipe.class;
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
    public void draw(@Nonnull PolychromeRecipe recipe, @Nonnull IRecipeSlotsView view, @Nonnull PoseStack ms, double mouseX, double mouseY) {
        RenderSystem.enableBlend();

        // 绘制覆盖层
        overlay.draw(ms, 0, 0);

        // 绘制魔力条 - 使用彩虹色调
        HUDHandler.renderManaBar(ms, 10, 380, 0xFF00FF, 0.75F, recipe.getManaUsage(), recipe.getManaUsage());

        // 绘制完整的多方块结构
        fullStructure.draw(ms, 20, 100);

        // 绘制结构说明文字
        drawStructureInfo(ms);

        RenderSystem.disableBlend();
    }

    private void drawStructureInfo(PoseStack ms) {
        ms.pushPose();
        ms.popPose();
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull PolychromeRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        // 输出槽 - 右上角位置
        builder.addSlot(RecipeIngredientRole.OUTPUT, 250, 20)
                .addItemStack(recipe.getResultItem());

        // 输入槽 - 左侧垂直排列
        var ingredients = recipe.getIngredients();
        int startY = 20;
        int slotSpacing = 20;

        for (int i = 0; i < ingredients.size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 10, startY + i * slotSpacing)
                    .addIngredients(ingredients.get(i));
        }

        builder.addSlot(RecipeIngredientRole.CATALYST, 150, 210)
                .addItemStack(new ItemStack(ModBlockss.POLYCHROME_COLLAPSE_PRISM.get()));
    }
}