package net.xiaoyang010.ex_enigmaticlegacy.Container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SyncableContainer implements Container {

    private ItemStack stored = ItemStack.EMPTY;

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return stored.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? stored : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0 || stored.isEmpty()) return ItemStack.EMPTY;
        return stored.split(amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0) return ItemStack.EMPTY;
        ItemStack old = stored;
        stored = ItemStack.EMPTY;
        return old;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            this.stored = stack;
        }
    }

    @Override
    public int getMaxStackSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.stored = ItemStack.EMPTY;
    }
}