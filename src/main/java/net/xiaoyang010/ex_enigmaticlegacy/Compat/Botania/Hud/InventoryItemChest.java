package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.TalismanHiddenRiches;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;

public class InventoryItemChest implements Container {

    private final Player player;
    private final int slot;
    private ItemStack[] stacks;
    private boolean invPushed;
    private ItemStack storedInv;
    private final int openChest;

    public InventoryItemChest(Player player, int slot, int openChest) {
        this.player = player;
        this.slot = slot;
        this.invPushed = false;
        this.openChest = openChest;
    }

    public static boolean isRelicTalisman(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.getItem() == ModItems.TALISMAN_HIDDEN_RICHES.get();
    }

    public ItemStack getStack() {
        ItemStack stack = this.player.getInventory().getItem(this.slot);
        if (stack != null && !stack.isEmpty()) {
            this.storedInv = stack;
            return stack;
        }
        return ItemStack.EMPTY;
    }

    public ItemStack[] getInventory() {
        if (this.stacks != null) {
            return this.stacks;
        }

        ItemStack stack = getStack();
        if (isRelicTalisman(stack)) {
            this.stacks = TalismanHiddenRiches.getChestLoot(stack, this.openChest);
        }

        if (this.stacks == null) {
            this.stacks = new ItemStack[getContainerSize()];
            for (int i = 0; i < this.stacks.length; i++) {
                this.stacks[i] = ItemStack.EMPTY;
            }
        }

        return this.stacks;
    }

    public void pushInventory() {
        if (this.invPushed) return;

        ItemStack stack = getStack();
        if (stack != null && !stack.isEmpty()) {
            ItemStack[] inv = getInventory();
            if (stack != null && !stack.isEmpty() && inv != null) {
                TalismanHiddenRiches.setChestLoot(stack, inv, this.openChest);
                TalismanHiddenRiches.setOpenChest(stack, this.openChest);
                this.invPushed = true;
            }
        }
    }

    @Override
    public int getContainerSize() {
        return 54;
    }

    @Override
    public boolean isEmpty() {
        ItemStack[] inv = getInventory();
        for (ItemStack stack : inv) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        ItemStack[] inv = getInventory();
        if (index >= 0 && index < inv.length) {
            return inv[index];
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack[] inv = getInventory();
        if (index >= 0 && index < inv.length && !inv[index].isEmpty()) {
            ItemStack itemstack = inv[index];

            if (itemstack.getCount() <= count) {
                inv[index] = ItemStack.EMPTY;
                setChanged();
                return itemstack;
            } else {
                ItemStack result = itemstack.split(count);
                if (itemstack.isEmpty()) {
                    inv[index] = ItemStack.EMPTY;
                }
                setChanged();
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack[] inv = getInventory();
        if (index >= 0 && index < inv.length) {
            ItemStack stack = inv[index];
            inv[index] = ItemStack.EMPTY;
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        ItemStack[] inv = getInventory();
        if (index >= 0 && index < inv.length) {
            inv[index] = stack;
            setChanged();
        }
    }

    @Override
    public void setChanged() {
        pushInventory();
    }

    @Override
    public boolean stillValid(Player player) {
        return isRelicTalisman(getStack());
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return isRelicTalisman(getStack());
    }

    @Override
    public void clearContent() {
        ItemStack[] inv = getInventory();
        for (int i = 0; i < inv.length; i++) {
            inv[i] = ItemStack.EMPTY;
        }
        setChanged();
    }

    public void stopOpen(Player player) {
        pushInventory();
    }

    public void startOpen(Player player) {
        // Called when chest is opened
    }
}