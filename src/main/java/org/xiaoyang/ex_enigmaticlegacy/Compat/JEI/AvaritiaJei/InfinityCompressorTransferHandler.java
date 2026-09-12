package org.xiaoyang.ex_enigmaticlegacy.Compat.JEI.AvaritiaJei;

import com.yuo.endless.compat.jei.NeutroniumCRecipeCategory;
import com.yuo.endless.recipe.NeutroniumRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xiaoyang.ex_enigmaticlegacy.Container.ContainerInfinityCompressor;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModMenus;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.InfinityCompressorRecipePacket;

import java.util.Optional;

public class InfinityCompressorTransferHandler implements IRecipeTransferHandler<ContainerInfinityCompressor, NeutroniumRecipe> {
    private final IRecipeTransferHandlerHelper helper;
    public InfinityCompressorTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
    }

    @Override
    public Class<ContainerInfinityCompressor> getContainerClass() {
        return ContainerInfinityCompressor.class;
    }

    @Override
    public @NotNull Optional<MenuType<ContainerInfinityCompressor>> getMenuType() {
        return Optional.of(ModMenus.INFINITY_COMPRESSOR_MENU.get());
    }

    @Override
    public @NotNull RecipeType<NeutroniumRecipe> getRecipeType() {
        return NeutroniumCRecipeCategory.RECIPE_TYPE;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(@NotNull ContainerInfinityCompressor container,
                                                          @NotNull NeutroniumRecipe recipe,
                                                          @NotNull IRecipeSlotsView recipeSlots,
                                                          @NotNull Player player, boolean maxTransfer,
                                                          boolean doTransfer) {
        int available = 0;
        for (var stack : player.getInventory().items) if (recipe.getInput().test(stack)) available += stack.getCount();
        if (available < recipe.getRecipeCount()) {
            return helper.createUserErrorForMissingSlots(Component.translatable("jei.tooltip.error.recipe.transfer.missing"),
                    recipeSlots.getSlotViews());
        }
        if (doTransfer) {
            NetworkHandler.sendToServer(new InfinityCompressorRecipePacket(recipe.getId()));
        }
        return null;
    }
}
