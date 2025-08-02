package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import com.mojang.blaze3d.vertex.PoseStack;
import morph.avaritia.api.CompressorRecipe;
import morph.avaritia.recipe.CompressorRecipeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.ITooltipSupplier;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RecipeResultItemElement extends ItemElement {
    private final ITooltipSupplier helpTooltipSupplier;
    private final Font fontRenderer;

    public RecipeResultItemElement(@Nonnull final Supplier<ItemStack> stackSupplier, @Nonnull final WGuiContainer<?> wGuiContainer, final int x, final int y) {
        super(stackSupplier, wGuiContainer, x, y);
        this.fontRenderer = getFontRenderer();
        final List<Component> helpTooltip = new ArrayList<>();
        helpTooltip.add(EComponent.translatable("ex_enigmaticlegacy.how-to-use").withStyle(ChatFormatting.GOLD));
        helpTooltip.add(EComponent.translatable("ex_enigmaticlegacy.compressor.usage.1"));
        helpTooltip.add(EComponent.translatable("ex_enigmaticlegacy.compressor.usage.2"));
        helpTooltip.add(EComponent.empty());
        helpTooltip.add(EComponent.translatable("ex_enigmaticlegacy.compressor.to.set").withStyle(ChatFormatting.GOLD));
        helpTooltip.add(EComponent.translatable("ex_enigmaticlegacy.compressor.to.set.desc.1"));
        helpTooltip.add(EComponent.translatable("ex_enigmaticlegacy.compressor.to.set.desc.2"));
        helpTooltipSupplier = (interaction, stackSupplier2) -> helpTooltip;
        setDefaultForegroundCheck();
    }

    public void draw(@Nonnull final WInteraction wInteraction) {
        if (!stackSupplier.get().isEmpty()) {
            super.draw(wInteraction);
        }
    }

    @Override
    public void drawForeground(@Nonnull final WInteraction interaction, @Nonnull final PoseStack poseStack) {
        if (stackSupplier.get().isEmpty()) {
            Component questionMark = EComponent.literal("?");
            fontRenderer.draw(poseStack, questionMark, getX() + 5, getY() + 5, 0xFFFFFF);
        }
        super.drawForeground(interaction, poseStack);
    }

    @Override
    @Nonnull
    public ITooltipSupplier getTooltipSupplier() {
        return stackSupplier.get().isEmpty() ? helpTooltipSupplier : super.getTooltipSupplier();
    }

    @Override
    public void interact(@Nonnull final WMouseInteraction wMouseInteraction) {
        if (stackSupplier.get().isEmpty()) {
            final ItemStack playerStack = wMouseInteraction.getEntityPlayer().containerMenu.getCarried();
            final CompressorRecipe compressorRecipe = CompressorRecipeHelper.getRecipe(
                    wMouseInteraction.getEntityPlayer().level, playerStack);
            final ResourceLocation recipeId = compressorRecipe != null ? compressorRecipe.getId() : null;
            if (recipeId != null) {
                DefineShapeMessage.sendToServer(wMouseInteraction.getWContainer(), recipeId);
                playPressSound();
            }
        } else if (Screen.hasShiftDown()) {
            ClearShapeMessage.sendToServer(getWContainer());
            playPressSound();
        }
    }
}