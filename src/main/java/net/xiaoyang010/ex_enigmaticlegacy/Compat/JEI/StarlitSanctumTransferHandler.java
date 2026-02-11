package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.xiaoyang010.ex_enigmaticlegacy.Container.StarlitSanctumMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.RecipeTransferPacket;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.StarlitSanctumRecipe;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.StarlitSanctumTile;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class StarlitSanctumTransferHandler implements IRecipeTransferHandler<StarlitSanctumMenu, StarlitSanctumRecipe> {

    private final IRecipeTransferHandlerHelper handlerHelper;

    public StarlitSanctumTransferHandler(IRecipeTransferHandlerHelper handlerHelper) {
        this.handlerHelper = handlerHelper;
    }

    @Override
    public Class<StarlitSanctumMenu> getContainerClass() {
        return StarlitSanctumMenu.class;
    }

    @Override
    public Class<StarlitSanctumRecipe> getRecipeClass() {
        return StarlitSanctumRecipe.class;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(StarlitSanctumMenu container,
                                                         StarlitSanctumRecipe recipe,
                                                         IRecipeSlotsView recipeSlots,
                                                         Player player,
                                                         boolean maxTransfer,
                                                         boolean doTransfer) {
        Map<Integer, RequiredItem> slotRequirements = new HashMap<>();

        var patternGroups = recipe.getPatternGroups();
        int[] blockStarts = {0, 9, 18};

        for (int blockIndex = 0; blockIndex < 3; blockIndex++) {
            var pattern = patternGroups.get(blockIndex);
            int startCol = blockStarts[blockIndex];

            for (int row = 0; row < 18; row++) {
                for (int col = 0; col < 9; col++) {
                    int patternIndex = row * 9 + col;
                    int slotIndex = row * 27 + (startCol + col);

                    if (patternIndex < pattern.size()) {
                        var ingredient = pattern.get(patternIndex);
                        if (!ingredient.isEmpty()) {
                            slotRequirements.put(slotIndex, new RequiredItem(ingredient, 1));
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

        Map<Ingredient, Integer> availableItems = new HashMap<>();
        for (ItemStack stack : player.getInventory().items) {
            for (RequiredItem req : slotRequirements.values()) {
                if (req.ingredient.test(stack)) {
                    availableItems.merge(req.ingredient, stack.getCount(), Integer::sum);
                }
            }
        }

        List<Integer> missingSlots = new ArrayList<>();
        Map<Integer, Integer> missingCounts = new HashMap<>();

        for (Map.Entry<Integer, RequiredItem> entry : slotRequirements.entrySet()) {
            int slotIndex = entry.getKey();
            RequiredItem required = entry.getValue();
            int available = availableItems.getOrDefault(required.ingredient, 0);

            if (available < required.count) {
                missingSlots.add(slotIndex);
                missingCounts.put(slotIndex, required.count - available);
            }
        }

        if (!recipe.getLeftInput().isEmpty()) {
            boolean hasValidStarlitItem = false;
            for (ItemStack stack : player.getInventory().items) {
                if (recipe.getLeftInput().test(stack) && stack.is(StarlitSanctumTile.STARLIT)) {
                    hasValidStarlitItem = true;
                    break;
                }
            }
            if (!hasValidStarlitItem && !missingSlots.contains(486)) {
                missingSlots.add(486);
            }
        }

        if (!missingSlots.isEmpty()) {
            Component message = EComponent.translatable("jei.tooltip.error.recipe.transfer.missing");
            return handlerHelper.createUserErrorForSlots(message, missingSlots);
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