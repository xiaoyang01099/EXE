package org.xiaoyang.ex_enigmaticlegacy.Block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class ArcaneIceChunk extends Block {
    public ArcaneIceChunk() {
        super(Properties.of().mapColor(MapColor.ICE).sound(SoundType.GLASS).strength(5f, 10f).requiresCorrectToolForDrops());
    }
}
