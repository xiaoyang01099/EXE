package org.xiaoyang.ex_enigmaticlegacy.Enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

// AutoTrackingEnchantment 让魔法弓拉弓时可以客户端锁定目标并在发射时朝目标方向射击。
public class AutoTrackingEnchantment extends Enchantment {
    public AutoTrackingEnchantment() {
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
