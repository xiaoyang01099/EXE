package net.xiaoyang010.ex_enigmaticlegacy.Item.armor.UV;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.UltimateValkyrie;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class UltimateValkyrieHelmet extends UltimateValkyrie {
    UUID uuid = UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150");

    public UltimateValkyrieHelmet(Properties properties) {
        super(EquipmentSlot.HEAD, properties.fireResistant().stacksTo(1));
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(MobEffects.HEAL, 2, 5, false, false));
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