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
            // 获取目标实体的当前生命值
            float currentHealth = target.getHealth();
            // 计算30%当前生命值的伤害
            float damage = currentHealth * 0.30f;

            // 直接设置生命值而不是造成伤害
            target.setHealth(currentHealth - damage);
        }

        // 调用父类的hurtEnemy方法以保持原有的其他效果
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 添加伤害说明
        tooltip.add(new TranslatableComponent("tooltip.damage.percent_health"));
    }
}