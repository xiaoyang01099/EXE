
package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class MiaomiaotouItem extends Item {
	public MiaomiaotouItem() {
		super(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM).stacksTo(1).rarity(Rarity.EPIC));
	}
}
