
package net.xiaoyang010.ex_enigmaticlegacy.Container;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import org.jetbrains.annotations.NotNull;

public class InfinityChestMenu extends AbstractContainerMenu {
    public final Level world;
    public final Player player;
    private final Container container;

    public InfinityChestMenu(int id, Inventory inv){
        this(id, inv, new SimpleContainer(243));
    }

    public InfinityChestMenu(int id, Inventory inv, Container chest) {
        super(ModMenus.INFINITE_CHEST_SCREEN, id);
        this.player = inv.player;
        this.world = inv.player.level;
        this.container = chest;
        this.container.startOpen(player);
        for (int j = 0; j < 9; j++) {
            for (int i = 0; i < 27; i++) {
                this.addSlot(new Slot(chest, i + j * 27, -154 + 18 * i, -54 + 18 * j));
            }
        }
        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(inv, sj + (si + 1) * 9, 8 + sj * 18, 38 + 84 + si * 18));
        for (int si = 0; si < 9; ++si)
            this.addSlot(new Slot(inv, si, 8 + si * 18, 38 + 142));
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    @NotNull
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 243) {
                if (!this.moveItemStackTo(itemstack1, 243, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 243, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        this.container.stopOpen(playerIn);
    }

    public Container getContainer() {
        return container;
    }
}
