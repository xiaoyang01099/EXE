package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud.TChest;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotItemChest extends Slot {
    InventoryItemChest itemChestInv;
    int actualSlotIndex;

    public SlotItemChest(InventoryItemChest inv, int index, int x, int y, int displaySlotIndex) {
        super(inv, index, x, y);
        this.itemChestInv = inv;
        this.actualSlotIndex = index;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.itemChestInv.setChanged();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !InventoryItemChest.isRelicTalisman(stack);
    }

    @Override
    public void set(ItemStack stack) {
        this.itemChestInv.setItem(this.actualSlotIndex, stack);
        this.setChanged();
    }

    @Override
    public ItemStack getItem() {
        return this.itemChestInv.getItem(this.actualSlotIndex);
    }

    @Override
    public ItemStack remove(int amount) {
        ItemStack result = this.itemChestInv.removeItem(this.actualSlotIndex, amount);
        this.setChanged();
        return result;
    }

    @Override
    public int getMaxStackSize() {
        return this.itemChestInv.getMaxStackSize();
    }

    @Override
    public boolean allowModification(Player player) {
        return this.itemChestInv.stillValid(player);
    }

    @Override
    public boolean mayPickup(Player player) {
        return this.itemChestInv.stillValid(player);
    }

    @Override
    public boolean hasItem() {
        return !this.getItem().isEmpty();
    }
}