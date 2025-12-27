package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI.AvaritiaJei;

import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import morph.avaritia.api.ExtremeCraftingRecipe;
import morph.avaritia.init.AvaritiaModContent;
import net.covers1624.quack.util.SneakyUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ContainerExtremeAutoCrafter;

import java.util.List;

public class ExtremeAutoTransferInfo implements IRecipeTransferInfo<ContainerExtremeAutoCrafter, ExtremeCraftingRecipe> {
    public ExtremeAutoTransferInfo() {
    }

    public Class<ContainerExtremeAutoCrafter> getContainerClass() {
        return ContainerExtremeAutoCrafter.class;
    }

    public RecipeType<ExtremeCraftingRecipe> getRecipeType() {
        return new RecipeType<>(AvaritiaModContent.EXTREME_CRAFTING_RECIPE_TYPE.getId(), ExtremeCraftingRecipe.class);
    }

    public boolean canHandle(ContainerExtremeAutoCrafter container, ExtremeCraftingRecipe recipe) {
        return true;
    }

    public List<Slot> getRecipeSlots(ContainerExtremeAutoCrafter container, ExtremeCraftingRecipe recipe) {
        return container.slots.stream().filter((s) -> s.index >= 81 && s.index <= 161).toList();
    }

    public List<Slot> getInventorySlots(ContainerExtremeAutoCrafter container, ExtremeCraftingRecipe recipe) {
        return container.slots.stream().filter((s) -> s.index >= 163).toList();
    }

    public Class<ExtremeCraftingRecipe> getRecipeClass() {
        return  SneakyUtils.unsafeCast(this.getRecipeType().getRecipeClass());
    }

    public ResourceLocation getRecipeCategoryUid() {
        return this.getRecipeType().getUid();
    }
}
