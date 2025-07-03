package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotItemChest extends Slot {

    private final InventoryItemChest itemChestInv;
    private final int slot;

    public SlotItemChest(InventoryItemChest inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.itemChestInv = inventory;
        this.slot = index;
    }

    @Override
    public void setChanged() {
        this.itemChestInv.setItem(this.slot, this.getItem());
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !InventoryItemChest.isRelicTalisman(stack);
    }
}