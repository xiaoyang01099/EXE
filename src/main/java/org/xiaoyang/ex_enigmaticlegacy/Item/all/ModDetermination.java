package org.xiaoyang.ex_enigmaticlegacy.Item.all;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ModDetermination extends Item {
    public ModDetermination() {
        super(new Properties()
                .stacksTo(64)
                .rarity(Rarity.EPIC));
    }
}
