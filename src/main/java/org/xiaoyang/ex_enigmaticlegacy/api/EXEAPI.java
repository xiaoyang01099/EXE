package org.xiaoyang.ex_enigmaticlegacy.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModItems;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModTags;

import java.util.List;

@SuppressWarnings("removal")
public class EXEAPI {
    public static final Tier MIRACLE_ITEM_TIER = TierSortingRegistry.registerTier(
            new ForgeTier(
                    100,
                    -1,
                    5201314F,
                    100.0F,
                    128,
                    ModTags.Blocks.NEEDS_MIRACLE_TOOL,
                    () -> Ingredient.of(ModItems.INFINITYDROP.get())
            ),
            new ResourceLocation("ex_enigmaticlegacy", "miracle"),
            List.of(Tiers.NETHERITE),
            List.of()
    );

    public static final Tier MITHRIL_TOOL_TIER = new ForgeTier(
            50,
            -1,
            20.0F,
            60.0F,
            1982,
            ModTags.Blocks.NEEDS_MIRACLE_TOOL,
            () -> Ingredient.EMPTY
    );

    public static final ArmorMaterial NEBULA_ARMOR_MATERIAL;
    public static final ArmorMaterial WILD_HUNT_ARMOR_MATERIAL;
    static {
        NEBULA_ARMOR_MATERIAL = new CustomArmorMaterial(
                "nebula",
                50,
                new int[]{5, 8, 10, 5},
                26,
                SoundEvents.ARMOR_EQUIP_DIAMOND,
                3.0F,
                0.1F
        );

        WILD_HUNT_ARMOR_MATERIAL = new CustomArmorMaterial(
                "wild_hunt",
                45,
                new int[]{4, 7, 9, 4},
                30,
                SoundEvents.ARMOR_EQUIP_IRON,
                2.5F,
                0.15F
        );
    }


    static class CustomArmorMaterial implements ArmorMaterial {
        private static final int[] BASE_DURABILITY = new int[]{13, 15, 16, 11};
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
        public int getDurabilityForType(ArmorItem.Type slot) {
            return BASE_DURABILITY[slot.getSlot().getIndex()] * this.durabilityMultiplier;
        }

        @Override
        public int getDefenseForType(ArmorItem.Type slot) {
            return this.protectionAmounts[slot.getSlot().getIndex()];
        }

        @Override
        public int getEnchantmentValue() {
            return this.enchantability;
        }

        @Override
        public SoundEvent getEquipSound() {
            return this.equipSound;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }

        @Override
        public String getName() {
            return "ex_enigmaticlegacy:" + this.name;
        }

        @Override
        public float getToughness() {
            return this.toughness;
        }

        @Override
        public float getKnockbackResistance() {
            return this.knockbackResistance;
        }
    }
}