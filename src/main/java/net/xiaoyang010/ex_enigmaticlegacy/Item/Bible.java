package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.Item;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class Bible extends BookItem {
    public Bible() {
        super(new Item.Properties()
                .stacksTo(1));
    }
}
