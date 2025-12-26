package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI.AvaritiaJei;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PacketRecipeTransfer;
import mezz.jei.common.transfer.BasicRecipeTransferHandler.InventoryState;
import mezz.jei.common.transfer.RecipeTransferHandlerHelper;
import mezz.jei.common.transfer.RecipeTransferOperationsResult;
import mezz.jei.common.transfer.RecipeTransferUtil;
import mezz.jei.common.util.StackHelper;
import mezz.jei.common.util.StringUtil;
import mezz.jei.forge.network.ConnectionToServer;
import morph.avaritia.api.ExtremeCraftingRecipe;
import morph.avaritia.recipe.ExtremeShapedRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ContainerExtremeAutoCrafter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class AvaTransferHandler implements IRecipeTransferHandler<ContainerExtremeAutoCrafter, ExtremeCraftingRecipe> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final IRecipeTransferHandlerHelper handlerHelper;
    private final IRecipeTransferInfo<ContainerExtremeAutoCrafter, ExtremeCraftingRecipe> transferInfo;

    public AvaTransferHandler() {
        this.transferInfo = new ExtremeAutoTransferInfo();
        this.handlerHelper = new RecipeTransferHandlerHelper();
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(ContainerExtremeAutoCrafter autoCrafter, ExtremeCraftingRecipe recipe, IRecipeSlotsView recipeSlotsView, Player player, boolean maxTransfer, boolean doTransfer) {
        if (this.transferInfo.canHandle(autoCrafter, recipe)) {
            List<Slot> craftingSlots = Collections.unmodifiableList(this.transferInfo.getRecipeSlots(autoCrafter, recipe));
            List<Slot> inventorySlots = Collections.unmodifiableList(this.transferInfo.getInventorySlots(autoCrafter, recipe));

            //配方有内容
            if (!recipe.getIngredients().isEmpty() && !recipe.getResultItem().isEmpty()){
                //清除上一配方
                for (int i = 81; i <= 161; ++i) {
                    autoCrafter.getSlot(i).set(ItemStack.EMPTY);
                }

                if (recipe instanceof ExtremeShapedRecipe shapedRecipe){ //有序合成列数可能不满
                    int width = shapedRecipe.getWidth();
                    if (width < 9){
                        NonNullList<Ingredient> ingredients = NonNullList.withSize(81, Ingredient.EMPTY);
                        for (int h = 0; h < 9; h++){
                            for (int w = 0; w < 9; w++){
                                int slot = h * 9 + w;//完整配方9*9格序数
                                int recipeSlot = h * width + w;
                                if (w >= width){
                                    ingredients.set(slot, Ingredient.EMPTY);
                                }else {
                                    ingredients.set(slot, recipe.getIngredients().get(recipeSlot));
                                }
                            }
                        }
                        setRecipe(shapedRecipe, autoCrafter, ingredients);
                    }else setRecipe(shapedRecipe, autoCrafter, shapedRecipe.getIngredients());
                }else setRecipe(recipe, autoCrafter, recipe.getIngredients());

                return null;
            }

            return this.handlerHelper.createInternalError();
        }

        return IRecipeTransferHandler.super.transferRecipe(autoCrafter, recipe, recipeSlotsView, player, maxTransfer, doTransfer);
    }

    /**
     * 设置无序配方
     */
    private static void setRecipe(ExtremeCraftingRecipe recipe, ContainerExtremeAutoCrafter autoCrafter, NonNullList<Ingredient> ingredients){
        int starSlot = 81;
        int endSlot = 161;
        int outSlot = 162;

        for (Ingredient ingredient : ingredients) {
            if (starSlot > endSlot) {
                System.out.println("Error Info:starSlot > endSlot");
                break;
            }
            if (ingredient.isEmpty()) autoCrafter.getSlot(starSlot).set(ItemStack.EMPTY);
            else {
                ItemStack stack = ingredient.getItems()[0];
                ItemStack copy = stack.copy();
                if (copy.getCount() > 1) copy.setCount(1);
                autoCrafter.getSlot(starSlot).set(copy); //输入设置
            }
            starSlot++;
        }

        ItemStack copy = recipe.getResultItem().copy();
        autoCrafter.getSlot(outSlot).set(copy);
    }

    @Override
    public @NotNull Class getContainerClass() {
        return ContainerExtremeAutoCrafter.class;
    }

    @Override
    public @NotNull Class getRecipeClass() {
        return ExtremeCraftingRecipe.class;
    }
}
