package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.Util.ColorText;

public class InfinityMatter extends Item {
    public InfinityMatter() {
        super(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM)
                .stacksTo(64)
                .fireResistant()
                .rarity(ModRarities.MIRACLE));
    }

    @Override
    public Component getName(ItemStack p_41458_) {
        return Component.nullToEmpty(ColorText.GetColor1("无尽物质"));
    }
}
