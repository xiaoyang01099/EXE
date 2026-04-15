package org.xiaoyang.ex_enigmaticlegacy.Container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TrashSlot extends Slot {
    public TrashSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return true;
    }

    @Override
    public void set(ItemStack stack) {
        super.set(ItemStack.EMPTY);
        this.setChanged();
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }
}