package net.xiaoyang010.ex_enigmaticlegacy.Item.armor.UV;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.UltimateValkyrie;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UltimateValkyrieBoots extends UltimateValkyrie {

    public UltimateValkyrieBoots(Properties properties) {
        super(EquipmentSlot.FEET, properties.fireResistant().stacksTo(1));
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide) {
            player.fallDistance = 0;
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.nullToEmpty("§c武神战靴");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltipComponents, flag);
        tooltipComponents.add(Component.nullToEmpty("§7》§a轻灵之步："));
        tooltipComponents.add(Component.nullToEmpty("§7穿戴者免疫摔落伤害。"));
    }
}