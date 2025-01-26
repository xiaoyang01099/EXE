package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

import javax.annotation.Nullable;
import java.util.List;

public class SongOfTheAbyss extends SwordItem {

    public SongOfTheAbyss() {
        super(Tiers.NETHERITE, 150, -2.4F, new Item.Properties()
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                .rarity(ModRarities.MIRACLE));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level.isClientSide) {

            float currentHealth = target.getHealth();
            float damage = currentHealth * 0.30f;
            target.setHealth(currentHealth - damage);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(new TranslatableComponent("tooltip.damage.percent_health"));
    }
}