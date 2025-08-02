package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud.TChest;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.TalismanHiddenRiches;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;

public class InventoryItemChest implements Container {
    Player player;
    int slot;
    ItemStack[] stacks = null;
    boolean invPushed = false;
    ItemStack storedInv = null;
    int openChest;
    boolean needsRefresh = false;

    public InventoryItemChest(Player player, int slot, int openChest) {
        this.player = player;
        this.slot = slot;
        this.openChest = openChest;
    }

    public static boolean isRelicTalisman(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == ModItems.TALISMAN_HIDDEN_RICHES.get();
    }

    protected ItemStack getStack() {
        ItemStack stack = this.player.getInventory().getItem(this.slot);
        if (!stack.isEmpty()) {
            this.storedInv = stack;
        }
        return stack;
    }

    protected ItemStack[] getInventory() {
        if (this.stacks == null || needsRefresh) {
            ItemStack stack = this.getStack();
            if (isRelicTalisman(stack)) {
                this.stacks = TalismanHiddenRiches.getChestLoot(stack, this.openChest);
                if (this.stacks == null) {
                    this.stacks = new ItemStack[this.getContainerSize()];
                    for (int i = 0; i < this.stacks.length; i++) {
                        this.stacks[i] = ItemStack.EMPTY;
                    }
                }
            } else {
                this.stacks = new ItemStack[this.getContainerSize()];
                for (int i = 0; i < this.stacks.length; i++) {
                    this.stacks[i] = ItemStack.EMPTY;
                }
            }
            needsRefresh = false;
        }
        return this.stacks;
    }

    public void pushInventory() {
        if (!this.invPushed) {
            ItemStack stack = this.getStack();
            if (stack.isEmpty()) {
                stack = this.storedInv;
            }

            if (!stack.isEmpty()) {
                ItemStack[] inv = this.getInventory();
                TalismanHiddenRiches.setChestLoot(stack, inv, this.openChest);
                TalismanHiddenRiches.setOpenChest(stack, -1);
            }

            this.invPushed = true;
        }
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    public ItemStack getItem(int index) {
        if (index < 0 || index >= getContainerSize()) {
            return ItemStack.EMPTY;
        }

        ItemStack[] inventory = this.getInventory();
        ItemStack stack = inventory[index];
        return stack != null ? stack : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (index < 0 || index >= getContainerSize()) {
            return ItemStack.EMPTY;
        }

        ItemStack[] inventorySlots = this.getInventory();
        ItemStack currentStack = inventorySlots[index];

        if (currentStack == null || currentStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result;
        if (currentStack.getCount() <= count) {
            result = currentStack.copy();
            inventorySlots[index] = ItemStack.EMPTY;
        } else {
            result = currentStack.split(count);
            if (currentStack.getCount() == 0) {
                inventorySlots[index] = ItemStack.EMPTY;
            }
        }

        this.setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (index < 0 || index >= getContainerSize()) {
            return ItemStack.EMPTY;
        }

        ItemStack[] inventorySlots = this.getInventory();
        ItemStack stack = inventorySlots[index];
        if (stack != null && !stack.isEmpty()) {
            inventorySlots[index] = ItemStack.EMPTY;
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index < 0 || index >= getContainerSize()) {
            return;
        }

        ItemStack[] inventorySlots = this.getInventory();
        inventorySlots[index] = stack != null ? stack : ItemStack.EMPTY;
        this.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return isRelicTalisman(this.getStack()) ? 64 : 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return isRelicTalisman(this.getStack());
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return isRelicTalisman(this.getStack()) && !isRelicTalisman(stack);
    }

    @Override
    public boolean isEmpty() {
        ItemStack[] inventory = this.getInventory();
        for (ItemStack stack : inventory) {
            if (stack != null && !stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void startOpen(Player player) {
        this.invPushed = false;
        this.needsRefresh = true;
    }

    @Override
    public void stopOpen(Player player) {
        this.pushInventory();
    }

    @Override
    public void setChanged() {
    }

    @Override
    public void clearContent() {
        ItemStack[] inventorySlots = this.getInventory();
        for (int i = 0; i < inventorySlots.length; i++) {
            inventorySlots[i] = ItemStack.EMPTY;
        }
        this.setChanged();
    }

    public void refresh() {
        this.needsRefresh = true;
        this.stacks = null;
    }
}