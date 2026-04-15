package org.xiaoyang.ex_enigmaticlegacy.Item.armor;


import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public class DragonWingArmorMaterial implements ArmorMaterial {
    public static final DragonWingArmorMaterial INSTANCE = new DragonWingArmorMaterial();

    private static final int[] DURABILITY_PER_SLOT = new int[]{13, 15, 16, 11};
    private static final int[] PROTECTION_VALUES = new int[]{2, 5, 6, 2};

    @Override
    public int getDurabilityForType(ArmorItem.Type slot) {
        return DURABILITY_PER_SLOT[slot.getSlot().getIndex()];
    }

    @Override
    public int getDefenseForType(ArmorItem.Type slot) {
        return PROTECTION_VALUES[slot.getSlot().getIndex()];
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_ELYTRA;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }

    @Override
    public String getName() {
        return "dragonwing";
    }

    @Override
    public float getToughness() {
        return 0.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.0F;
    }
}