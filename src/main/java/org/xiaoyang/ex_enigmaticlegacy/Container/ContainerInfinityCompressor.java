package org.xiaoyang.ex_enigmaticlegacy.Container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModMenus;
import org.xiaoyang.ex_enigmaticlegacy.Tile.TileEntityInfinityCompressor;

public class ContainerInfinityCompressor extends AbstractContainerMenu {
    private final TileEntityInfinityCompressor tile;

    public ContainerInfinityCompressor(int id, Inventory playerInv, FriendlyByteBuf data) {
        this(id, playerInv, (TileEntityInfinityCompressor) playerInv.player.level.getBlockEntity(data.readBlockPos()));
    }

    public ContainerInfinityCompressor(int id, Inventory playerInv, TileEntityInfinityCompressor tile) {
        super(ModMenus.INFINITY_COMPRESSOR_MENU.get(), id);
        this.tile = tile;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return tile.stillValid(player);
    }

    public TileEntityInfinityCompressor getTile() {
        return tile;
    }
}
