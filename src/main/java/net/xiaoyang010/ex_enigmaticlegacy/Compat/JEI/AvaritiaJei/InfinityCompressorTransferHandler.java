package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI.AvaritiaJei;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import morph.avaritia.api.CompressorRecipe;
import morph.avaritia.recipe.CompressorRecipeHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ContainerInfinityCompressor;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.DefineShapeMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class InfinityCompressorTransferHandler implements IRecipeTransferHandler<ContainerInfinityCompressor, CompressorRecipe> {

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

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull final ContainerInfinityCompressor container,
                                               @Nonnull final CompressorRecipe recipe,
                                               @Nonnull final IRecipeSlotsView recipeSlots,
                                               @Nonnull final Player player,
                                               final boolean maxTransfer,
                                               final boolean doTransfer) {
        if (!doTransfer) {
            return null;
        }

        ResourceLocation recipeId = recipe.getId();

        if (player.level != null) {
            Map<ResourceLocation, CompressorRecipe> recipes = CompressorRecipeHelper.getRecipeMap(player.level);
            CompressorRecipe recipeFromRegistry = recipes.get(recipeId);

            if (recipeFromRegistry != null && recipeFromRegistry == recipe) {
                DefineShapeMessage.sendToServer(container, recipeId);
            }
        }

        return null;
    }
}