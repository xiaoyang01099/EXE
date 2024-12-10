package net.xiaoyang010.ex_enigmaticlegacy.Recipe;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.Item.RegenIvy;

public class RegenIvyRecipe implements Recipe<CraftingContainer> {
    public static final RecipeSerializer<RegenIvyRecipe> SERIALIZER = new SimpleRecipeSerializer<>(RegenIvyRecipe::new);
    private final ResourceLocation id;

    public RegenIvyRecipe(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        ItemStack tool = ItemStack.EMPTY;
        boolean foundIvy = false;
        int materialsFound = 0;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == ModItems.IVYREGEN.get()) {
                    foundIvy = true;
                } else if (tool.isEmpty()
                        && !(stack.hasTag() && stack.getTag().getBoolean(RegenIvy.TAG_REGEN))
                        && stack.isDamageableItem()) {
                    tool = stack;
                }
            }
        }

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (materialsFound < 3 && !tool.isEmpty() && canRepairItem(tool, stack)) {
                    materialsFound++;
                }
            } else if (!stack.isEmpty() && stack != tool && stack.getItem() != ModItems.IVYREGEN.get()) {
                return false;
            }
        }

        return foundIvy && !tool.isEmpty() && materialsFound == 3;
    }

    private boolean canRepairItem(ItemStack tool, ItemStack material) {
        return tool.getItem().isValidRepairItem(tool, material);
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        ItemStack item = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                item = stack;
            }
        }

        ItemStack copy = item.copy();
        CompoundTag tag = copy.getOrCreateTag();
        tag.putBoolean(RegenIvy.TAG_REGEN, true);
        copy.setCount(1);

        return copy;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 5;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }
}