package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.TalismanHiddenRiches;
import vazkii.botania.client.gui.SlotLocked;

public class ItemChestContainer extends AbstractContainerMenu {

    final InventoryItemChest itemChestInv;
    private final int numRows;

    public ItemChestContainer(int windowId, Inventory playerInventory, Player player) {
        super(getMenuType(), windowId);

        ItemStack stack = player.getInventory().getSelected();
        this.itemChestInv = new InventoryItemChest(player, TalismanHiddenRiches.getOpenChest(stack), 0);
        this.numRows = this.itemChestInv.getContainerSize() / 9;

        // Add chest slots
        for (int i = 0; i < this.numRows; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new SlotItemChest(this.itemChestInv, j + i * 9, 8 + j * 18, 18 + i * 18));
            }
        }

        // Add player inventory slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 103 + i * 18 + this.numRows * 18 - 18));
            }
        }

        // Add hotbar slots
        for (int k = 0; k < 9; ++k) {
            if (playerInventory.selected == k) {
                this.addSlot(new SlotLocked(playerInventory, k, 8 + k * 18, 161 + this.numRows * 18 - 18));
            } else {
                this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 161 + this.numRows * 18 - 18));
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.itemChestInv.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.itemChestInv.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < this.numRows * 9) {
                if (!this.moveItemStackTo(itemstack1, this.numRows * 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, this.numRows * 9, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    private static MenuType<?> getMenuType() {
        return null;
    }
}