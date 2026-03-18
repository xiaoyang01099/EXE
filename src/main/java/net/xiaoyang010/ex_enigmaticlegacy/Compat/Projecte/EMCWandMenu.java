package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import org.jetbrains.annotations.NotNull;

public class EMCWandMenu extends AbstractContainerMenu {
    public EMCWandMenu(int i, Inventory inventory, FriendlyByteBuf friendlyByteBuf) {
        super(ModMenus.EMC_MENU, i);
    }

    public EMCWandMenu(MenuType<?> emcMenu, int windowId, @NotNull Inventory playerInventory) {
        super(ModMenus.EMC_MENU, windowId);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
}