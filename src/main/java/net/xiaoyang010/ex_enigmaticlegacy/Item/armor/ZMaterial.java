package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class ZMaterial implements ArmorMaterial {

    // 无限防御：为每个盔甲部位设置极大的防御值
    private static final int[] DEFENSE_PER_SLOT = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};

    // 这个属性我们保持不变，用于计算盔甲的附魔能力
    private static final int ENCHANTABILITY = Integer.MAX_VALUE; // 极高的附魔值

    // 盔甲的修复材料为钻石
    private final Ingredient repairIngredient = Ingredient.of(Items.DIAMOND);

    public ZMaterial() {
        // 构造函数可以保留现状
    }

    @Override
    public int getDurabilityForSlot(EquipmentSlot slot) {
        return Integer.MAX_VALUE; // 无限耐久，盔甲几乎不会损坏
    }

    @Override
    public int getDefenseForSlot(EquipmentSlot slot) {
        return DEFENSE_PER_SLOT[slot.getIndex()]; // 极高的防御值
    }

    @Override
    public int getEnchantmentValue() {
        return ENCHANTABILITY; // 极高的附魔值
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_DIAMOND; // 穿上盔甲的声音
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient; // 修复材料设置为钻石
    }

    @Override
    public String getName() {
        return "ex_enigmaticlegacy:manaita"; // 盔甲材质的名称
    }

    @Override
    public float getToughness() {
        return 10240f; // 无限韧性
    }

    @Override
    public float getKnockbackResistance() {
        return 102.4f; // 无限击退抗性
    }
}
