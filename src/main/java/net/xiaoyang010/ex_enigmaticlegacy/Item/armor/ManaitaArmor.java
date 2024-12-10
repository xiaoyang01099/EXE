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

    // 覆盖此方法以隐藏盔甲的属性提示信息
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.FEET) {
            // 返回空的属性映射以隐藏提示信息
            return ImmutableMultimap.of();
        }
        return super.getAttributeModifiers(slot, stack);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false; // 盔甲无法损坏
    }
}
