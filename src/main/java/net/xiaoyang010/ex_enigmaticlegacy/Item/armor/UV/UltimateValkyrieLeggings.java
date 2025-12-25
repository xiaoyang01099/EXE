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

public class UltimateValkyrieLeggings extends UltimateValkyrie {

    public UltimateValkyrieLeggings(Properties properties) {
        super(EquipmentSlot.LEGS, properties.fireResistant().stacksTo(1));
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2, 1, false, false));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.nullToEmpty("§c武神战裙");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltipComponents, flag);
        tooltipComponents.add(Component.nullToEmpty("§7》§b迅捷之力："));
        tooltipComponents.add(Component.nullToEmpty("§7穿戴者获得永久速度提升。"));
    }
}