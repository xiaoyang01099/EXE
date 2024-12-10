package net.xiaoyang010.ex_enigmaticlegacy.Item.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class TabooApex {

    public static final Item TabooApex = new Item(new Properties()
            .tab(ModTabs.TAB_EXENIGMATICLEGACY_FOOD)
            .rarity(ModRarities.MIRACLE)
            .food(new FoodProperties.Builder()
                    .nutrition(90)
                    .saturationMod(0.6F)
                    .effect(() -> new MobEffectInstance(ModEffects.CREEPER_FRIENDLY.get(), Integer.MAX_VALUE, 0), 1.0F)   // 无限时间，满级的爬行者友好
                    .effect(() -> new MobEffectInstance(ModEffects.DAMAGE_REDUCTION.get(), Integer.MAX_VALUE, 1), 1.0F)   // 无限时间，满级的减伤
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, Integer.MAX_VALUE, 255), 1.0F)   // 无限时间，满级的再生
                    .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, Integer.MAX_VALUE, 255), 1.0F)   // 无限时间，满级的力量
                    .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 255), 1.0F)   // 无限时间，满级的火焰抗性
                    .effect(() -> new MobEffectInstance(MobEffects.WATER_BREATHING, Integer.MAX_VALUE, 255), 1.0F)   // 无限时间，满级的水下呼吸
                    .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, Integer.MAX_VALUE, 255), 1.0F)   // 无限时间，满级的护盾
                    .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 255), 1.0F)   // 无限时间，满级的抗性提升
                    .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, Integer.MAX_VALUE, 255), 1.0F)   // 无限时间，满级的生命提升
                    .effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 255), 1.0F)   // 无限时间，满级的夜视
                    .effect(() -> new MobEffectInstance(MobEffects.INVISIBILITY, Integer.MAX_VALUE, 255), 1.0F)   // 无限时间，满级的隐身
                    .effect(() -> new MobEffectInstance(MobEffects.SATURATION, Integer.MAX_VALUE, 255), 1.0F)   // 无限时间，满级的饱和
                    .effect(() -> new MobEffectInstance(MobEffects.LUCK, Integer.MAX_VALUE, 255), 1.0F)   // 无限时间，满级的幸运
                    .effect(() -> new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, Integer.MAX_VALUE, 255), 1.0F)   // 无限时间，满级的村庄英雄
                    .effect(() -> new MobEffectInstance(MobEffects.SLOW_FALLING, Integer.MAX_VALUE, 255), 1.0F)   // 无限时间，满级的缓降
                    .effect(() -> new MobEffectInstance(MobEffects.CONDUIT_POWER, Integer.MAX_VALUE, 255), 1.0F)   // 无限时间，满级的潮涌能量
                    .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 9), 1.0F)   // 无限时间，10级的速度提升 (9表示10级)
                    .effect(() -> new MobEffectInstance(MobEffects.JUMP, Integer.MAX_VALUE, 9), 1.0F)   // 无限时间，10级的跳跃提升 (9表示10级)
                    .alwaysEat()
                    .build())
    );
}