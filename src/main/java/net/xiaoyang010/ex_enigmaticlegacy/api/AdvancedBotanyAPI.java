package net.xiaoyang010.ex_enigmaticlegacy.api;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;


import java.util.ArrayList;
import java.util.List;

public class AdvancedBotanyAPI {
    public static final List<ResourceLocation> achievements = new ArrayList<>();
    public static final List<ItemLike> relicList = new ArrayList<>();
    public static final List<ItemLike> diceList = new ArrayList<>();

    public static final Tier MITHRIL_ITEM_TIER = TierSortingRegistry.registerTier(
            new ForgeTier(7, -1, 8.0F, 4.0F, 24,
                    net.minecraft.tags.BlockTags.NEEDS_DIAMOND_TOOL,
                    () -> Ingredient.EMPTY),
            new ResourceLocation("ex_enigmaticlegacy", "mithril"),
            List.of(net.minecraft.world.item.Tiers.NETHERITE),
            List.of()
    );

    public static final ArmorMaterial nebulaArmorMaterial;
    public static final ArmorMaterial wildHuntArmor;

    public static final Rarity rarityNebula;
    public static final Rarity rarityWildHunt;

    static {
        //使用自定义实现初始化装甲材质
        nebulaArmorMaterial = new CustomArmorMaterial(
                "nebula",
                0,
                new int[]{3, 8, 6, 3},
                26,
                null,
                0.0F,
                0.0F
        );

        wildHuntArmor = new CustomArmorMaterial(
                "wild_hunt",
                34,
                new int[]{7, 8, 3, 2},
                26,
                null,
                0.0F,
                0.0F
        );

        // 创建自定义稀有度
        rarityNebula = Rarity.create("NEBULA", ChatFormatting.LIGHT_PURPLE);
        rarityWildHunt = Rarity.create("WILD_HUNT", ChatFormatting.DARK_AQUA);
    }
}

// 自定义装甲材料实现（需要根据您的特定需求完成）
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