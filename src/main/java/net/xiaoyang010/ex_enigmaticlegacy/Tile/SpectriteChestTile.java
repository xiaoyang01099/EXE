package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTags;

import javax.annotation.Nullable;

public class SpectriteChestTile extends ChestBlockEntity implements LidBlockEntity, WorldlyContainer {
    private final NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private final ChestLidController chestLidController = new ChestLidController();
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }

        protected void onClose(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }

        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
            SpectriteChestTile.this.signalOpenCount(level, pos, state, oldCount, newCount);
        }

        protected boolean isOwnContainer(Player player) {
            if (!(player.containerMenu instanceof ChestMenu)) {
                return false;
            } else {
                Container container = ((ChestMenu)player.containerMenu).getContainer();
                return container == SpectriteChestTile.this ||
                        container instanceof CompoundContainer &&
                                ((CompoundContainer)container).contains(SpectriteChestTile.this);
            }
        }
    };

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items.clear();
        ContainerHelper.loadAllItems(tag, this.items);
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.chestLidController.shouldBeOpen(type > 0);
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.triggerEvent(1, 1);
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.triggerEvent(1, 0);
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public SpectriteChestTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPECTRITE_CHEST_TILE.get(), pos, state);
    }

    @Override
    protected void signalOpenCount(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
        super.signalOpenCount(level, pos, state, oldCount, newCount);
        if (oldCount != newCount) {
            Block block = state.getBlock();
            level.blockEvent(pos, block, 1, newCount);
        }
    }

    public static void lidAnimateTick(Level level, BlockPos pos, BlockState state, SpectriteChestTile blockEntity) {
        blockEntity.chestLidController.tickLid();
    }

    @Override
    public float getOpenNess(float partialTicks) {
        return this.chestLidController.getOpenness(partialTicks);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.is(ModTags.Items.SPECTRITE_ITEMS);
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    public boolean isEmpty() {
        return this.getItems().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.getItems().get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack itemstack = ContainerHelper.removeItem(this.getItems(), slot, amount);
        if (!itemstack.isEmpty()) {
            this.setChanged();
        }
        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.getItems(), slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.getItems().set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public void clearContent() {
        this.getItems().clear();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        int[] slots = new int[27];
        for(int i = 0; i < slots.length; i++) {
            slots[i] = i;
        }
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        for(int i = 0; i < items.size(); i++) {
            this.items.set(i, items.get(i));
        }
    }
}