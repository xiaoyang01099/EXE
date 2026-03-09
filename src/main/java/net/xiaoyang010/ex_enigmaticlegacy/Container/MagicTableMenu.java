package net.xiaoyang010.ex_enigmaticlegacy.Container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileMagicTable;

public class MagicTableMenu extends AbstractContainerMenu {
    private final TileMagicTable blockEntity;
    private final ContainerLevelAccess access;
    public int progress = 0;
    public int progressMax = 200;

    public MagicTableMenu(int containerId, Inventory playerInventory, TileMagicTable blockEntity) {
        super(ModMenus.MAGIC_TABLE_MENU, containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        IItemHandler handler = blockEntity.getItemHandler();
        this.addSlot(new SlotItemHandler(handler, 0, 79, 83));
        this.addSlot(new SlotItemHandler(handler, 1, 161, 83){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addPlayerInventory(playerInventory);

        this.addDataSlot(new DataSlot() {
            @Override public int get() {
                return blockEntity.getProgress();
            }
            @Override public void set(int value) {
                progress = value;
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override public int get() {
                return blockEntity.getProgressMax();
            }
            @Override public void set(int value) {
                progressMax = value;
            }
        });
    }

    public MagicTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory,
                (TileMagicTable) playerInventory.player.level
                        .getBlockEntity(extraData.readBlockPos()));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player,
            ModBlockss.MAGIC_TABLE.get());
    }

    public int getProgressScaled() {
        if (progressMax <= 0) return 0;
        return progress * 42 / progressMax;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory,
                        col + row * 9 + 9,
                        50 + col * 18, 152 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col,
                    50 + col * 18, 210));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack    = slot.getItem();
        result             = stack.copy();

        if (index >= 0 && index <= 1) {
            if (!this.moveItemStackTo(stack, 2, 29, false)) {
                if (!this.moveItemStackTo(stack, 29, 38, false)) {
                    return ItemStack.EMPTY;
                }
            }

        } else if (index >= 2 && index <= 37) {
            if (!this.moveItemStackTo(stack, 1, 2, false)) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    if (index >= 2 && index <= 28) {
                        if (!this.moveItemStackTo(stack, 29, 38, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.moveItemStackTo(stack, 2, 29, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return result;
    }
}