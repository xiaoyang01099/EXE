package org.xiaoyang.ex_enigmaticlegacy.Item.armor.UV;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.xiaoyang.ex_enigmaticlegacy.Item.armor.UltimateValkyrie;

import java.util.List;

public class UltimateValkyrieHelmet extends UltimateValkyrie {
    public UltimateValkyrieHelmet(Properties properties) {
        super(ArmorItem.Type.HELMET, properties.fireResistant().stacksTo(1));
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 2, 3, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2, 2, false, false));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.nullToEmpty("§c武神战盔");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltipComponents, flag);
        tooltipComponents.add(Component.nullToEmpty("§7》§c武神意志："));
        tooltipComponents.add(Component.nullToEmpty("§7穿戴者将获得永久生命恢复IV，以及极高的防御。"));
    }
}