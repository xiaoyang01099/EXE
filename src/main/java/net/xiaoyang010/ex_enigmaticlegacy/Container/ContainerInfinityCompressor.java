package net.xiaoyang010.ex_enigmaticlegacy.Container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileEntityInfinityCompressor;

import javax.annotation.Nonnull;

public class ContainerInfinityCompressor extends AbstractContainerMenu {
    private final TileEntityInfinityCompressor compressor;
    private final Player player;
    private final Level level;

    public ContainerInfinityCompressor(int containerId, Inventory playerInventory, BlockEntity tile) {
        super(ModMenus.INFINITY_COMPRESSOR_MENU, containerId);
        this.compressor = (TileEntityInfinityCompressor) tile;
        this.player = playerInventory.player;
        this.level = player.level;
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 9; x++)
                addSlot(new Slot(playerInventory, 9 + y * 9 + x, 8 + (18 * x), 84 + (18 * y)));

        for (int i = 0; i < 9; i++)
            addSlot(new Slot(playerInventory, i, 8 + (18 * i), 142));
    }

    public ContainerInfinityCompressor(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level.getBlockEntity(buf.readBlockPos()));
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull final Player player, final int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        final Slot actualSlot = this.slots.get(slotIndex);
        if (actualSlot.hasItem()) {
            ItemStack itemstack1 = actualSlot.getItem();
            itemstack = itemstack1.copy();
            if (slotIndex < 27) {
                if (!moveItemStackTo(itemstack1, 27, 36, false))
                    return ItemStack.EMPTY;
            } else {
                if (!moveItemStackTo(itemstack1, 0, 27, false))
                    return ItemStack.EMPTY;
            }
            if (itemstack1.getCount() == 0)
                actualSlot.set(ItemStack.EMPTY);
            actualSlot.setChanged();
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.compressor.stillValid(player);
    }

}