package net.xiaoyang010.ex_enigmaticlegacy.api;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTags;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.RecipeAdvancedPlate;


import java.util.ArrayList;
import java.util.List;

public class AdvancedBotanyAPI {
    public static final List<RecipeAdvancedPlate> advancedPlateRecipes = new ArrayList<>();
    public static final List<ResourceLocation> achievements = new ArrayList<>();
    public static final List<ItemLike> relicList = new ArrayList<>();
    public static final List<ItemLike> diceList = new ArrayList<>();

    public static final Tier MITHRIL_ITEM_TIER = TierSortingRegistry.registerTier(
            new ForgeTier(
                    15,      // 挖掘等级
                    -1,     // 耐久度 (-1 表示无限)
                    8.0F,   // 挖掘速度
                    4.0F,   // 基础攻击伤害
                    24,     // 附魔值
                    ModTags.Blocks.NEEDS_MITHRIL_TOOL,  // 需要这个等级工具挖掘的方块标签
                    () -> Ingredient.EMPTY              // 修复材料
            ),
            new ResourceLocation("ex_enigmaticlegacy", "mithril"),  // 工具等级ID
            List.of(net.minecraft.world.item.Tiers.NETHERITE),      // 比下界合金强
            List.of()                                               // 比这个等级更强的工具
    );

    public static RecipeAdvancedPlate registerAdvancedPlateRecipe(ItemStack output, ItemStack input1, ItemStack input2, ItemStack input3, int mana, int color) {
        RecipeAdvancedPlate recipe = new RecipeAdvancedPlate(output, mana, color, new ItemStack[]{input1, input2, input3});
        advancedPlateRecipes.add(recipe);
        return recipe;
    }

    public static final ForgeTier mithrilToolMaterial = new ForgeTier(
            7,
            -1,
            8.0F,
            12.0F,
            24,
            ModTags.Blocks.NEEDS_MITHRIL_TOOL,
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
        rarityWildHunt = Rarity.create("WILD_HUNT", ChatFormatting.DARK_AQUA);
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