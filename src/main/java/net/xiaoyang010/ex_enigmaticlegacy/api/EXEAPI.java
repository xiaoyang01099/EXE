package net.xiaoyang010.ex_enigmaticlegacy.api;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.TerraFarmlandList;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTags;
import java.util.ArrayList;
import java.util.List;

public class EXEAPI {
    public static List<TerraFarmlandList> farmlandList = new ArrayList<>();
    public static List<ItemStack> relicList = new ArrayList<>();
    public static List<ItemStack> diceList = new ArrayList<>();

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
        //使用自定义实现初始化装甲材质
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
                "wild_hunt",//护甲材质的标识符
                -1,//护甲的耐久度乘数
                new int[]{10, 40, 30, 20},//护甲各部位提供的护甲值数组
                30,//附魔能力值
                null,//装备时播放的声音效果
                100.0F,//护甲韧性
                100.0F//击退抗性
        );

        // 创建自定义稀有度
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