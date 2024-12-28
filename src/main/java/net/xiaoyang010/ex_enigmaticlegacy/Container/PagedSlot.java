package net.xiaoyang010.ex_enigmaticlegacy.Container;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.PagedChestBlockEntity;

public class PagedSlot extends Slot {
    private final int baseIndex;
    private int currentPage;

    public PagedSlot(Container container, int index, int x, int y, int page) {
        super(container, index, x, y);
        this.baseIndex = index;
        this.currentPage = page;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setPage(int page) {
        this.currentPage = page;
    }

    @Override
    public int getSlotIndex() {
        return this.baseIndex + this.currentPage * 117;
    }

    @Override
    public int getContainerSlot() {
        return this.baseIndex + this.currentPage * 117;
    }

    @Override
    public ItemStack getItem() {
        return this.container.getItem(baseIndex + (currentPage * 117));
    }

    @Override
    public void set(ItemStack stack) {
        if (container instanceof PagedChestBlockEntity pagedChestBlock) {
//            if (pagedChestBlock.getLevel() != null && pagedChestBlock.getLevel().isClientSide) return;
//            int currentPage1 = ((PagedChestContainer) container).getCurrentPage();
            this.container.setItem(baseIndex + (currentPage * 117), stack); // Client page 为1 但 Server page为0
            this.setChanged();
        }
    }

    @Override
    public ItemStack remove(int amount) {
        return this.container.removeItem(baseIndex + (currentPage * 117), amount);
    }
}