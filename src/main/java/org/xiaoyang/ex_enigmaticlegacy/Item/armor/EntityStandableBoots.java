package org.xiaoyang.ex_enigmaticlegacy.Item.armor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.xiaoyang.ex_enigmaticlegacy.Item.armor.ManaitaArmor.MANAITA_ARMOR;

public class EntityStandableBoots extends ArmorItem {

    public EntityStandableBoots(ArmorMaterial material, ArmorItem.Type slot) {
        super(material, slot, MANAITA_ARMOR);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.nullToEmpty("tooltip.ex_enigmaticlegacy.entity_standable_boots"));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}