package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Container.PagedChestContainer;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;

public class PagedChestBlockEntity extends BlockEntity implements MenuProvider, Container {
    // 5页，每页117个槽位（9x13）
    private static final int TOTAL_SLOTS = 585;  // 117 * 5
    private NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    public PagedChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PAGED_CHEST.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("block.ex_enigmaticlegacy.paged_chest");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new PagedChestContainer(windowId, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return TOTAL_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot >= 0 && slot < TOTAL_SLOTS) {
            return items.get(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot >= 0 && slot < TOTAL_SLOTS) {
            ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
            if (!stack.isEmpty()) {
                setChanged();
            }
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot >= 0 && slot < TOTAL_SLOTS) {
            return ContainerHelper.takeItem(items, slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < TOTAL_SLOTS) {
            items.set(slot, stack);
            setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.level != null &&
                this.level.getBlockEntity(this.worldPosition) == this &&
                player.distanceToSqr(this.worldPosition.getX() + 0.5D,
                        this.worldPosition.getY() + 0.5D,
                        this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    public void dropContents(Level level, BlockPos pos) {
        Containers.dropContents(level, pos, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
    }
}