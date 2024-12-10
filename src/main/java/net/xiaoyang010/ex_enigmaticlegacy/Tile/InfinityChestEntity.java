package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.Capability;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.stream.IntStream;
import io.netty.buffer.Unpooled;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Menu.InfinityChestMenu;

public class InfinityChestEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> stacks = NonNullList.withSize(273, ItemStack.EMPTY);
    private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());

    private int openCount; // 当前打开箱子的人数
    private float openNess; // 开合状态，0.0表示关闭，1.0表示完全打开
    private float openNessPrev; // 上一tick的开合状态，用于插值

    public InfinityChestEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.INFINITY_CHEST.get(), position, state);
        this.openNess = 0.0F;
        this.openNessPrev = 0.0F;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (!this.tryLoadLootTable(compound))
            this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compound, this.stacks);
        this.openNess = compound.getFloat("OpenNess");
        this.openNessPrev = compound.getFloat("OpenNessPrev");
        this.openCount = compound.getInt("OpenCount");
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        if (!this.trySaveLootTable(compound)) {
            ContainerHelper.saveAllItems(compound, this.stacks);
        }
        compound.putFloat("OpenNess", this.openNess);
        compound.putFloat("OpenNessPrev", this.openNessPrev);
        compound.putInt("OpenCount", this.openCount);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithFullMetadata();
    }

    @Override
    public int getContainerSize() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.stacks)
            if (!itemstack.isEmpty())
                return false;
        return true;
    }

    @Override
    public Component getDefaultName() {
        return new TextComponent("infinity_chest");
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new InfinityChestMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.worldPosition));
    }

    @Override
    public Component getDisplayName() {
        return new TextComponent("Infinity Chest");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (!this.remove && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return handlers[facing.ordinal()].cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        for (LazyOptional<? extends IItemHandler> handler : handlers)
            handler.invalidate();
    }

    // 更新方法，处理开关动画
    public void tick() {
        this.openNessPrev = this.openNess;
        if (this.openCount > 0 && this.openNess < 1.0F) {
            this.openNess += 0.1F;
        } else if (this.openCount == 0 && this.openNess > 0.0F) {
            this.openNess -= 0.1F;
        }
        this.openNess = Math.max(0.0F, Math.min(1.0F, this.openNess));
        if (this.openNess > 0.0F && this.openNessPrev == 0.0F) {
            this.level.playSound(null, this.worldPosition, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else if (this.openNess == 0.0F && this.openNessPrev > 0.0F) {
            this.level.playSound(null, this.worldPosition, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    // 开箱子时调用
    public void openChest() {
        if (this.openCount < 0) {
            this.openCount = 0;
        }
        ++this.openCount;
        this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
    }

    // 关箱子时调用
    public void closeChest() {
        if (this.openCount > 0) {
            --this.openCount;
        }
        this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
    }

    // 返回箱子当前的开合状态，0.0 表示关闭，1.0 表示完全打开
    public float getOpenNess(float partialTicks) {
        return this.openNessPrev + (this.openNess - this.openNessPrev) * partialTicks;
    }

}
