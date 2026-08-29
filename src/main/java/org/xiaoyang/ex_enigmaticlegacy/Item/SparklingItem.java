package org.xiaoyang.ex_enigmaticlegacy.Item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigFile;
import org.xiaoyang.ex_enigmaticlegacy.Event.SparklingEventHandler;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEffects;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModSounds;

import java.util.List;

public class SparklingItem extends Item {
    private static final Component TOOLTIP_1 = Component.translatable("item.exe.sparkling.tooltip.1");
    private static final Component TOOLTIP_2 = Component.translatable("item.exe.sparkling.tooltip.2");
    public static final FoodProperties SPARKLING_FRUIT_FOOD = new FoodProperties.Builder()
            .nutrition(20)
            .saturationMod(1.2F)
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 4), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 20 * 30, 4), 1.0F)
            .alwaysEat()
            .build();

    public SparklingItem(Properties properties) {
        super(properties.food(SPARKLING_FRUIT_FOOD));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(TOOLTIP_1);
        tooltip.add(TOOLTIP_2);
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide()) {
            int duration = ConfigFile.sparklingBuffDurationTicks();
            entity.addEffect(new MobEffectInstance(
                    ModEffects.SPARKLING_EFFECT.get(),
                    duration,
                    0,
                    false,
                    true,
                    true
            ));
            if (entity instanceof Player) {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, ConfigFile.sparklingSpeedAmplifier(), false, true, true));
                entity.addEffect(new MobEffectInstance(MobEffects.JUMP, duration, ConfigFile.sparklingJumpAmplifier(), false, true, true));
            }
            SparklingEventHandler.syncOutlineRefresh(entity);
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    ModSounds.AAAAA.get(), entity.getSoundSource(), 1.0F, 1.0F);
        }
        return result;
    }
}
