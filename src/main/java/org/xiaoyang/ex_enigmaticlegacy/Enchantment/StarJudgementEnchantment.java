package org.xiaoyang.ex_enigmaticlegacy.Enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

// StarJudgementEnchantment 让魔法弓获得星辰裁决触发资格。
public class StarJudgementEnchantment extends Enchantment {
    public StarJudgementEnchantment() {
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
