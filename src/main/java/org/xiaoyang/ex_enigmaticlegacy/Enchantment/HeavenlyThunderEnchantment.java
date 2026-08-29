package org.xiaoyang.ex_enigmaticlegacy.Enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

// HeavenlyThunderEnchantment 让天雷战戟获得按 V 释放天雷法阵技能的资格。
public class HeavenlyThunderEnchantment extends Enchantment {
    public HeavenlyThunderEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.TRIDENT, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

//    @Override
//    public boolean canEnchant(ItemStack stack) {
//        return stack.is(AkatZumaTool.TRIDENT_PLUS.get());
//    }
}
