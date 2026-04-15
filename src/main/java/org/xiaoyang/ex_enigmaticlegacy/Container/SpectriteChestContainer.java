package org.xiaoyang.ex_enigmaticlegacy.Container;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModMenus;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModTags;
import org.xiaoyang.ex_enigmaticlegacy.Tile.SpectriteChestTile;
import org.xiaoyang.ex_enigmaticlegacy.Util.ColorText;

public class SpectriteChestContainer extends AbstractContainerMenu {
    private final SpectriteChestTile blockEntity;
    private final Player player;
    public static final int CHEST_SLOTS = SpectriteChestTile.SLOTS_PER_PAGE;
    private static final int PLAYER_INV_SLOTS = 36;

    public SpectriteChestContainer(int windowId, Inventory playerInventory, SpectriteChestTile blockEntity) {
        super(ModMenus.SPECTRITE_CHEST_MENU.get(), windowId);
        this.blockEntity = blockEntity;
        this.player = playerInventory.player;
        blockEntity.startOpen(playerInventory.player);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9;
                this.addSlot(new FilteredSlot(
                        blockEntity,
                        slotIndex,
                        8 + col * 18,
                        18 + row * 18,
                        player));
            }
        }

        int playerInvOffsetY = (3 - 4) * 18;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        103 + row * 18 + playerInvOffsetY));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(
                    playerInventory,
                    col,
                    8 + col * 18,
                    161 + playerInvOffsetY));
        }
    }

    public SpectriteChestTile getBlockEntity() {
        return blockEntity;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.blockEntity.stopOpen(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = this.slots.get(index);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyBeforeMove = sourceStack.copy();

        if (index < CHEST_SLOTS) {
            if (!moveToPlayerInventory(sourceStack)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!sourceStack.is(ModTags.Items.SPECTRITE_ITEMS)) {
                if (!player.level().isClientSide) {
                    player.displayClientMessage(
                            Component.nullToEmpty(
                                    ColorText.GetColor1(
                                            I18n.get("msg.ex_enigmaticlegacy.container_not_allowed"))),
                            true);
                }
                return ItemStack.EMPTY;
            }
            if (!moveToChest(sourceStack)) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        if (sourceStack.getCount() == copyBeforeMove.getCount()) {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(player, sourceStack);
        return copyBeforeMove;
    }

    private boolean moveToChest(ItemStack stack) {
        int originalCount = stack.getCount();

        for (int i = 0; i < CHEST_SLOTS; i++) {
            Slot slot = this.slots.get(i);
            if (!slot.hasItem()) continue;
            ItemStack slotStack = slot.getItem();

            if (ItemStack.isSameItemSameTags(slotStack, stack) && slot.mayPlace(stack)) {
                int limit = SpectriteChestTile.MAX_STACK_SIZE;
                int canAdd = limit - slotStack.getCount();
                if (canAdd > 0) {
                    int toAdd = Math.min(canAdd, stack.getCount());
                    slotStack.grow(toAdd);
                    stack.shrink(toAdd);
                    slot.setChanged();
                    if (stack.isEmpty()) return true;
                }
            }
        }

        for (int i = 0; i < CHEST_SLOTS; i++) {
            Slot slot = this.slots.get(i);
            if (slot.hasItem()) continue;
            if (!slot.mayPlace(stack)) continue;

            int toPlace = Math.min(SpectriteChestTile.MAX_STACK_SIZE, stack.getCount());
            slot.set(stack.copyWithCount(toPlace));
            stack.shrink(toPlace);
            slot.setChanged();
            if (stack.isEmpty()) return true;
        }

        return stack.getCount() < originalCount;
    }

    private boolean moveToPlayerInventory(ItemStack stack) {
        int startIndex = CHEST_SLOTS;
        int endIndex = CHEST_SLOTS + PLAYER_INV_SLOTS;
        int originalCount = stack.getCount();

        for (int i = startIndex; i < endIndex; i++) {
            Slot slot = this.slots.get(i);
            if (!slot.hasItem()) continue;
            ItemStack slotStack = slot.getItem();

            if (ItemStack.isSameItemSameTags(slotStack, stack)) {
                int limit = stack.getMaxStackSize();
                int canAdd = limit - slotStack.getCount();
                if (canAdd > 0) {
                    int toAdd = Math.min(canAdd, stack.getCount());
                    slotStack.grow(toAdd);
                    stack.shrink(toAdd);
                    slot.setChanged();
                    if (stack.isEmpty()) return true;
                }
            }
        }

        for (int i = startIndex; i < endIndex; i++) {
            Slot slot = this.slots.get(i);
            if (slot.hasItem()) continue;

            int limit = stack.getMaxStackSize();
            int toPlace = Math.min(limit, stack.getCount());
            slot.set(stack.copyWithCount(toPlace));
            stack.shrink(toPlace);
            slot.setChanged();
            if (stack.isEmpty()) return true;
        }

        return stack.getCount() < originalCount;
    }

    private static class FilteredSlot extends Slot {
        private final Player player;

        public FilteredSlot(SpectriteChestTile container, int index, int x, int y, Player player) {
            super(container, index, x, y);
            this.player = player;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(ModTags.Items.SPECTRITE_ITEMS);
        }

        @Override
        public int getMaxStackSize() {
            return SpectriteChestTile.MAX_STACK_SIZE;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return SpectriteChestTile.MAX_STACK_SIZE;
        }
    }
}