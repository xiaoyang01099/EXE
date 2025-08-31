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

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(
            ContainerExtremeAutoCrafter container,
            ExtremeCraftingRecipe recipe,
            IRecipeSlotsView recipeSlots,
            Player player,
            boolean maxTransfer,
            boolean doTransfer) {

        if (!doTransfer)
            return null;

        ResourceLocation recipeId = recipe.getId();
        if (recipeId != null) {
            DefineShapeMessage.sendToServer(container, recipeId);
        }

        return null;
    }

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
}