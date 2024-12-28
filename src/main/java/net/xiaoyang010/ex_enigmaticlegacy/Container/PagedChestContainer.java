package net.xiaoyang010.ex_enigmaticlegacy.Container;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.PageChestPacket;

public class PagedChestContainer extends AbstractContainerMenu {
    private final Container container;
//    private int currentPage = 0;
    private static final int PAGES = 5;
    private static final int SLOTS_PER_PAGE = 117; // 9 rows * 13 columns
    private final Player player;
    private DatePage datePage = new DatePage();

    public PagedChestContainer(int windowId, Inventory playerInventory, Container container) {
        super(ModMenus.PAGED_CHEST, windowId);
        this.container = container;
        this.player = playerInventory.player;


//        // 为当前页面添加宝箱库存槽
//        int containerRows = 9;
//        for (int row = 0; row < containerRows; row++) {
//            for (int col = 0; col < 13; col++) {
//                int index = col + row * 13 + (currentPage * SLOTS_PER_PAGE);
//                this.addSlot(new PagedSlot(container, index, 12 + col * 18, 8 + row * 18, currentPage));
//            }
//        }
        datePage.set(0, 0);
        addSlot(datePage.get(0));

        // 添加玩家物品栏槽
        int playerInvY = 174;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 48 + col * 18, playerInvY + row * 18));
            }
        }

        // 添加玩家热键栏槽位
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 48 + col * 18, 232));
        }

        this.addDataSlots(datePage);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    private void addSlot(int page){
        // 为当前页面添加宝箱库存槽
        int containerRows = 9;
        for (int row = 0; row < containerRows; row++) {
            for (int col = 0; col < 13; col++) {
                int index = col + row * 13 ;//+ (page * SLOTS_PER_PAGE);
                this.addSlot(new PagedSlot(container, index, 12 + col * 18, 8 + row * 18, page));
            }
        }
    }

    public void nextPage() {
        if (datePage.get(0) < PAGES) {
            NetworkHandler.CHANNEL.sendToServer(new PageChestPacket(datePage.get(0) + 1));
            updateSlots();
        }
    }

    public void previousPage() {
        if (datePage.get(0) > 0) {
            NetworkHandler.CHANNEL.sendToServer(new PageChestPacket(datePage.get(0) - 1));
            updateSlots();
        }
    }

    public void setPages(int page) {
        this.datePage.set(0, page);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
    }

    private void updateSlots() {
//        if (!player.level.isClientSide) return;
        for (int i = 0; i < SLOTS_PER_PAGE; i++) {
            PagedSlot slot = (PagedSlot) this.slots.get(i);
            slot.setPage(datePage.get(0));
        }
//        addSlot(datePage.get(0));
        this.broadcastChanges();
    }

    public int getCurrentPage() {
        return datePage.get(0);
    }

    public int getTotalPages() {
        return PAGES;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < SLOTS_PER_PAGE) {
                if (!this.moveItemStackTo(itemstack1, SLOTS_PER_PAGE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, SLOTS_PER_PAGE, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    private static class DatePage implements ContainerData {
        private int page;

        @Override
        public int get(int i) {
            return page;
        }

        @Override
        public void set(int i, int i1) {
            this.page = i1;
        }

        @Override
        public int getCount() {
            return 1;
        }
    }
}