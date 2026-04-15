package org.xiaoyang.ex_enigmaticlegacy.Container;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModBlocks;

public class CobblesStoneMenu extends CraftingMenu {
    private final ContainerLevelAccess access;
    public CobblesStoneMenu(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess access) {
        super(pContainerId, pPlayerInventory, access);
        this.access = access;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(this.access, pPlayer, ModBlocks.COBBLE_STONE.get());
    }
}
