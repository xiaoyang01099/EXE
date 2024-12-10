
package net.xiaoyang010.ex_enigmaticlegacy.Item;


import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import vazkii.botania.api.mana.ManaItemHandler;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class RegenIvy extends Item {
	public static final String TAG_REGEN = "ex_enigmaticlegacy_regenIvy";
	private static final int MANA_PER_DAMAGE = 200;

	public RegenIvy() {
		super(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).stacksTo(64).fireResistant().rarity(Rarity.EPIC));
	}

	@SubscribeEvent
	public static void onTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END && !event.player.level.isClientSide) {
			for (int i = 0; i < event.player.getInventory().getContainerSize(); i++) {
				ItemStack stack = event.player.getInventory().getItem(i);
				if (!stack.isEmpty()
						&& stack.hasTag()
						&& stack.getTag().getBoolean(TAG_REGEN)
						&& stack.getDamageValue() > 0
						&& ManaItemHandler.instance().requestManaExact(stack, event.player, MANA_PER_DAMAGE, true)) {
					stack.setDamageValue(stack.getDamageValue() - 1);
				}
			}
		}
	}
}