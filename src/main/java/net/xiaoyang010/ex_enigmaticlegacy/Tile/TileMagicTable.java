package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Container.MagicTableMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.BlockEntityBase;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import org.jetbrains.annotations.NotNull;

public class TileMagicTable extends BlockEntityBase implements MenuProvider {
    public static final int SLOT_COUNT = 2;
    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> lazyHandler =
            LazyOptional.of(() -> itemHandler);

    private int progress = 0;
    private int progressMax = 200;

    public TileMagicTable(@NotNull BlockEntityType<TileMagicTable> Type, BlockPos pos, BlockState state) {
        super(Type, pos, state);
    }

    @Override
    public Component getDisplayName() {
        return EComponent.translatable("container.ex_enigmaticlegacy.magic_table");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MagicTableMenu(containerId, inventory, this);
    }

    public int getProgress() { return progress; }
    public void setProgress(int v) { progress = v; setChanged(); }
    public int getProgressMax() { return progressMax; }
    public void setProgressMax(int v) { progressMax = v; setChanged(); }

    public IItemHandler getItemHandler() { return itemHandler; }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return lazyHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyHandler.invalidate();
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("progress", progress);
        tag.putInt("progressMax", progressMax);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
        progressMax = tag.getInt("progressMax");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileMagicTable be) {
        if (level.isClientSide) return;

    }
}