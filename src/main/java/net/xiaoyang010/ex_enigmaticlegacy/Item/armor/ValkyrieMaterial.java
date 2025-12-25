package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class ValkyrieMaterial implements ArmorMaterial {

    public static final ValkyrieMaterial INSTANCE = new ValkyrieMaterial();

    @Override
    public int getDurabilityForSlot(EquipmentSlot slot) {
        return 1024;
    }

    @Override
    public int getDefenseForSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 1024;
            case CHEST -> 1024;
            case LEGS -> 1024;
            case FEET -> 1024;
            default -> 0;
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