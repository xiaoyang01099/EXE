package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;

public class InventoryOverpowered implements Container {
    public static int INV_SIZE;
    public final NonNullList<ItemStack> inventory;
    public ItemStack enderPearlStack = ItemStack.EMPTY;
    public ItemStack enderChestStack = ItemStack.EMPTY;
    Player player;
    private static final String TAG_NAME = "opinvtags";
    private static final String TAG_SLOT = "Slot";

    public InventoryOverpowered(Player player) {
        this.player = player;
        INV_SIZE = 2 * Const.HOTBAR_SIZE +
                Const.V_INVO_SIZE * ConfigHandler.PowerInventoryConfig.getMaxSections();
        this.inventory = NonNullList.withSize(INV_SIZE, ItemStack.EMPTY);
    }

    @Override
    public int getContainerSize() {
        return INV_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) return false;
        }
        return enderPearlStack.isEmpty() && enderChestStack.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot == Const.SLOT_EPEARL) return enderPearlStack;
        if (slot == Const.SLOT_ECHEST) return enderChestStack;
        if (slot >= 0 && slot < inventory.size()) return inventory.get(slot);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack itemstack;

        if (index == Const.SLOT_ECHEST) {
            if (this.enderChestStack.getCount() <= count) {
                itemstack = this.enderChestStack;
                this.enderChestStack = ItemStack.EMPTY;
                syncSlotToClients(index);
                return itemstack;
            } else {
                itemstack = this.enderChestStack.split(count);
                if (this.enderChestStack.getCount() == 0) {
                    this.enderChestStack = ItemStack.EMPTY;
                }
                syncSlotToClients(index);
                return itemstack;
            }
        }
        else if (index == Const.SLOT_EPEARL) {
            if (this.enderPearlStack.getCount() <= count) {
                itemstack = this.enderPearlStack;
                this.enderPearlStack = ItemStack.EMPTY;
                syncSlotToClients(index);
                return itemstack;
            } else {
                itemstack = this.enderPearlStack.split(count);
                if (this.enderPearlStack.getCount() == 0) {
                    this.enderPearlStack = ItemStack.EMPTY;
                }
                syncSlotToClients(index);
                return itemstack;
            }
        }
        else {
            if (!this.getItem(index).isEmpty()) {
                if (this.getItem(index).getCount() <= count) {
                    itemstack = this.getItem(index);
                    this.setItem(index, ItemStack.EMPTY);
                    return itemstack;
                } else {
                    itemstack = this.getItem(index).split(count);
                    if (this.getItem(index).getCount() == 0) {
                        this.setItem(index, ItemStack.EMPTY);
                    }
                    syncSlotToClients(index);
                    return itemstack;
                }
            } else {
                return ItemStack.EMPTY;
            }
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = getItem(slot);
        if (!stack.isEmpty()) {
            setItemDirect(slot, ItemStack.EMPTY);
        }
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        setItemDirect(slot, stack);

        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }

        if (player != null && !player.level.isClientSide) {
            syncSlotToClients(slot);
        }
    }

    private void setItemDirect(int slot, ItemStack stack) {
        if (slot == Const.SLOT_EPEARL) {
            enderPearlStack = stack;
        } else if (slot == Const.SLOT_ECHEST) {
            enderChestStack = stack;
        } else if (slot >= 0 && slot < inventory.size()) {
            inventory.set(slot, stack);
        }
    }

    @Override
    public void setChanged() {
        for (int i = 0; i < inventory.size(); i++) {
            if (!inventory.get(i).isEmpty() && inventory.get(i).getCount() == 0) {
                inventory.set(i, ItemStack.EMPTY);
            }
        }

        if (!enderPearlStack.isEmpty() && enderPearlStack.getCount() == 0) {
            enderPearlStack = ItemStack.EMPTY;
        }
        if (!enderChestStack.isEmpty() && enderChestStack.getCount() == 0) {
            enderChestStack = ItemStack.EMPTY;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.player == player;
    }

    @Override
    public void clearContent() {
        inventory.clear();
        enderPearlStack = ItemStack.EMPTY;
        enderChestStack = ItemStack.EMPTY;
    }

    public void writeToNBT(CompoundTag tags) {
        ListTag nbttaglist = new ListTag();

        for (int i = 0; i < this.getContainerSize(); i++) {
            if (!this.getItem(i).isEmpty()) {
                CompoundTag tagcompound = new CompoundTag();
                tagcompound.putInt(TAG_SLOT, i);
                this.getItem(i).save(tagcompound);
                nbttaglist.add(tagcompound);
            }
        }

        if (!this.enderChestStack.isEmpty()) {
            CompoundTag tagcompound = new CompoundTag();
            tagcompound.putInt(TAG_SLOT, Const.SLOT_ECHEST);
            this.enderChestStack.save(tagcompound);
            nbttaglist.add(tagcompound);
        }

        if (!this.enderPearlStack.isEmpty()) {
            CompoundTag tagcompound = new CompoundTag();
            tagcompound.putInt(TAG_SLOT, Const.SLOT_EPEARL);
            this.enderPearlStack.save(tagcompound);
            nbttaglist.add(tagcompound);
        }

        tags.put(TAG_NAME, nbttaglist);
    }

    public void readFromNBT(CompoundTag tagcompound) {
        ListTag nbttaglist = tagcompound.getList(TAG_NAME, 10);

        for (int i = 0; i < nbttaglist.size(); i++) {
            CompoundTag tags = nbttaglist.getCompound(i);
            int slot = tags.getInt(TAG_SLOT);
            ItemStack itemstack = ItemStack.of(tags);

            if (slot >= 0 && slot < this.getContainerSize()) {
                this.inventory.set(slot, itemstack);
            } else if (!itemstack.isEmpty()) {
                if (slot == Const.SLOT_EPEARL) {
                    enderPearlStack = itemstack;
                }
                if (slot == Const.SLOT_ECHEST) {
                    enderChestStack = itemstack;
                }
            }
        }
    }

    public void syncSlotToClients(int slot) {
        if (player == null || player.level.isClientSide) return;

        try {
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHandler.sendSlotSync(serverPlayer, slot, getItem(slot));
            }
        } catch (Exception e) {
        }
    }

    public void syncAll(ServerPlayer player) {
        if (player.level.isClientSide) return;

        for (int i = 0; i < inventory.size(); i++) {
            if (!inventory.get(i).isEmpty()) {
                NetworkHandler.sendSlotSync(player, i, inventory.get(i));
            }
        }

        if (!enderPearlStack.isEmpty()) {
            NetworkHandler.sendSlotSync(player, Const.SLOT_EPEARL, enderPearlStack);
        }

        if (!enderChestStack.isEmpty()) {
            NetworkHandler.sendSlotSync(player, Const.SLOT_ECHEST, enderChestStack);
        }
    }

}