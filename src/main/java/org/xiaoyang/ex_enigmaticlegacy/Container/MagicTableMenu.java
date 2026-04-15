package org.xiaoyang.ex_enigmaticlegacy.Container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.xiaoyang.ex_enigmaticlegacy.Container.slot.MagicTableInputSlot;
import org.xiaoyang.ex_enigmaticlegacy.Container.slot.MagicTableOutputSlot;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModBlocks;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModMenus;
import org.xiaoyang.ex_enigmaticlegacy.Tile.TileMagicTable;

import java.math.BigInteger;

public class MagicTableMenu extends AbstractContainerMenu {
    private final TileMagicTable blockEntity;
    private final ContainerLevelAccess access;
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int PLAYER_INV_START = 2;
    public static final int PLAYER_HOTBAR_START = 29;
    public static final int TOTAL_SLOTS = 38;
    public int progress = 0;
    public int progressMax = 200;
    public int convertGear = 0;
    public long syncedInputCount = 0;
    public long syncedOutputCount = 0;
    public long syncedPlayerEmc = 0;
    public long syncedItemEmc = 0;

    public MagicTableMenu(int containerId, Inventory playerInventory, TileMagicTable blockEntity) {
        super(ModMenus.MAGIC_TABLE_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.addSlot(new MagicTableInputSlot(blockEntity, 79, 83));
        this.addSlot(new MagicTableOutputSlot(blockEntity, 161, 83));

        addPlayerInventory(playerInventory);
        addSyncSlots(playerInventory);
    }

    private void addSyncSlots(Inventory playerInventory) {
        this.addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getProgress(); }
            @Override public void set(int v) { progress = v; }
        });
        this.addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getProgressMax(); }
            @Override public void set(int v) { progressMax = v; }
        });
        this.addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getConvertGear(); }
            @Override public void set(int v) { convertGear = v; }
        });
        addLongSync(blockEntity::getInputCount, v -> syncedInputCount = v);
        addLongSync(blockEntity::getOutputCount, v -> syncedOutputCount = v);
        addLongSync(() -> {
            BigInteger emc = TileMagicTable.getPlayerEmc(playerInventory.player);
            return emc.min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
        }, v -> syncedPlayerEmc = v);
        addLongSync(() -> TileMagicTable.getItemEmcValue(blockEntity.getOutputType()),
                v -> syncedItemEmc = v);
    }

    private void addLongSync(java.util.function.LongSupplier serverGetter,
                             java.util.function.LongConsumer clientSetter) {
        final long[] assembled = {0};
        this.addDataSlot(new DataSlot() {
            @Override public int get() { return (int)(serverGetter.getAsLong() & 0xFFFFFFFFL); }
            @Override public void set(int v) {
                assembled[0] = (assembled[0] & 0xFFFFFFFF00000000L) | (v & 0xFFFFFFFFL);
                clientSetter.accept(assembled[0]);
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override public int get() { return (int)(serverGetter.getAsLong() >>> 32); }
            @Override public void set(int v) {
                assembled[0] = (assembled[0] & 0x00000000FFFFFFFFL) | ((long)v << 32);
                clientSetter.accept(assembled[0]);
            }
        });
    }

    public MagicTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory,
                (TileMagicTable) playerInventory.player.level.getBlockEntity(extraData.readBlockPos()));
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(access, player, ModBlocks.MAGIC_TABLE.get());
    }

    @Override
    public void broadcastChanges() {
        forceUpdateVirtualSlots();

        super.broadcastChanges();
    }

    private void forceUpdateVirtualSlots() {
        Slot inputSlot = this.slots.get(SLOT_INPUT);
        inputSlot.container.setItem(0, blockEntity.getInputDisplay());

        Slot outputSlot = this.slots.get(SLOT_OUTPUT);
        outputSlot.container.setItem(0, blockEntity.getOutputDisplay());
    }

    @Override
    public void clicked(int slotId, int dragType, @NotNull ClickType clickType, @NotNull Player player) {
        if (slotId == SLOT_INPUT || slotId == SLOT_OUTPUT) {
            if (player.level.isClientSide) {
                return;
            }
            handleVirtualSlotClick(slotId, dragType, clickType, player);
            broadcastChanges();
            return;
        }

        super.clicked(slotId, dragType, clickType, player);
    }

    private void handleVirtualSlotClick(int slotId, int dragType, ClickType clickType, Player player) {
        switch (clickType) {
            case PICKUP:
                if (slotId == SLOT_INPUT) {
                    handleInputPickup(player, dragType == 1);
                } else {
                    handleOutputPickup(player, dragType == 1);
                }
                break;

            case QUICK_MOVE:
                quickMoveStack(player, slotId);
                break;

            case SWAP:
                handleSwap(slotId, dragType, player);
                break;

            case THROW:
                handleThrow(slotId, dragType == 1, player);
                break;

            case CLONE:
                if (player.getAbilities().instabuild) {
                    ItemStack peek = (slotId == SLOT_INPUT)
                            ? blockEntity.peekInput(64)
                            : blockEntity.peekOutput(64);
                    if (!peek.isEmpty()) {
                        setCarried(peek.copy());
                    }
                }
                break;

            default:
                break;
        }
    }

    private void handleInputPickup(Player player, boolean rightClick) {
        ItemStack carried = getCarried();

        if (carried.isEmpty()) {
            if (blockEntity.getInputCount() <= 0) return;

            int takeCount;
            if (rightClick) {
                long half = blockEntity.getInputCount() / 2;
                takeCount = (int) Math.min(Math.max(1, half), 64);
            } else {
                takeCount = (int) Math.min(blockEntity.getInputCount(), 64);
            }

            ItemStack taken = blockEntity.extractInput(takeCount);
            setCarried(taken);
        } else {
            if (rightClick) {
                ItemStack single = carried.copy();
                single.setCount(1);
                ItemStack remainder = blockEntity.insertInput(single, false);
                if (remainder.isEmpty()) {
                    carried.shrink(1);
                    if (carried.isEmpty()) setCarried(ItemStack.EMPTY);
                }
            } else {
                ItemStack remainder = blockEntity.insertInput(carried.copy(), false);
                setCarried(remainder);
            }
        }
    }

    private void handleOutputPickup(Player player, boolean rightClick) {
        ItemStack carried = getCarried();
        if (!carried.isEmpty()) return;

        if (blockEntity.getOutputCount() <= 0) return;

        int takeCount;
        if (rightClick) {
            long half = blockEntity.getOutputCount() / 2;
            takeCount = (int) Math.min(Math.max(1, half), 64);
        } else {
            takeCount = (int) Math.min(blockEntity.getOutputCount(), 64);
        }

        ItemStack taken = blockEntity.extractOutput(takeCount);
        setCarried(taken);
    }

    private void handleSwap(int slotId, int hotbarIndex, Player player) {
        if (hotbarIndex < 0 || hotbarIndex >= 9) return;

        ItemStack hotbarStack = player.getInventory().getItem(hotbarIndex);

        if (slotId == SLOT_INPUT) {
            if (!hotbarStack.isEmpty()) {
                ItemStack extracted = blockEntity.extractInput(64);
                ItemStack remainder = blockEntity.insertInput(hotbarStack.copy(), false);
                if (remainder.isEmpty()) {
                    player.getInventory().setItem(hotbarIndex, extracted);
                } else {
                    blockEntity.insertInput(extracted, false);
                }
            } else {
                ItemStack extracted = blockEntity.extractInput(64);
                player.getInventory().setItem(hotbarIndex, extracted);
            }
        } else {
            if (hotbarStack.isEmpty()) {
                ItemStack extracted = blockEntity.extractOutput(64);
                player.getInventory().setItem(hotbarIndex, extracted);
            }
        }
    }

    private void handleThrow(int slotId, boolean throwAll, Player player) {
        if (slotId == SLOT_INPUT) {
            int count = throwAll ? (int) Math.min(blockEntity.getInputCount(), 64) : 1;
            ItemStack toDrop = blockEntity.extractInput(count);
            if (!toDrop.isEmpty()) player.drop(toDrop, false);
        } else {
            int count = throwAll ? (int) Math.min(blockEntity.getOutputCount(), 64) : 1;
            ItemStack toDrop = blockEntity.extractOutput(count);
            if (!toDrop.isEmpty()) player.drop(toDrop, false);
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        if (index == SLOT_OUTPUT) {
            return handleOutputQuickMove(player);
        }
        if (index == SLOT_INPUT) {
            return handleInputQuickMove(player);
        }
        if (index >= PLAYER_INV_START && index < TOTAL_SLOTS) {
            return handlePlayerInvQuickMove(player, index);
        }
        return ItemStack.EMPTY;
    }

    private ItemStack handleOutputQuickMove(Player player) {
        if (blockEntity.getOutputCount() <= 0) return ItemStack.EMPTY;

        ItemStack preview = blockEntity.peekOutput(64);
        if (preview.isEmpty()) return ItemStack.EMPTY;

        int canFit = simulateFitInPlayerInventory(player.getInventory(), preview);
        if (canFit <= 0) return ItemStack.EMPTY;

        ItemStack extracted = blockEntity.extractOutput(canFit);
        if (extracted.isEmpty()) return ItemStack.EMPTY;

        if (!player.getInventory().add(extracted)) {
            player.drop(extracted, false);
        }

        broadcastChanges();
        return extracted;
    }

    private ItemStack handleInputQuickMove(Player player) {
        if (blockEntity.getInputCount() <= 0) return ItemStack.EMPTY;

        ItemStack preview = blockEntity.peekInput(64);
        if (preview.isEmpty()) return ItemStack.EMPTY;

        int canFit = simulateFitInPlayerInventory(player.getInventory(), preview);
        if (canFit <= 0) return ItemStack.EMPTY;

        ItemStack extracted = blockEntity.extractInput(canFit);
        if (extracted.isEmpty()) return ItemStack.EMPTY;

        if (!player.getInventory().add(extracted)) {
            player.drop(extracted, false);
        }

        broadcastChanges();
        return extracted;
    }

    private ItemStack handlePlayerInvQuickMove(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        ItemStack stack = slot.getItem();
        if (stack.isEmpty()) return ItemStack.EMPTY;

        ItemStack original = stack.copy();

        ItemStack remainder = blockEntity.insertInput(stack.copy(), false);

        int inserted = stack.getCount() - remainder.getCount();
        if (inserted > 0) {
            stack.shrink(inserted);
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            broadcastChanges();
            return original;
        }

        if (slotIndex < PLAYER_HOTBAR_START) {
            if (!this.moveItemStackTo(stack, PLAYER_HOTBAR_START, TOTAL_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, PLAYER_HOTBAR_START, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return stack.getCount() == original.getCount() ? ItemStack.EMPTY : original;
    }

    private int simulateFitInPlayerInventory(Inventory inventory, ItemStack stack) {
        int remaining = stack.getCount();
        int maxStackSize = stack.getMaxStackSize();

        for (int i = 0; i < inventory.items.size() && remaining > 0; i++) {
            ItemStack invStack = inventory.items.get(i);
            if (!invStack.isEmpty() && ItemHandlerHelper.canItemStacksStack(invStack, stack)) {
                int canAdd = Math.min(remaining, maxStackSize - invStack.getCount());
                if (canAdd > 0) remaining -= canAdd;
            }
        }

        for (int i = 0; i < inventory.items.size() && remaining > 0; i++) {
            if (inventory.items.get(i).isEmpty()) {
                int canAdd = Math.min(remaining, maxStackSize);
                remaining -= canAdd;
            }
        }

        return stack.getCount() - remaining;
    }

    public int getProgressScaled() {
        if (progressMax <= 0) return 0;
        return progress * 42 / progressMax;
    }

    public String getFormattedInputCount() { return formatLargeNumber(syncedInputCount); }
    public String getFormattedOutputCount() { return formatLargeNumber(syncedOutputCount); }
    public String getFormattedPlayerEmc() { return formatLargeNumber(syncedPlayerEmc); }

    public String getFormattedConvertEmc() {
        if (syncedItemEmc <= 0 || syncedOutputCount <= 0) return "0";
        long convertCount = getConvertCountClient();
        long actualConvert = Math.min(convertCount, syncedOutputCount);
        BigInteger total = BigInteger.valueOf(syncedItemEmc).multiply(BigInteger.valueOf(actualConvert));
        return formatBigInteger(total);
    }

    private long getConvertCountClient() {
        switch (convertGear) {
            case 0: return 1;
            case 1: return 10;
            case 2: return 64;
            case 3: return 1000;
            case 4: return syncedOutputCount;
            case 5: return blockEntity != null ? blockEntity.getCustomConvertAmount() : 1;
            default: return 1;
        }
    }

    public static String formatLargeNumber(long value) {
        if (value < 0) return "0";
        if (value < 1000) return String.valueOf(value);
        if (value < 1_000_000) return String.format("%.1fK", value / 1000.0);
        if (value < 1_000_000_000L) return String.format("%.1fM", value / 1_000_000.0);
        if (value < 1_000_000_000_000L) return String.format("%.1fB", value / 1_000_000_000.0);
        if (value < 1_000_000_000_000_000L) return String.format("%.1fT", value / 1_000_000_000_000.0);
        return String.format("%.1fQ", value / 1_000_000_000_000_000.0);
    }

    public static String formatBigInteger(BigInteger value) {
        if (value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
            return formatLargeNumber(value.longValue());
        }
        return value.toString();
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        50 + col * 18, 152 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 50 + col * 18, 210));
        }
    }

    public TileMagicTable getBlockEntity() { return blockEntity; }
}