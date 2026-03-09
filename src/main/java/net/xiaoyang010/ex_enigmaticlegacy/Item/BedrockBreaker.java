package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.entity.item.ItemEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class BedrockBreaker extends Item {
    public BedrockBreaker() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM));
    }

    @Override
    public boolean isFireResistant() {
        return true;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        entity.setInvulnerable(true);
        return super.onEntityItemUpdate(stack, entity);
    }
}
