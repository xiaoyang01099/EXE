package org.xiaoyang.ex_enigmaticlegacy.Tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
import org.xiaoyang.ex_enigmaticlegacy.Container.SpectriteChestContainer;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModTags;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class SpectriteChestTile extends BaseContainerBlockEntity implements LidBlockEntity, WorldlyContainer {
    public static final int SLOTS_PER_PAGE = 27;
    public static final int MAX_STACK_SIZE = 100;
    public static final int MAX_PAGE = 9999;
    private final Map<Integer, ItemStack> allItems = new HashMap<>();
    private int currentPage = 0;

    private final ChestLidController chestLidController = new ChestLidController();
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos, SoundEvents.CHEST_OPEN,
                    SoundSource.BLOCKS, 0.5F,
                    level.random.nextFloat() * 0.1F + 0.9F);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos, SoundEvents.CHEST_CLOSE,
                    SoundSource.BLOCKS, 0.5F,
                    level.random.nextFloat() * 0.1F + 0.9F);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos,
                                          BlockState state, int oldCount, int newCount) {
            SpectriteChestTile.this.signalOpenCount(level, pos, state, oldCount, newCount);
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            if (!(player.containerMenu instanceof net.minecraft.world.inventory.ChestMenu menu))
                return false;
            Container c = menu.getContainer();
            return c == SpectriteChestTile.this;
        }
    };

    public SpectriteChestTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPECTRITE_CHEST_TILE.get(), pos, state);
    }

    public int getCurrentPage() { return currentPage; }

    public void setCurrentPage(int page) {
        this.currentPage = Math.max(0, Math.min(page, MAX_PAGE));
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(worldPosition,
                    getBlockState(), getBlockState(), 3);
        }
    }

    public void setCurrentPageClient(int page) {
        this.currentPage = Math.max(0, Math.min(page, MAX_PAGE));
    }

    public void scrollPage(int delta) {
        setCurrentPage(this.currentPage + delta);
    }

    private int toGlobalIndex(int slotInPage) {
        return currentPage * SLOTS_PER_PAGE + slotInPage;
    }

    @Override
    public int getContainerSize() {
        return SLOTS_PER_PAGE;
    }

    @Override
    public int getMaxStackSize() {
        return MAX_STACK_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return allItems.values().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= SLOTS_PER_PAGE) return ItemStack.EMPTY;
        return allItems.getOrDefault(toGlobalIndex(slot), ItemStack.EMPTY);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= SLOTS_PER_PAGE) return ItemStack.EMPTY;
        int globalIndex = toGlobalIndex(slot);
        ItemStack existing = allItems.getOrDefault(globalIndex, ItemStack.EMPTY);
        if (existing.isEmpty()) return ItemStack.EMPTY;

        ItemStack result;
        if (existing.getCount() <= amount) {
            result = existing.copy();
            allItems.remove(globalIndex);
        } else {
            result = existing.split(amount);
            allItems.put(globalIndex, existing);
        }
        this.setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= SLOTS_PER_PAGE) return ItemStack.EMPTY;
        int globalIndex = toGlobalIndex(slot);
        ItemStack stack = allItems.remove(globalIndex);
        return stack != null ? stack : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOTS_PER_PAGE) return;
        int globalIndex = toGlobalIndex(slot);
        if (stack.isEmpty()) {
            allItems.remove(globalIndex);
        } else {
            if (stack.getCount() > MAX_STACK_SIZE) {
                stack.setCount(MAX_STACK_SIZE);
            }
            allItems.put(globalIndex, stack);
        }
        this.setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.is(ModTags.Items.SPECTRITE_ITEMS);
    }

    @Override
    public void clearContent() {
        allItems.clear();
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null) return false;
        if (this.level.getBlockEntity(this.worldPosition) != this) return false;
        return player.distanceToSqr(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        int[] slots = new int[SLOTS_PER_PAGE];
        for (int i = 0; i < slots.length; i++) slots[i] = i;
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
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("CurrentPage", currentPage);
        ListTag listTag = new ListTag();
        for (Map.Entry<Integer, ItemStack> entry : allItems.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            CompoundTag slotTag = new CompoundTag();
            slotTag.putInt("GlobalSlot", entry.getKey());
            entry.getValue().save(slotTag);
            listTag.add(slotTag);
        }
        tag.put("Items", listTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        currentPage = tag.getInt("CurrentPage");
        allItems.clear();
        ListTag listTag = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag slotTag = listTag.getCompound(i);
            int globalSlot = slotTag.getInt("GlobalSlot");
            ItemStack stack = ItemStack.of(slotTag);
            if (!stack.isEmpty()) {
                allItems.put(globalSlot, stack);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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
            this.openersCounter.incrementOpeners(player,
                    this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player,
                    this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    protected void signalOpenCount(Level level, BlockPos pos,
                                   BlockState state, int oldCount, int newCount) {
        level.blockEvent(pos, state.getBlock(), 1, newCount);
    }

    public static void lidAnimateTick(Level level, BlockPos pos,
                                      BlockState state, SpectriteChestTile be) {
        be.chestLidController.tickLid();
    }

    @Override
    public float getOpenNess(float partialTicks) {
        return this.chestLidController.getOpenness(partialTicks);
    }

    public Map<Integer, ItemStack> getAllItems() {
        return allItems;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.spectrite_chest");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new SpectriteChestContainer(id, inventory, this);
    }
}