package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ShapeSlot extends Slot {
    public ShapeSlot(@Nonnull final Container container, final int slot, final int x, final int y)
    {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(@Nonnull final ItemStack itemStack)
    {
        return false;
    }

    @Override
    public boolean mayPickup(@Nonnull final Player player)
    {
        return false;
    }
}