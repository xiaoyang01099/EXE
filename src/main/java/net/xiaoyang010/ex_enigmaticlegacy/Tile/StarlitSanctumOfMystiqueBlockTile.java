package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Container.StarlitSanctumMenu;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class StarlitSanctumOfMystiqueBlockTile extends RandomizableContainerBlockEntity implements WorldlyContainer {
    // 489个槽位：486个主要区域 + 2个输入槽 + 1个输出槽
    private static final int TOTAL_SLOTS = 489;
    private static final int MAIN_GRID_SLOTS = 486;
    private static final int INPUT_LEFT_SLOT = 486;
    private static final int OUTPUT_SLOT = 487;
    private static final int INPUT_RIGHT_SLOT = 488;

    private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(TOTAL_SLOTS, ItemStack.EMPTY);
    private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());

    public StarlitSanctumOfMystiqueBlockTile(BlockPos position, BlockState state) {
        super(ModBlockEntities.STARLIT_SANCTUM_OF_MYSTIQUE.get(), position, state);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (!this.tryLoadLootTable(compound))
            this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compound, this.stacks);
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        if (!this.trySaveLootTable(compound)) {
            ContainerHelper.saveAllItems(compound, this.stacks);
        }
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
        return new TextComponent("starlit_sanctum_of_mystique");
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new StarlitSanctumMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.worldPosition));
    }

    @Override
    public Component getDisplayName() {
        return new TextComponent("Starlit Sanctum Of Mystique");
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
        // 输出槽不能放入物品
        if (index == OUTPUT_SLOT) {
            return false;
        }
        // 其他槽位都可以放入物品
        return true;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        // 根据面的不同返回不同的槽位
        switch (side) {
            case UP:
                // 上面可以访问输入槽和主要区域
                return IntStream.concat(
                        IntStream.range(0, MAIN_GRID_SLOTS),
                        IntStream.of(INPUT_LEFT_SLOT, INPUT_RIGHT_SLOT)
                ).toArray();
            case DOWN:
                // 下面只能访问输出槽
                return new int[]{OUTPUT_SLOT};
            default:
                // 侧面可以访问所有槽位
                return IntStream.range(0, this.getContainerSize()).toArray();
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        // 从上面放入时，只能放入输入槽和主要区域
        if (direction == Direction.UP) {
            return index != OUTPUT_SLOT && this.canPlaceItem(index, stack);
        }
        // 从下面不能放入任何物品
        if (direction == Direction.DOWN) {
            return false;
        }
        // 从侧面可以放入除输出槽外的所有槽位
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        // 从下面只能取出输出槽的物品
        if (direction == Direction.DOWN) {
            return index == OUTPUT_SLOT;
        }
        // 从上面和侧面可以取出所有槽位的物品
        return true;
    }

    /**
     * 设置输出槽位的物品
     */
    public void setOutput(ItemStack stack) {
        this.setItem(OUTPUT_SLOT, stack);
    }

    /**
     * 获取输出槽位的物品
     */
    public ItemStack getOutput() {
        return this.getItem(OUTPUT_SLOT);
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
}