package org.xiaoyang.ex_enigmaticlegacy.Block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;


public class MithrillBlock extends Block {
    public MithrillBlock() {
        super(Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(2f, 10f)
                .requiresCorrectToolForDrops());
    }
}
