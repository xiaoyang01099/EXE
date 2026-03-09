package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

import javax.annotation.Nullable;
import java.util.List;

public class ChaosCore extends Item {
    public ChaosCore() {
        super(new Properties()
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM)
                .stacksTo(1)
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && Math.random() <= 2.08E-4) {
            if (entity instanceof LivingEntity livingEntity) {
                MobEffectInstance randomEffect = getRandomPotionEffect();
                if (randomEffect != null) {
                    livingEntity.addEffect(randomEffect);
                }
            }
        }
    }

    private MobEffectInstance getRandomPotionEffect() {
        var effects = new MobEffectInstance[]{
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, getRandomDuration(), getRandomAmplifier()),
                new MobEffectInstance(MobEffects.DIG_SPEED, getRandomDuration(), getRandomAmplifier()),
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, getRandomDuration(), getRandomAmplifier()),
                new MobEffectInstance(MobEffects.HEAL, 1, getRandomAmplifier()),
                new MobEffectInstance(MobEffects.JUMP, getRandomDuration(), getRandomAmplifier()),
                new MobEffectInstance(MobEffects.REGENERATION, getRandomDuration(), getRandomAmplifier()),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, getRandomDuration(), getRandomAmplifier()),
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, getRandomDuration(), getRandomAmplifier()),
                new MobEffectInstance(MobEffects.WATER_BREATHING, getRandomDuration(), getRandomAmplifier()),
                new MobEffectInstance(MobEffects.INVISIBILITY, getRandomDuration(), 0),
                new MobEffectInstance(MobEffects.NIGHT_VISION, getRandomDuration(), 0),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, getRandomDuration(), getRandomAmplifier()),
                new MobEffectInstance(MobEffects.ABSORPTION, getRandomDuration(), getRandomAmplifier()),
                new MobEffectInstance(MobEffects.SATURATION, getRandomDuration(), getRandomAmplifier()),
                new MobEffectInstance(MobEffects.GLOWING, getRandomDuration(), 0),
                new MobEffectInstance(MobEffects.LUCK, getRandomDuration(), getRandomAmplifier()),
                new MobEffectInstance(MobEffects.SLOW_FALLING, getRandomDuration(), 0),
                new MobEffectInstance(MobEffects.CONDUIT_POWER, getRandomDuration(), 0),
                new MobEffectInstance(MobEffects.DOLPHINS_GRACE, getRandomDuration(), 0),
                new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, getRandomDuration(), getRandomAmplifier())
        };

        return effects[(int) (Math.random() * effects.length)];
    }

    private int getRandomDuration() {
        return 100 + (int) (Math.random() * 2400);
    }

    private int getRandomAmplifier() {
        return (int) (Math.random() * 3);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.chaos_core.lore1")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.chaos_core.lore2")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.chaos_core.lore3")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(new TranslatableComponent(""));

            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.chaos_core.tooltip")
                    .withStyle(ChatFormatting.DARK_PURPLE));

            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.chaos_core.lore4")
                    .withStyle(ChatFormatting.RED));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.chaos_core.lore5")
                    .withStyle(ChatFormatting.RED));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.chaos_core.lore6")
                    .withStyle(ChatFormatting.RED));
            tooltip.add(new TranslatableComponent(""));

            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.chaos_core.lore7")
                    .withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.shift_tooltip")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}