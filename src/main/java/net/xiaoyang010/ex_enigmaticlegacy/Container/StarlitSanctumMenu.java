package net.xiaoyang010.ex_enigmaticlegacy.Container;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.*;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.StarlitSanctumTile;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class StarlitSanctumMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
    private static final int MAIN_GRID_SLOTS = 486;
    private static final int INPUT_LEFT_SLOT = 486;
    private static final int INPUT_RIGHT_SLOT = 487;
    private static final int OUTPUT_SLOT = 488;
    private static final int TOTAL_CUSTOM_SLOTS = 489;
    public final Level world;
    public final Player entity;
    private final DataSlot manaLow = DataSlot.standalone();
    private final DataSlot manaHigh = DataSlot.standalone();
    private final DataSlot maxManaLow = DataSlot.standalone();
    private final DataSlot maxManaHigh = DataSlot.standalone();
    private final DataSlot craftingProgress = DataSlot.standalone();
    private final ContainerLevelAccess access;
    private final Map<Integer, Slot> customSlots = new HashMap<>();
    private IItemHandler internal;
    private StarlitSanctumTile tileEntity;

    public StarlitSanctumMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ModMenus.STARLIT_SANCTUM_SCREEN, id);
        this.entity = inv.player;
        this.world = inv.player.level;
        this.internal = new ItemStackHandler(TOTAL_CUSTOM_SLOTS);

        BlockPos pos = null;
        if (extraData != null) {
            pos = extraData.readBlockPos();
        }

        if (pos != null) {
            this.access = ContainerLevelAccess.create(world, pos);
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof StarlitSanctumTile tile) {
                this.tileEntity = tile;
                blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
                    if (world.isClientSide) {
                        this.internal = new ClientPermissiveHandler(handler);
                    } else {
                        this.internal = handler;
                    }
                });
            }
        } else {
            this.access = ContainerLevelAccess.NULL;
        }

        setupSlots();
        setupPlayerInventory(inv);

        this.addDataSlot(manaLow);
        this.addDataSlot(manaHigh);
        this.addDataSlot(maxManaLow);
        this.addDataSlot(maxManaHigh);
        this.addDataSlot(craftingProgress);

        setMaxManaLong(Long.MAX_VALUE);
    }

    public StarlitSanctumMenu(int id, Inventory inv, BlockPos pos) {
        super(ModMenus.STARLIT_SANCTUM_SCREEN, id);
        this.entity = inv.player;
        this.world = inv.player.level;
        this.access = ContainerLevelAccess.create(world, pos);
        this.internal = new ItemStackHandler(TOTAL_CUSTOM_SLOTS);

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null) {
            blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> this.internal = h);
        }

        setupSlots();

        this.addDataSlot(manaLow);
        this.addDataSlot(manaHigh);
        this.addDataSlot(maxManaLow);
        this.addDataSlot(maxManaHigh);
        this.addDataSlot(craftingProgress);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (tileEntity != null && !world.isClientSide) {
            updateManaLong(tileEntity.getCurrentManaLong(), tileEntity.getMaxMana());
            this.craftingProgress.set(tileEntity.getCraftingProgressPercent());
        }
    }

    private void setupSlots() {
        int inputLeftX = 95;
        int inputLeftY = 31;
        int outputX = 259;
        int outputY = 31;
        int inputRightX = 425;
        int inputRightY = 31;

        int leftBlockX = 23;
        int leftBlockY = 84;
        int midBlockX = 187;
        int midBlockY = 84;
        int rightBlockX = 353;
        int rightBlockY = 84;

        for (int row = 0; row < 18; row++) {
            for (int col = 0; col < 27; col++) {
                int index = row * 27 + col;
                if (index >= MAIN_GRID_SLOTS) break;

                int actualX, actualY;

                if (col < 9) {
                    actualX = leftBlockX + col * 18;
                    actualY = leftBlockY + row * 18;
                } else if (col < 18) {
                    actualX = midBlockX + (col - 9) * 18;
                    actualY = midBlockY + row * 18;
                } else {
                    actualX = rightBlockX + (col - 18) * 18;
                    actualY = rightBlockY + row * 18;
                }

                if (index < internal.getSlots()) {
                    this.addSlot(new BSlot(internal, index, actualX, actualY));
                }
            }
        }

        this.customSlots.put(INPUT_LEFT_SLOT, this.addSlot(new BSlot(internal, INPUT_LEFT_SLOT, inputLeftX, inputLeftY){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty() && stack.is(StarlitSanctumTile.STARLIT) && super.mayPlace(stack);
            }
        }));
        this.customSlots.put(INPUT_RIGHT_SLOT, this.addSlot(new BSlot(internal, INPUT_RIGHT_SLOT, inputRightX, inputRightY)));
        this.customSlots.put(OUTPUT_SLOT, this.addSlot(new SlotItemHandler(internal, OUTPUT_SLOT, outputX, outputY) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        }));
    }

    private void setupPlayerInventory(Inventory inv) {
        int xBase = 187;
        int yBase = 416;
        int hotbarYOffset = 59;

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + (row + 1) * 9, xBase + col * 18, yBase + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, xBase + col * 18, yBase + hotbarYOffset));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlockss.STARLIT_SANCTUM.get());
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack sourceStack = ItemStack.EMPTY;
        Slot sourceSlot = this.slots.get(index);

        if (sourceSlot != null && sourceSlot.hasItem()) {
            ItemStack slotStack = sourceSlot.getItem();
            sourceStack = slotStack.copy();

            int gridStart = 0;
            int gridEnd = MAIN_GRID_SLOTS;
            int specialStart = MAIN_GRID_SLOTS;
            int specialEnd = TOTAL_CUSTOM_SLOTS;
            int playerStart = TOTAL_CUSTOM_SLOTS;
            int playerEnd = this.slots.size();

            if (index < playerStart) {
                if (!this.moveItemStackTo(slotStack, playerStart, playerEnd, true)) {
                    return ItemStack.EMPTY;
                }

                if (index == OUTPUT_SLOT) {
                    sourceSlot.onQuickCraft(slotStack, sourceStack);
                }
            } else {
                if (!this.moveItemStackTo(slotStack, gridStart, gridEnd, false)) {
                    if (!this.moveItemStackTo(slotStack, INPUT_LEFT_SLOT, INPUT_RIGHT_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (slotStack.isEmpty()) {
                sourceSlot.set(ItemStack.EMPTY);
            } else {
                sourceSlot.setChanged();
            }

            if (slotStack.getCount() == sourceStack.getCount()) {
                return ItemStack.EMPTY;
            }

            sourceSlot.onTake(playerIn, slotStack);
        }

        return sourceStack;
    }

    @Override
    public Map<Integer, Slot> get() {
        return customSlots;
    }

    public long getManaLong() {
        long low = Integer.toUnsignedLong(manaLow.get());
        long high = ((long) manaHigh.get()) << 32;
        return high | low;
    }

    private void setManaLong(long value) {
        this.manaLow.set((int) value);
        this.manaHigh.set((int) (value >>> 32));
    }

    public long getMaxManaLong() {
        long low = Integer.toUnsignedLong(maxManaLow.get());
        long high = ((long) maxManaHigh.get()) << 32;
        return high | low;
    }

    private void setMaxManaLong(long value) {
        this.maxManaLow.set((int) value);
        this.maxManaHigh.set((int) (value >>> 32));
    }

    public void updateManaLong(long current, long max) {
        setManaLong(current);
        setMaxManaLong(max);
    }

    /**
     * 兼容旧代码：获取当前魔力值（int，可能溢出）
     *
     * @deprecated 使用 getManaLong() 代替
     */
    @Deprecated
    public int getMana() {
        long mana = getManaLong();
        if (mana > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) mana;
    }

    /**
     * 兼容旧代码：获取最大魔力值（int，可能溢出）
     *
     * @deprecated 使用 getMaxManaLong() 代替
     */
    @Deprecated
    public int getMaxMana() {
        long maxMana = getMaxManaLong();
        if (maxMana > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (maxMana == 0) {
            return 1; // 防止除零
        }
        return (int) maxMana;
    }

    /**
     * 兼容旧代码：更新魔力值（int）
     *
     * @deprecated 使用 updateManaLong() 代替
     */
    @Deprecated
    public void updateMana(int current, int max) {
        updateManaLong(current, max);
    }

    public int getCraftingProgress() {
        return this.craftingProgress.get();
    }

    private static class ClientPermissiveHandler implements IItemHandlerModifiable {
        private final IItemHandler parent;

        public ClientPermissiveHandler(IItemHandler parent) {
            this.parent = parent;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlots() {
            return parent.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return parent.getStackInSlot(slot);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return parent.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (parent instanceof IItemHandlerModifiable) {
                ((IItemHandlerModifiable) parent).setStackInSlot(slot, stack);
            }
        }
    }

    public static class BSlot extends Slot {
        private final IItemHandler handler;
        private final int index;

        public BSlot(IItemHandler handler, int index, int x, int y) {
            super(new SimpleContainer(handler.getSlots()), index, x, y);
            this.handler = handler;
            this.index = index;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return handler.isItemValid(index, stack);
        }

        @Override
        public ItemStack getItem() {
            return handler.getStackInSlot(index);
        }

        @Override
        public void set(ItemStack stack) {
            if (handler instanceof IItemHandlerModifiable) {
                ((IItemHandlerModifiable) handler).setStackInSlot(index, stack);
            }
            this.setChanged();
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            ItemStack maxAdd = stack.copy();
            int maxInput = stack.getMaxStackSize();
            maxAdd.setCount(maxInput);

            int handlerLimit = handler.getSlotLimit(index);
            return Math.min(maxInput, handlerLimit);
        }

        @Override
        public int getMaxStackSize() {
            return handler.getSlotLimit(index);
        }

        @Override
        public ItemStack remove(int amount) {
            return handler.extractItem(index, amount, false);
        }

        @Override
        public void setChanged() {
            super.setChanged();
        }
    }
}