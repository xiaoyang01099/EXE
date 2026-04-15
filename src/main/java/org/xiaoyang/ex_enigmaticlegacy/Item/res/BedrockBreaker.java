package org.xiaoyang.ex_enigmaticlegacy.Item.res;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class BedrockBreaker extends Item {
    public BedrockBreaker() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
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
