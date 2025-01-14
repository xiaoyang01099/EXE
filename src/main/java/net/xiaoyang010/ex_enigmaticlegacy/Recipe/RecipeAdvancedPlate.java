package net.xiaoyang010.ex_enigmaticlegacy.Recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeAdvancedPlate {
    private final ItemStack output;
    private final int color;
    private final List<ItemStack> inputs;
    private final int mana;

    public RecipeAdvancedPlate(ItemStack output, int mana, int color, ItemStack... inputs) {
        this.output = output;
        this.mana = mana;
        this.color = color;
        this.inputs = new ArrayList<>(Arrays.asList(inputs));
    }

    public List<ItemStack> getInputs() {
        return new ArrayList<>(this.inputs);
    }

    public ItemStack getOutput() {
        return this.output.copy();
    }

    public int getManaUsage() {
        return this.mana;
    }

    public int getColor() {
        return this.color;
    }

    public boolean matches(Container inv) {
        List<ItemStack> inputsMissing = new ArrayList<>(this.inputs);

        for (int i = 1; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                break;
            }

            int stackIndex = -1;
            for (int j = 0; j < inputsMissing.size(); j++) {
                ItemStack input = inputsMissing.get(j);
                if (simpleAreStacksEqual(input.copy(), stack)) {
                    stackIndex = j;
                    break;
                }
            }

            if (stackIndex == -1) {
                return false;
            }
            inputsMissing.remove(stackIndex);
        }

        return inputsMissing.isEmpty();
    }

    private boolean simpleAreStacksEqual(ItemStack input, ItemStack stack) {
        if (input.getDamageValue() == 32767) {
            input.setDamageValue(stack.getDamageValue());
        }
        return input.getItem() == stack.getItem() &&
                (input.getDamageValue() == stack.getDamageValue() || input.getDamageValue() == 32767);
    }
}