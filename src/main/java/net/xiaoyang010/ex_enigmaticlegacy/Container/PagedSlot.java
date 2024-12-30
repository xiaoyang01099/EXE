package net.xiaoyang010.ex_enigmaticlegacy.Container;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PagedSlot extends Slot {
    // 在当前页面内的实际槽位索引
    private final int baseSlot;
    // 当前页码
    private int page;
    // 每页的槽位数
    private static final int SLOTS_PER_PAGE = 117;

    public PagedSlot(Container container, int slotIndex, int x, int y, int page) {
        // 调用父类构造函数，传入真实的槽位索引
        super(container, slotIndex + (page * SLOTS_PER_PAGE), x, y);
        this.baseSlot = slotIndex;
        this.page = page;
    }

    // 计算当前实际的槽位索引
    private int getActualSlot() {
        return baseSlot + (page * SLOTS_PER_PAGE);
    }

    // 重写getItem以确保从正确的页面获取物品
    @Override
    public ItemStack getItem() {
        return this.container.getItem(getActualSlot());
    }

    // 重写set以确保物品放在正确的页面
    @Override
    public void set(ItemStack stack) {
        this.container.setItem(getActualSlot(), stack);
        this.setChanged();
    }

    // 重写remove以确保从正确的页面移除物品
    @Override
    public ItemStack remove(int amount) {
        return this.container.removeItem(getActualSlot(), amount);
    }

    // 重写getSlotIndex以返回真实的槽位索引
    @Override
    public int getSlotIndex() {
        return getActualSlot();
    }

    // 重写getContainerSlot以返回真实的容器槽位索引
    @Override
    public int getContainerSlot() {
        return getActualSlot();
    }

    // 设置页码并更新槽位索引
    public void setPage(int newPage) {
        this.page = newPage;
    }
}