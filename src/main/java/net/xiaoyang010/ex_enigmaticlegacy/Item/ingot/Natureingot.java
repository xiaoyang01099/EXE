
package net.xiaoyang010.ex_enigmaticlegacy.Item.ingot;

import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class Natureingot extends Item {
	public Natureingot() {
		super(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_MINERAL).stacksTo(64).rarity(Rarity.EPIC));
	}
}
