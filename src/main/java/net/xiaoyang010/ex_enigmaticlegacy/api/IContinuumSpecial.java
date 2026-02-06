package net.xiaoyang010.ex_enigmaticlegacy.api;

import net.minecraft.world.item.ItemStack;

import java.util.Random;

public interface IContinuumSpecial {
    ItemStack getContinuumDrop(ItemStack stack, Random random);
}