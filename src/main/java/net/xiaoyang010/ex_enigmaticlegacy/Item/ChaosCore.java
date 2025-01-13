package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

import javax.annotation.Nullable;
import java.util.List;

public class ChaosCore extends Item {
    public ChaosCore() {
        super(new Item.Properties()
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM)
                .stacksTo(1)
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.chaos_core.tooltip"));
        super.appendHoverText(stack, world, tooltip, flag);
    }
}
