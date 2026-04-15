package org.xiaoyang.ex_enigmaticlegacy.Container.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.xiaoyang.ex_enigmaticlegacy.Tile.TileMagicTable;

public class MagicTableInputSlot extends Slot {
    private final TileMagicTable tile;

    public MagicTableInputSlot(TileMagicTable tile, int x, int y) {
        super(new SyncableContainer(), 0, x, y);
        this.tile = tile;
    }

    @Override
    public @NotNull ItemStack getItem() {
        ItemStack display = tile.getInputDisplay();
        this.container.setItem(0, display);
        return display;
    }

    @Override
    public boolean hasItem() {
        return tile.getInputCount() > 0;
    }

    @Override
    public void set(@NotNull ItemStack stack) {
        this.container.setItem(0, stack);
        this.setChanged();
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        ItemStack result = tile.extractInput(amount);
        this.container.setItem(0, tile.getInputDisplay());
        return result;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
        this.setChanged();
    }

    @Override
    public void setChanged() {
        tile.setChanged();
    }

    @Override
    public boolean mayPickup(@NotNull Player player) {
        return tile.getInputCount() > 0;
    }
}