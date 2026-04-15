package org.xiaoyang.ex_enigmaticlegacy.Item.res;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.xiaoyang.ex_enigmaticlegacy.Util.ColorText;

public class InfinityMatter extends Item {
    public InfinityMatter() {
        super(new Properties().stacksTo(64).fireResistant());
    }

    @Override
    public Component getName(ItemStack p_41458_) {
        return Component.nullToEmpty(ColorText.GetColor1("无尽物质"));
    }
}
