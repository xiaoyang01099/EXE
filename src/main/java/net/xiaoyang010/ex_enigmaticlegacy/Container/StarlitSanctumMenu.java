package net.xiaoyang010.ex_enigmaticlegacy.Container;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.StarlitSanctumTile;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class StarlitSanctumMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {

    public final Level world;
    public final Player entity;
    private final DataSlot mana = DataSlot.standalone();
    private final DataSlot maxMana = DataSlot.standalone();
    private final DataSlot craftingProgress = DataSlot.standalone();

    // 左侧输入槽指定的Tag
    private static final TagKey<Item> ALLOWED_TAG = ItemTags.create(new ResourceLocation("ex_enigmaticlegacy", "starlit"));
    private final ContainerLevelAccess access;

    private IItemHandler internal;
    private final Map<Integer, Slot> customSlots = new HashMap<>();
    private StarlitSanctumTile tileEntity;

    private static final int MAIN_GRID_SLOTS = 486;
    private static final int INPUT_LEFT_SLOT = 486;
    private static final int OUTPUT_SLOT = 487;
    private static final int INPUT_RIGHT_SLOT = 488;
    private static final int TOTAL_CUSTOM_SLOTS = 489;



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
                    this.internal = handler;
                });
            }
        } else {
            this.access = ContainerLevelAccess.NULL;
        }

        setupSlots();
        setupPlayerInventory(inv);

        this.addDataSlot(mana);
        this.addDataSlot(maxMana);
        this.maxMana.set(10000);
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
        //setupPlayerInventory(inv);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (tileEntity != null && !world.isClientSide) {
            updateMana(tileEntity.getCurrentMana(), tileEntity.getMaxMana());
            this.craftingProgress.set(tileEntity.getCraftingProgressPercent());
        }
    }

    private void setupSlots() {
        int inputLeftX = 95;   int inputLeftY = 31;
        int outputX = 259;     int outputY = 31;
        int inputRightX = 425; int inputRightY = 31;

        int leftBlockX = 23;
        int leftBlockY = 84;

        int midBlockX = 187;
        int midBlockY = 84;

        int rightBlockX = 353;
        int rightBlockY = 84;


        this.customSlots.put(INPUT_LEFT_SLOT, this.addSlot(new SlotItemHandler(internal, INPUT_LEFT_SLOT, inputLeftX, inputLeftY) {
            @Override public boolean mayPlace(ItemStack stack) {return stack.is(ALLOWED_TAG);}
        }));
        this.customSlots.put(OUTPUT_SLOT, this.addSlot(new SlotItemHandler(internal, OUTPUT_SLOT, outputX, outputY) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        }));
        this.customSlots.put(INPUT_RIGHT_SLOT, this.addSlot(new SlotItemHandler(internal, INPUT_RIGHT_SLOT, inputRightX, inputRightY) {
            @Override public boolean mayPlace(ItemStack stack) { return true; }
        }));

        for (int row = 0; row < 18; row++) {
            for (int col = 0; col < 27; col++) {
                int index = row * 27 + col;
                if (index >= MAIN_GRID_SLOTS) break;

                int actualX;
                int actualY;

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
                    this.addSlot(new SlotItemHandler(internal, index, actualX, actualY));
                }
            }
        }
    }

    private void setupPlayerInventory(Inventory inv) {
        int xBase = 187;
        int yBase = 416;
        int hotbarYOffset = 59;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + (row + 1) * 9,
                        xBase + col * 18,
                        yBase + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col,
                    xBase + col * 18,
                    yBase + hotbarYOffset));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlockss.STARLIT_SANCTUM.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem()) {
            return result;
        }

        ItemStack stackInSlot = slot.getItem();
        result = stackInSlot.copy();

        int customSlotsEnd = TOTAL_CUSTOM_SLOTS;
        int playerInventoryStart = customSlotsEnd;
        int playerInventoryEnd = playerInventoryStart + 27;
        int hotbarStart = playerInventoryEnd;
        int hotbarEnd = hotbarStart + 9;

        if (index == OUTPUT_SLOT) {
            if (!this.moveItemStackTo(stackInSlot, playerInventoryStart, hotbarEnd, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stackInSlot, result);
        }
        else if (index == INPUT_LEFT_SLOT || index == INPUT_RIGHT_SLOT) {
            if (!this.moveItemStackTo(stackInSlot, playerInventoryStart, hotbarEnd, false)) {
                return ItemStack.EMPTY;
            }
        }
        else if (index < MAIN_GRID_SLOTS) {
            if (!this.moveItemStackTo(stackInSlot, playerInventoryStart, hotbarEnd, false)) {
                return ItemStack.EMPTY;
            }
        }
        else if (index >= playerInventoryStart && index < hotbarEnd) {
            if (stackInSlot.is(ALLOWED_TAG)) {
                if (!this.moveItemStackTo(stackInSlot, INPUT_LEFT_SLOT, INPUT_LEFT_SLOT + 1, false)) {
                    if (!this.moveItemStackTo(stackInSlot, 0, MAIN_GRID_SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                if (!this.moveItemStackTo(stackInSlot, INPUT_RIGHT_SLOT, INPUT_RIGHT_SLOT + 1, false)) {
                    if (!this.moveItemStackTo(stackInSlot, 0, MAIN_GRID_SLOTS, false)) {
                        if (index < hotbarStart) {
                            if (!this.moveItemStackTo(stackInSlot, hotbarStart, hotbarEnd, false)) {
                                return ItemStack.EMPTY;
                            }
                        } else {
                            if (!this.moveItemStackTo(stackInSlot, playerInventoryStart, hotbarStart, false)) {
                                return ItemStack.EMPTY;
                            }
                        }
                    }
                }
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stackInSlot.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stackInSlot);
        return result;
    }

    @Override
    public Map<Integer, Slot> get() {
        return customSlots;
    }


    public int getMana() {
        return this.mana.get();
    }


    public int getMaxMana() {
        int m = this.maxMana.get();
        return m == 0 ? 1 : m;
    }

    public int getCraftingProgress() {
        return this.craftingProgress.get();
    }

    public void updateMana(int current, int max) {
        this.mana.set(current);
        this.maxMana.set(max);
    }
}