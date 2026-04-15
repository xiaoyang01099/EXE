package org.xiaoyang.ex_enigmaticlegacy.Compat.JEI;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xiaoyang.ex_enigmaticlegacy.Container.StarlitSanctumMenu;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModMenus;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.RecipeTransferPacket;
import org.xiaoyang.ex_enigmaticlegacy.Recipe.StarlitSanctumRecipe;
import org.xiaoyang.ex_enigmaticlegacy.Tile.StarlitSanctumTile;

import java.util.*;

public class StarlitSanctumTransferHandler implements IRecipeTransferHandler<StarlitSanctumMenu, StarlitSanctumRecipe> {
    public static final RecipeType<StarlitSanctumRecipe> RECIPE_TYPE = RecipeType.create("ex_enigmaticlegacy", "starlit_sanctum", StarlitSanctumRecipe.class);
    private final IRecipeTransferHandlerHelper handlerHelper;

    public StarlitSanctumTransferHandler(IRecipeTransferHandlerHelper handlerHelper) {
        this.handlerHelper = handlerHelper;
    }

    @Override
    public Class<StarlitSanctumMenu> getContainerClass() {
        return StarlitSanctumMenu.class;
    }

    @Override
    public @NotNull Optional<MenuType<StarlitSanctumMenu>> getMenuType() {
        return Optional.of(ModMenus.STARLIT_SANCTUM_SCREEN.get());

    }

    @Override
    public @NotNull RecipeType<StarlitSanctumRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(@NotNull StarlitSanctumMenu container, @NotNull StarlitSanctumRecipe recipe, @NotNull IRecipeSlotsView recipeSlots, @NotNull Player player, boolean maxTransfer, boolean doTransfer) {

        Map<Integer, RequiredItem> slotRequirements = new HashMap<>();

        var patternGroups = recipe.getPatternGroups();

        for (int row = 0; row < 18; row++) {
            for (int col = 0; col < 27; col++) {
                int jeiSlotIndex = row * 27 + col;

                int blockIndex  = col / 9;
                int colInBlock  = col % 9;

                if (blockIndex < patternGroups.size()) {
                    var pattern = patternGroups.get(blockIndex);
                    int indexInPattern = row * 9 + colInBlock;

                    if (indexInPattern < pattern.size()) {
                        Ingredient ing = pattern.get(indexInPattern);
                        if (!ing.isEmpty()) {
                            slotRequirements.put(jeiSlotIndex, new RequiredItem(ing, 1));
                        }
                    }
                }
            }
        }

        if (!recipe.getLeftInput().isEmpty()) {
            slotRequirements.put(486, new RequiredItem(recipe.getLeftInput(), recipe.getLeftInputCount()));
        }
        if (!recipe.getRightInput().isEmpty()) {
            slotRequirements.put(487, new RequiredItem(recipe.getRightInput(), recipe.getRightInputCount()));
        }

        Map<Ingredient, Integer> availableItems = new IdentityHashMap<>();
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            for (RequiredItem req : slotRequirements.values()) {
                if (req.ingredient.test(stack)) {
                    availableItems.merge(req.ingredient, stack.getCount(), Integer::sum);
                }
            }
        }

        List<IRecipeSlotView> allSlotViews = recipeSlots.getSlotViews();
        List<IRecipeSlotView> missingSlotViews = new ArrayList<>();

        for (Map.Entry<Integer, RequiredItem> entry : slotRequirements.entrySet()) {
            int jeiSlotIndex  = entry.getKey();
            RequiredItem required = entry.getValue();

            if (jeiSlotIndex >= allSlotViews.size()) continue;

            int available = availableItems.getOrDefault(required.ingredient, 0);
            if (available < required.count) {
                missingSlotViews.add(allSlotViews.get(jeiSlotIndex));
            }
        }

        if (!recipe.getLeftInput().isEmpty()) {
            boolean hasValidStarlitItem = false;
            for (ItemStack stack : player.getInventory().items) {
                if (recipe.getLeftInput().test(stack)
                        && stack.is(StarlitSanctumTile.STARLIT)) {
                    hasValidStarlitItem = true;
                    break;
                }
            }

            if (!hasValidStarlitItem && 486 < allSlotViews.size()) {
                IRecipeSlotView leftView = allSlotViews.get(486);
                if (!missingSlotViews.contains(leftView)) {
                    missingSlotViews.add(leftView);
                }
            }
        }

        if (!missingSlotViews.isEmpty()) {
            Component message = Component.translatable(
                    "jei.tooltip.error.recipe.transfer.missing");
            return handlerHelper.createUserErrorForMissingSlots(message, missingSlotViews);
        }

        if (!doTransfer) {
            return null;
        }

        NetworkHandler.sendToServer(new RecipeTransferPacket(recipe.getId()));
        return null;
    }

    private static class RequiredItem {
        final Ingredient ingredient;
        final int count;

        RequiredItem(Ingredient ingredient, int count) {
            this.ingredient = ingredient;
            this.count = count;
        }
    }
}