package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vazkii.botania.api.recipe.IRuneAltarRecipe;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.integration.jei.RunicAltarRecipeCategory;
import vazkii.botania.common.block.tile.mana.TilePool;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static vazkii.botania.client.gui.HUDHandler.manaBar;

@Mixin(RunicAltarRecipeCategory.class)
public abstract class RunicAltarRecipeCategoryMixin implements IRecipeCategory<IRuneAltarRecipe> {

    @Unique
    private static int mana_display$lastManaBarX = 6;
    @Unique
    private static int mana_display$lastManaBarY = 150;
    @Unique
    private static final int MANA_BAR_WIDTH = 102;
    @Unique
    private static final int MANA_BAR_HEIGHT = 5;

    @Redirect(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Lvazkii/botania/client/gui/HUDHandler;renderManaBar(Lcom/mojang/blaze3d/vertex/PoseStack;IIIFII)V"
            ),
            remap = false
    )
    private void mana_display$renderManaBarWithNumbers(PoseStack poseStack, int x, int y, int color, float alpha, int mana, int maxMana) {
        mana_display$lastManaBarX = x;
        mana_display$lastManaBarY = y;
        RenderSystem.setShaderTexture(0, manaBar);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderHelper.drawTexturedModalRect(poseStack, x, y, 0, 0, 102, 5);

        int manaPercentage = Math.max(0, (int)((double)mana / maxMana * 100));
        if (manaPercentage == 0 && mana > 0) manaPercentage = 1;

        RenderHelper.drawTexturedModalRect(poseStack, x + 1, y + 1, 0, 5, 100, 3);

        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        RenderSystem.setShaderColor(red, green, blue, alpha);

        RenderHelper.drawTexturedModalRect(poseStack, x + 1, y + 1, 0, 5, Math.min(100, manaPercentage), 3);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(
            IRuneAltarRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            double mouseX,
            double mouseY
    ) {
        if (mana_display$isMouseOverManaBar(mouseX, mouseY)) {
            int manaCost = recipe.getManaUsage();
            return List.of(mana_display$createManaTooltip(manaCost));
        }

        return IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
    }

    @Unique
    private boolean mana_display$isMouseOverManaBar(double mouseX, double mouseY) {
        return mouseX >= mana_display$lastManaBarX &&
                mouseX <= mana_display$lastManaBarX + MANA_BAR_WIDTH &&
                mouseY >= mana_display$lastManaBarY &&
                mouseY <= mana_display$lastManaBarY + MANA_BAR_HEIGHT;
    }

    @Unique
    private Component mana_display$createManaTooltip(int mana) {
        String formattedMana = mana_display$formatNumber(mana);
        String formattedMaxMana = mana_display$formatNumber(TilePool.MAX_MANA / 10);

        MutableComponent manaText = new TranslatableComponent(
                "ex.mana",
                new TextComponent(formattedMana).withStyle(ChatFormatting.AQUA),
                new TextComponent(formattedMaxMana).withStyle(ChatFormatting.AQUA)
        ).withStyle(ChatFormatting.AQUA);

        return manaText.withStyle(ChatFormatting.AQUA);
    }

    @Unique
    private static String mana_display$formatNumber(int number) {
        return NumberFormat.getNumberInstance(Locale.ROOT).format(number)
                .replace(",", " ");
    }
}