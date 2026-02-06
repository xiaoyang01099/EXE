package net.xiaoyang010.ex_enigmaticlegacy.Container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Container.PagedChestContainer;

public class PagedSlot extends Slot {
    private  final PagedChestContainer pagedChestContainer;
    private final int baseSlot;
    private int page;
    private static final int SLOTS_PER_PAGE = 117;

    public PagedSlot(Container container,PagedChestContainer pagedChestContainer, int slotIndex, int x, int y) {
        super(container, slotIndex + (SLOTS_PER_PAGE), x, y);
        this.pagedChestContainer = pagedChestContainer;
        this.baseSlot = slotIndex;
    }

    private int getActualSlot() {
            return baseSlot + ((pagedChestContainer.getCurrentPage()) * SLOTS_PER_PAGE);
    }

    @Override
    public ItemStack getItem() {
        return this.container.getItem(getActualSlot());
    }

    @Override
    public void set(ItemStack stack) {
        this.container.setItem(getActualSlot(), stack);
        this.setChanged();
    }

    @Override
    public ItemStack remove(int amount) {
        return this.container.removeItem(getActualSlot(), amount);
    }

    @Override
    public int getSlotIndex() {
        return getActualSlot();
    }

    @Override
    public int getContainerSlot() {
        return getActualSlot();
    }
}