package org.xiaoyang.ex_enigmaticlegacy.Block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class DecayBlock extends Block {
    public DecayBlock() {
        super(Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(3f, 10f)
                .requiresCorrectToolForDrops());
    }
}
