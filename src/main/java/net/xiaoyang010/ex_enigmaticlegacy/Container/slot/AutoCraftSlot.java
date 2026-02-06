package net.xiaoyang010.ex_enigmaticlegacy.Container.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileEntityExtremeAutoCrafter;

public class AutoCraftSlot extends Slot {
    public AutoCraftSlot(TileEntityExtremeAutoCrafter container, int i, int i1, int i2) {
        super(container, i, i1, i2);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }
}
