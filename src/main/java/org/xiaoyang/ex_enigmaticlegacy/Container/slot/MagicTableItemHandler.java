package org.xiaoyang.ex_enigmaticlegacy.Container.slot;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.xiaoyang.ex_enigmaticlegacy.Tile.TileMagicTable;

public class MagicTableItemHandler implements IItemHandler {
    private final TileMagicTable tile;

    public MagicTableItemHandler(TileMagicTable tile) {
        this.tile = tile;
    }

    @Override
    public int getSlots() {
        return 2;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (slot == 0) return tile.getInputDisplay();
        if (slot == 1) return tile.getOutputDisplay();
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (slot == 0) return tile.insertInput(stack, simulate);
        return stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot == 0) {
            if (simulate) return tile.peekInput(amount);
            return tile.extractInput(amount);
        }
        if (slot == 1) {
            if (simulate) return tile.peekOutput(amount);
            return tile.extractOutput(amount);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return slot == 0;
    }
}