package net.xiaoyang010.ex_enigmaticlegacy.Container;

import codechicken.lib.data.MCDataInput;
import morph.avaritia.container.MachineMenu;
import morph.avaritia.container.slot.OutputSlot;
import morph.avaritia.container.slot.ScrollingFakeSlot;
import morph.avaritia.container.slot.StaticFakeSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileEntityInfinityCompressor;

import java.util.Objects;

public class ContainerInfinityCompressor extends MachineMenu<TileEntityInfinityCompressor> {
    private static final int CRAFT_SLOT = 0;
    private static final int RESULT_SLOT = 1;
    private static final int INV_SLOT_START = 2;
    private static final int INV_SLOT_END = 29;
    private static final int USE_ROW_SLOT_START = 29;
    private static final int USE_ROW_SLOT_END = 38;

    public ContainerInfinityCompressor(int windowId, Inventory playerInv, MCDataInput packet) {
        this(windowId, playerInv, (TileEntityInfinityCompressor)playerInv.player.level.getBlockEntity(packet.readPos()));
    }

    public ContainerInfinityCompressor(int windowId, Inventory playerInv, TileEntityInfinityCompressor machineTile) {
        super(ModMenus.INFINITY_COMPRESSOR_MENU, windowId, playerInv, machineTile);
        this.addSlot(new Slot(machineTile.inventory, 0, 39, 35));
        this.addSlot(new OutputSlot(machineTile.inventory, 1, 117, 35));
        this.addPlayerInv(8, 84);
        Objects.requireNonNull(machineTile);
        this.addSlot(new StaticFakeSlot(147, 35, machineTile::getTargetStack));
        Objects.requireNonNull(machineTile);
        this.addSlot(new ScrollingFakeSlot(13, 35, machineTile::getInputItems));
    }

    public ItemStack quickMoveStack(Player p_39391_, int index) {
        ItemStack resultStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            resultStack = slotStack.copy();
            if (index == 1) {
                if (!this.moveItemStackTo(slotStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(slotStack, resultStack);
            } else if (index >= 2 && index < 38) {
                if (!this.machineTile.inventory.canPlaceItem(0, slotStack) || !this.moveItemStackTo(slotStack, 0, 1, false)) {
                    if (index < 29) {
                        if (!this.moveItemStackTo(slotStack, 29, 38, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(slotStack, 2, 29, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(slotStack, 2, 38, false)) {
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

            slot.onTake(p_39391_, slotStack);
            if (index == 0) {
                p_39391_.drop(slotStack, false);
            }
        }

        return resultStack;
    }
}
