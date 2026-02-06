package net.xiaoyang010.ex_enigmaticlegacy.api;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTags;

import java.util.List;

public class EXEAPI {
    public static final Tier MIRACLE_ITEM_TIER = TierSortingRegistry.registerTier(
            new ForgeTier(
                    100,
                    -1,
                    5201314F,
                    100.0F,
                    128,
                        ModTags.Blocks.NEEDS_MIRACLE_TOOL, () -> Ingredient.of(ModItems.INFINITYDROP.get())
            ),
            new ResourceLocation("ex_enigmaticlegacy", "miracle"), List.of(Tiers.NETHERITE), List.of()
    );

    public static final ForgeTier mithrilToolMaterial = new ForgeTier(
            50,
            -1,
            20.0F,
            60.0F,
            1982,
            ModTags.Blocks.NEEDS_MIRACLE_TOOL,
            () -> Ingredient.EMPTY
    );

    public static final ArmorMaterial nebulaArmorMaterial;
    public static final ArmorMaterial wildHuntArmor;

    public static final Rarity rarityNebula;
    public static final Rarity rarityWildHunt;

    static {
        nebulaArmorMaterial = new CustomArmorMaterial(
                "nebula",
                0,
                new int[]{10, 40, 30, 20},
                26,
                null,
                100.0F,
                100.0F
        );

        wildHuntArmor = new CustomArmorMaterial(
                "wild_hunt",
                -1,
                new int[]{10, 40, 30, 20},
                30,
                null,
                100.0F,
                100.0F
        );

        rarityNebula = Rarity.create("NEBULA", ChatFormatting.LIGHT_PURPLE);
        rarityWildHunt = Rarity.create("WILD_HUNT", ChatFormatting.DARK_GRAY);
    }
}


class CustomArmorMaterial implements ArmorMaterial {
    private final String name;
    private final int durabilityMultiplier;
    private final int[] protectionAmounts;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;

    public CustomArmorMaterial(String name, int durabilityMultiplier, int[] protectionAmounts,
                               int enchantability, SoundEvent equipSound, float toughness, float knockbackResistance) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protectionAmounts = protectionAmounts;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
    }

    @Override
    public int getDurabilityForSlot(EquipmentSlot equipmentSlot) {
        return 0;
    }

    @Override
    public int getDefenseForSlot(EquipmentSlot equipmentSlot) {
        return 0;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public SoundEvent getEquipSound() {
        return null;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public float getToughness() {
        return 0;
    }

    @Override
    public float getKnockbackResistance() {
        return 0;
    }
}