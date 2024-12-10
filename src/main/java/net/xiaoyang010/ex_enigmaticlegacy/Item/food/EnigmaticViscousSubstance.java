package net.xiaoyang010.ex_enigmaticlegacy.Item.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;

public class EnigmaticViscousSubstance {

    public static final Item ENIGMATICVISCOUSSUBSTANCE = new Item(new Properties()
            .tab(ModTabs.TAB_EXENIGMATICLEGACY_FOOD)
            .rarity(ModRarities.MIRACLE)
            .food(new FoodProperties.Builder()
                    .nutrition(90)
                    .saturationMod(0.6F)
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, Integer.MAX_VALUE, 1), 1.0F)
                    .effect(() -> new MobEffectInstance(ModEffects.EMESIS.get(), 500, 1), 1.0F)
                    .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 100), 1.0F)
                    .alwaysEat()
                    .build())
    );
}