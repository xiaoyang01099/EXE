package net.xiaoyang010.ex_enigmaticlegacy.Container;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;

public class CobblesStoneMenu extends CraftingMenu {
    private final ContainerLevelAccess access;
    public CobblesStoneMenu(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess access) {
        super(pContainerId, pPlayerInventory, access);
        this.access = access;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(this.access, pPlayer, ModBlockss.COBBLE_STONE.get());
    }
}
