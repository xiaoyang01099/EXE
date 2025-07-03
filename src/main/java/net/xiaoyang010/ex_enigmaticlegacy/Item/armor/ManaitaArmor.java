package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class ManaitaArmor extends ArmorItem {
    protected static final Properties MANAITA_ARMOR = new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR).rarity(ModRarities.MIRACLE);

    public ManaitaArmor(EquipmentSlot pSlot) {
        super(new ZMaterial(), pSlot, MANAITA_ARMOR);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return ImmutableMultimap.of();
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}
