package net.xiaoyang010.ex_enigmaticlegacy.Recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;

import static net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes.MANAITA;

public class ManaitaRecipe implements CraftingRecipe {
    private final ResourceLocation id;

    public ManaitaRecipe(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        boolean source = false;
        boolean item = false;

        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack itemStack = container.getItem(i);
            if (!itemStack.isEmpty()) {
                if (itemStack.getItem() == ModItems.MANAITA.get()) {
                    if (!source) {
                        source = true;
                    } else {
                        if (item) {
                            return false;
                        }
                        item = true;
                    }
                } else {
                    if (item) {
                        return false;
                    }
                    item = true;
                }
            }
        }
        return source && item;
    }

    @Override
    public ItemStack assemble(CraftingContainer container) {
        ItemStack itemStack = ItemStack.EMPTY;
        int source = 0;

        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack itemStackInSlot = container.getItem(i);
            if (!itemStackInSlot.isEmpty() && itemStackInSlot.getItem() != ModItems.MANAITA.get()) {
                itemStack = itemStackInSlot;
            }
            if (!itemStackInSlot.isEmpty() && itemStackInSlot.getItem() == ModItems.MANAITA.get()) {
                ++source;
            }
        }

        if (source == 2) {
            ItemStack result = new ItemStack(ModItems.MANAITA.get());
            result.setCount(64);
            return result;
        } else if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack result = itemStack.copy();
            result.setCount(64);
            return result;
        }
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MANAITA.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}