package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI.AvaritiaJei;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import morph.avaritia.api.ExtremeCraftingRecipe;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ContainerExtremeAutoCrafter;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.DefineShapeMessage;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AutoExtremeCrafterTransferHandler implements IRecipeTransferHandler<ContainerExtremeAutoCrafter, ExtremeCraftingRecipe> {

    @Nonnull
    @Override
    public Class<ContainerExtremeAutoCrafter> getContainerClass() {
        return ContainerExtremeAutoCrafter.class;
    }

    @Nonnull
    @Override
    public Class<ExtremeCraftingRecipe> getRecipeClass() {
        return ExtremeCraftingRecipe.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull final ContainerExtremeAutoCrafter container,
                                               @Nonnull final ExtremeCraftingRecipe recipe,
                                               @Nonnull final IRecipeSlotsView recipeSlots,
                                               @Nonnull final Player player,
                                               final boolean maxTransfer,
                                               final boolean doTransfer) {
        if (!doTransfer) {
            return null;
        }
        ResourceLocation recipeId = recipe.getId();
        DefineShapeMessage.sendToServer(container, recipeId);

        return null;
    }
}