package org.xiaoyang.ex_enigmaticlegacy.Compat.Projecte;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModMenus;

public class EMCWandMenuProvider implements MenuProvider {

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("gui.ex_enigmaticlegacy.emc_wand.title");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new EMCWandMenu(ModMenus.EMC_MENU.get(), windowId, playerInventory);
    }
}