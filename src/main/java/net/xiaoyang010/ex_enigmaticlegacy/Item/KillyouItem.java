
package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Item;

public class KillyouItem extends SwordItem {
	public KillyouItem() {
		super(new Tier() {
			public int getUses() {
				return 0;
			}

			public float getSpeed() {
				return 4f;
			}

			public float getAttackDamageBonus() {
				return 19f;
			}

			public int getLevel() {
				return 1;
			}

			public int getEnchantmentValue() {
				return 2;
			}

			public Ingredient getRepairIngredient() {
				return Ingredient.of();
			}
		}, 3, 96f, new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR));
	}
}
