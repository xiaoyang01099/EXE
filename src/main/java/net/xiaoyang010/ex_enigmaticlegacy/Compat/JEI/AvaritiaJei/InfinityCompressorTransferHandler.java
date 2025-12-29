package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI.AvaritiaJei;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import morph.avaritia.api.CompressorRecipe;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ContainerInfinityCompressor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InfinityCompressorTransferHandler implements IRecipeTransferHandler<ContainerInfinityCompressor, CompressorRecipe> {

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(ContainerInfinityCompressor container, CompressorRecipe recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {

        if (!doTransfer) return null;

        ResourceLocation recipeId = recipe.getId();
        if (recipeId != null) {
        }

        return null;
    }

    @Nonnull
    @Override
    public Class<ContainerInfinityCompressor> getContainerClass() {
        return ContainerInfinityCompressor.class;
    }

    @Nonnull
    @Override
    public Class<CompressorRecipe> getRecipeClass() {
        return CompressorRecipe.class;
    }
}