package net.xiaoyang010.ex_enigmaticlegacy.Block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class ArcaneIceChunk extends Block {
    public ArcaneIceChunk() {
        super(Properties.of(Material.ICE).sound(SoundType.GLASS).strength(5f, 10f).requiresCorrectToolForDrops());
    }
}
