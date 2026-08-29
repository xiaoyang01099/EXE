package org.xiaoyang.ex_enigmaticlegacy.Enchantment;


import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

// AutoShootEnchantment 让魔法弓满蓄后自动射击并继续下一轮蓄力。
public class AutoShootEnchantment extends Enchantment {
    public AutoShootEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BOW, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

//    @Override
//    public boolean canEnchant(ItemStack stack) {
//        return stack.is(AkatZumaTool.MAGIC_BOW.get());
//    }
}
