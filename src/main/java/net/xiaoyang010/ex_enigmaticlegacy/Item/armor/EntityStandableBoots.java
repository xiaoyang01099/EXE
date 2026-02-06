package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityStandableBoots extends ArmorItem {

    public EntityStandableBoots(ArmorMaterial material, Properties properties) {
        super(material, EquipmentSlot.FEET, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.nullToEmpty("tooltip.ex_enigmaticlegacy.entity_standable_boots"));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}