package org.xiaoyang.ex_enigmaticlegacy.Item.armor;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class ValkyrieMaterial implements ArmorMaterial {

    public static final ValkyrieMaterial INSTANCE = new ValkyrieMaterial();

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return 1024;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type slot) {
        return switch (slot) {
            case HELMET -> 1024;
            case CHESTPLATE -> 1024;
            case LEGGINGS -> 1024;
            case BOOTS -> 1024;
        };
    }

    @Override
    public int getEnchantmentValue() {
        return 1024;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_NETHERITE;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(Items.NETHERITE_INGOT);
    }

    @Override
    public String getName() {
        return "ex_enigmaticlegacy:ultimatevalkyrie";
    }

    @Override
    public float getToughness() {
        return 20.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 1.0F;
    }
}