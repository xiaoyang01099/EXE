package org.xiaoyang.ex_enigmaticlegacy.api.exboapi;


import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

public interface IContinuumSpecial {
    ItemStack getContinuumDrop(ItemStack stack, RandomSource random);
}