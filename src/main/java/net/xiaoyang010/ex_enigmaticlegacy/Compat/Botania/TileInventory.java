package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.botania.api.item.ISparkEntity;


public abstract class TileInventory extends BlockEntity implements Container {
    private NonNullList<ItemStack> inventoryItems;

    public TileInventory(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventoryItems = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ListTag listTag = tag.getList("Items", 10);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag itemTag = listTag.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot < inventoryItems.size()) {
                inventoryItems.set(slot, ItemStack.of(itemTag));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag listTag = new ListTag();
        for (int i = 0; i < inventoryItems.size(); i++) {
            if (!inventoryItems.get(i).isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) i);
                inventoryItems.get(i).save(itemTag);
                listTag.add(itemTag);
            }
        }
        tag.put("Items", listTag);
    }

    @Override
    public ItemStack getItem(int slot) {
        return inventoryItems.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack itemstack = getItem(slot);
        if (!itemstack.isEmpty()) {
            if (!level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }

            if (itemstack.getCount() <= amount) {
                setItem(slot, ItemStack.EMPTY);
                setChanged();
                return itemstack;
            } else {
                ItemStack splitStack = itemstack.split(amount);
                if (itemstack.isEmpty()) {
                    setItem(slot, ItemStack.EMPTY);
                }
                setChanged();
                return splitStack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = getItem(slot);
        setItem(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventoryItems.set(slot, stack);
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean stillValid(Player player) {
        if (level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public void clearContent() {
        inventoryItems.clear();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : inventoryItems) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void startOpen(Player player) {
    }

    @Override
    public void stopOpen(Player player) {
    }

    public abstract int getCurrentMana();

    public abstract void recieveMana(int mana);

    public abstract boolean canReceiveManaFromBursts();

    public abstract void attachSpark(ISparkEntity entity);
}