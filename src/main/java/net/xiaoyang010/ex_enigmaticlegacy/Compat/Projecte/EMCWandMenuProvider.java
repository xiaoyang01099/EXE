package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EMCWandMenuProvider implements MenuProvider {

    @Override
    public @NotNull Component getDisplayName() {
        return new TranslatableComponent("gui.ex_enigmaticlegacy.emc_wand.title");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new EMCWandMenu(ModMenus.EMC_MENU, windowId, playerInventory);
    }
}