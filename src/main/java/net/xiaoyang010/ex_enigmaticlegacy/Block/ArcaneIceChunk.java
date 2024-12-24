package net.xiaoyang010.ex_enigmaticlegacy.Block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class ArcaneIceChunk extends Block {
    public ArcaneIceChunk() {
        super(BlockBehaviour.Properties.of(Material.STONE).sound(SoundType.STONE).strength(5f, 10f));
    }
}
