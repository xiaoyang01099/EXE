package net.xiaoyang010.ex_enigmaticlegacy.Container;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Container.slot.PagedSlot;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;

public class PagedChestContainer extends AbstractContainerMenu {
    private final Container container;
    private static final int PAGES = 5;
    private static final int SLOTS_PER_PAGE = 117;
    private static ContainerData pageData;

    public PagedChestContainer(int windowId, Inventory playerInventory) {
        this(windowId, playerInventory, new SimpleContainer(585), new SimpleContainerData(1));
    }

    public PagedChestContainer(int windowId, Inventory playerInventory, Container container) {
        this(windowId, playerInventory, container, new SimpleContainerData(1));
    }

    public PagedChestContainer(int windowId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenus.PAGED_CHEST, windowId);
        this.container = container;
        checkContainerSize(container, 585);
        container.startOpen(playerInventory.player);

        this.pageData = new ContainerData() {
            private int page;

            @Override
            public int get(int index) {
                return page;
            }

            @Override
            public void set(int index, int value) {
                if (value >= 0 && value < PAGES && page != value) {
                    page = value;
                    updateSlots();
                }
            }

            @Override
            public int getCount() {
                return 1;
            }
        };

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 13; col++) {
                int index = col + (row * 13);
                this.addSlot(new PagedSlot(container,this, index, 12 + col * 18, 8 + row * 18));
            }
        }

        int playerInvY = 174;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 48 + col * 18, playerInvY + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 48 + col * 18, 232));
        }

        this.addDataSlots(pageData);
    }

    private void updateSlots() {
        for (int i = 0; i < SLOTS_PER_PAGE; i++) {
            if (this.slots.get(i) instanceof PagedSlot pagedSlot) {
                pagedSlot.setChanged();
            }
        }
    }

    public void nextPage() {
        if (getCurrentPage() < (PAGES-1)) {
            pageData.set(0,getCurrentPage()+1);
        }
    }

    public void previousPage() {
        if (getCurrentPage() > 0) {
            pageData.set(0,getCurrentPage()-1);
        }
    }

    public void setPages(int page) {
        this.pageData.set(0, page);
    }

    public int getCurrentPage() {
        return pageData.get(0);
    }

    public int getTotalPages() {
        return PAGES;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index < SLOTS_PER_PAGE) {
                if (!this.moveItemStackTo(stackInSlot, SLOTS_PER_PAGE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stackInSlot, 0, SLOTS_PER_PAGE, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    public Container getContainer() {
        return container;
    }
}