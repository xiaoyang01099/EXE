package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class SMaterial implements ArmorMaterial {

    private static final int[] DURABILITY_PER_SLOT = new int[]{13, 15, 16, 11};
    private static final int[] DEFENSE_PER_SLOT = new int[]{3, 6, 8, 3};
    private static final int ENCHANTABILITY = 15;
    private final Ingredient repairIngredient;

    public SMaterial() {
        this.repairIngredient = Ingredient.of(Items.GOLD_INGOT); // 修复盔甲的物品（钻石）
    }

    @Override
    public int getDurabilityForSlot(EquipmentSlot slot) {
        return DURABILITY_PER_SLOT[slot.getIndex()] * 37; // 设置每个盔甲部位的耐久度
    }

    @Override
    public int getDefenseForSlot(EquipmentSlot slot) {
        return DEFENSE_PER_SLOT[slot.getIndex()]; // 设置每个盔甲部位的防御值
    }

    @Override
    public int getEnchantmentValue() {
        return ENCHANTABILITY; // 设置附魔值
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_GOLD; // 穿上盔甲的声音
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient; // 修复材料
    }

    @Override
    public String getName() {
        return "ex_enigmaticlegacy:crown"; // 盔甲材质的名称
    }

    @Override
    public float getToughness() {
        return 2.0F; // 韧性
    }

    @Override
    public float getKnockbackResistance() {
        return 1F; // 设置击退抗性
    }
}