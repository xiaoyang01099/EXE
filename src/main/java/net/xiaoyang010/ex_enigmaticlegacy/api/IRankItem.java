package net.xiaoyang010.ex_enigmaticlegacy.api;

import net.minecraft.world.item.ItemStack;
import vazkii.botania.api.mana.IManaItem;

public interface IRankItem extends IManaItem {
    int getLevel(ItemStack stack);
    int[] getLevels();
}