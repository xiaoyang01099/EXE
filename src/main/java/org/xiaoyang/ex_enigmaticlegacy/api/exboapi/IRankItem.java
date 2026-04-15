package org.xiaoyang.ex_enigmaticlegacy.api.exboapi;

import net.minecraft.world.item.ItemStack;
import vazkii.botania.api.mana.ManaItem;

public interface IRankItem extends ManaItem {
    int getLevel(ItemStack stack);
    int[] getLevels();
}