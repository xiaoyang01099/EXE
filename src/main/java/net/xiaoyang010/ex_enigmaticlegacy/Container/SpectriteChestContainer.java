package net.xiaoyang010.ex_enigmaticlegacy.Container;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTags;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.SpectriteChestTile;

public class SpectriteChestContainer extends ChestMenu {
    private final Container container;
    private final SpectriteChestTile blockEntity;

    public SpectriteChestContainer(int windowId, Inventory playerInventory, SpectriteChestTile blockEntity) {
        super(MenuType.GENERIC_9x3, windowId, playerInventory, blockEntity, 3);
        this.container = blockEntity;
        this.blockEntity = blockEntity;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9;
                Slot oldSlot = this.slots.get(index);
                this.slots.set(index, new FilteredSlot(blockEntity, oldSlot.getSlotIndex(), oldSlot.x, oldSlot.y));
            }
        }
    }

    public SpectriteChestContainer(int windowId, Inventory playerInventory, Container container) {
        super(MenuType.GENERIC_9x6, windowId, playerInventory, container, 6);
        this.container = container;
        this.blockEntity = null;

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9;
                Slot oldSlot = this.slots.get(index);
                this.slots.set(index, new FilteredSlot(container, oldSlot.getSlotIndex(), oldSlot.x, oldSlot.y));
            }
        }
    }

    private static class FilteredSlot extends Slot {
        public FilteredSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(ModTags.Items.SPECTRITE_ITEMS);
        }
    }
}