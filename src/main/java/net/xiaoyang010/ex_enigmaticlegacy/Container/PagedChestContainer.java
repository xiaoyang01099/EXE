package net.xiaoyang010.ex_enigmaticlegacy.Container;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import net.xiaoyang010.ex_enigmaticlegacy.Util.PagedSlot;

public class PagedChestContainer extends AbstractContainerMenu {
    private final Container container;
    private static final int PAGES = 5;
    private static final int SLOTS_PER_PAGE = 117;
    private static ContainerData pageData;

    public PagedChestContainer(int windowId, Inventory playerInventory, Container container) {
        super(ModMenus.PAGED_CHEST, windowId);
        this.container = container;

        // 初始化页面数据
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

        // 添加箱子槽位（第一页）
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 13; col++) {
                int index = col + (row * 13);
                this.addSlot(new PagedSlot(container,this, index, 12 + col * 18, 8 + row * 18));
            }
        }

        // 添加玩家物品栏槽位
        int playerInvY = 174;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 48 + col * 18, playerInvY + row * 18));
            }
        }

        // 添加玩家快捷栏槽位
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 48 + col * 18, 232));
        }

        this.addDataSlots(pageData);
    }

    private void updateSlots() {
        for (int i = 0; i < SLOTS_PER_PAGE; i++) {
            if (this.slots.get(i) instanceof PagedSlot pagedSlot) {
                //pagedSlot.setPage(pageData.get(0));
                pagedSlot.setChanged();
            }
        }
    }

    public void nextPage() {
        if (getCurrentPage() < (PAGES-1)) {
            //updateSlots();
            // NetworkHandler.CHANNEL.sendToServer(new PageChestPacket(getCurrentPage() + 1));
            pageData.set(0,getCurrentPage()+1);
        }
    }

    public void previousPage() {
        if (getCurrentPage() > 0) {
            //updateSlots();
            // NetworkHandler.CHANNEL.sendToServer(new PageChestPacket(getCurrentPage() - 1));
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
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index < SLOTS_PER_PAGE) {
                // 从箱子到玩家物品栏
                if (!this.moveItemStackTo(stackInSlot, SLOTS_PER_PAGE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 从玩家物品栏到箱子
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