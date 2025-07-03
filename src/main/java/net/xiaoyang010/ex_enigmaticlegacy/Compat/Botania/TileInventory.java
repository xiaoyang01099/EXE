package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import vazkii.botania.common.block.tile.TileMod;

import javax.annotation.Nullable;

public abstract class TileInventory extends TileMod implements Container {
    protected NonNullList<ItemStack> inventorySlots = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
    private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new InvWrapper(this));

    public TileInventory(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void readPacketNBT(CompoundTag tag) {
        super.readPacketNBT(tag);

        ListTag itemList = tag.getList("Items", Tag.TAG_COMPOUND);
        this.inventorySlots = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < itemList.size(); ++i) {
            CompoundTag itemTag = itemList.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < this.inventorySlots.size()) {
                this.inventorySlots.set(slot, ItemStack.of(itemTag));
            }
        }
    }

    @Override
    public void writePacketNBT(CompoundTag tag) {
        super.writePacketNBT(tag);

        ListTag itemList = new ListTag();
        for (int i = 0; i < this.inventorySlots.size(); ++i) {
            if (!this.inventorySlots.get(i).isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) i);
                this.inventorySlots.get(i).save(itemTag);
                itemList.add(itemTag);
            }
        }
        tag.put("Items", itemList);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.inventorySlots.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack itemStack = ContainerHelper.removeItem(this.inventorySlots, slot, amount);
        if (!itemStack.isEmpty()) {
            this.setChanged();
            if (this.level != null && !this.level.isClientSide) {
                this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return itemStack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.inventorySlots, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.inventorySlots.set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(
                (double) this.worldPosition.getX() + 0.5D,
                (double) this.worldPosition.getY() + 0.5D,
                (double) this.worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.inventorySlots) {
            if (!itemStack.isEmpty()) {
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

    @Override
    public void clearContent() {
        this.inventorySlots.clear();
        this.setChanged();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        itemHandler.invalidate();
    }
}