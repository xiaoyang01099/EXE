package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud.TChest;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.TalismanHiddenRiches;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import vazkii.botania.client.gui.SlotLocked;

public class ContainerItemChest extends AbstractContainerMenu {
    public InventoryItemChest itemChestInv;
    private int numRows;
    private final Player player;

    public ContainerItemChest(int windowId, Inventory playerInventory, Player player) {
        super(ModMenus.TALISMAN_CHEST, windowId);
        this.player = player;
        int slot = playerInventory.selected;
        this.itemChestInv = new InventoryItemChest(player, slot,
                TalismanHiddenRiches.getOpenChest(player.getMainHandItem()));
        this.numRows = this.itemChestInv.getContainerSize() / 9;

        for(int j = 0; j < this.numRows; ++j) {
            for(int k = 0; k < 9; ++k) {
                int slotIndex = k + j * 9;
                this.addSlot(new SlotItemChest(this.itemChestInv, slotIndex, 8 + k * 18, 18 + j * 18, k));
            }
        }

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 85 + i * 18));
            }
        }

        for(int i = 0; i < 9; ++i) {
            if (playerInventory.selected == i) {
                this.addSlot(new SlotLocked(playerInventory, i, 8 + i * 18, 143));
            } else {
                this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 143));
            }
        }
    }

    public ContainerItemChest(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, playerInventory.player);
    }

    @Override
    public boolean stillValid(Player player) {
        boolean valid = this.itemChestInv.stillValid(player);
        if (!valid) {
            this.removed(player);
        }
        return valid;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.itemChestInv.pushInventory();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (InventoryItemChest.isRelicTalisman(slotStack)) {
                this.itemChestInv.pushInventory();
                return ItemStack.EMPTY;
            }

            int chestSlots = this.itemChestInv.getContainerSize();

            if (index < chestSlots) {
                if (!this.moveItemStackTo(slotStack, chestSlots, this.slots.size(), true)) {
                    this.itemChestInv.pushInventory();
                    return ItemStack.EMPTY;
                }
            }

            else if (!this.moveItemStackTo(slotStack, 0, chestSlots, false)) {
                this.itemChestInv.pushInventory();
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemstack;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (this.itemChestInv != null) {
            this.itemChestInv.setChanged();
        }
    }
}