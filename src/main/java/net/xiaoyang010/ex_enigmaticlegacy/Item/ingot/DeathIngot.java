package net.xiaoyang010.ex_enigmaticlegacy.Item.ingot;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class DeathIngot extends Item {
    public DeathIngot() {
        super(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_MINERAL).stacksTo(64).rarity(Rarity.COMMON));
    }
}
