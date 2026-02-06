package net.xiaoyang010.ex_enigmaticlegacy.Container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.NeutroniumDecompressorTile;

public class NeutroniumDecompressorMenu extends AbstractContainerMenu {
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int INV_SLOT_START = 2;
    private static final int INV_SLOT_END = 29;
    private static final int HOTBAR_SLOT_START = 29;
    private static final int HOTBAR_SLOT_END = 38;
    private final NeutroniumDecompressorTile tile;
    private final ContainerData data;

    public NeutroniumDecompressorMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, (NeutroniumDecompressorTile) playerInv.player.level
                .getBlockEntity(buf.readBlockPos()));
    }

    public NeutroniumDecompressorMenu(int id, Inventory playerInv, NeutroniumDecompressorTile tile) {
        super(ModMenus.NEUTRONIUM_DECOMPRESSOR_MENU, id);
        this.tile = tile;
        this.data = tile.data;

        addSlots(playerInv);
        addDataSlots(this.data);
    }

    private void addSlots(Inventory playerInv) {
        this.addSlot(new SlotItemHandler(tile.getInventory(), INPUT_SLOT, 39, 35));

        this.addSlot(new SlotItemHandler(tile.getInventory(), OUTPUT_SLOT, 117, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv,
                        col + row * 9 + 9,
                        8 + col * 18,
                        84 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack resultStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            resultStack = slotStack.copy();

            if (index == OUTPUT_SLOT) {
                if (!this.moveItemStackTo(slotStack, INV_SLOT_START, HOTBAR_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, resultStack);
            }
            else if (index >= INV_SLOT_START && index < HOTBAR_SLOT_END) {
                if (tile.getInventory().isItemValid(INPUT_SLOT, slotStack)) {
                    if (!this.moveItemStackTo(slotStack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                        if (index < INV_SLOT_END) {
                            if (!this.moveItemStackTo(slotStack, HOTBAR_SLOT_START, HOTBAR_SLOT_END, false)) {
                                return ItemStack.EMPTY;
                            }
                        } else if (!this.moveItemStackTo(slotStack, INV_SLOT_START, INV_SLOT_END, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else {
                    if (index < INV_SLOT_END) {
                        if (!this.moveItemStackTo(slotStack, HOTBAR_SLOT_START, HOTBAR_SLOT_END, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(slotStack, INV_SLOT_START, INV_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            else if (!this.moveItemStackTo(slotStack, INV_SLOT_START, HOTBAR_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == resultStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return resultStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.tile.getLevel() != null &&
                this.tile.getLevel().getBlockEntity(this.tile.getBlockPos()) == this.tile &&
                player.distanceToSqr(
                        this.tile.getBlockPos().getX() + 0.5,
                        this.tile.getBlockPos().getY() + 0.5,
                        this.tile.getBlockPos().getZ() + 0.5
                ) <= 64.0;
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return 100;
    }

    public NeutroniumDecompressorTile getTile() {
        return this.tile;
    }
}