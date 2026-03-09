package net.xiaoyang010.ex_enigmaticlegacy.Block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class DecayBlock extends Block {
    public DecayBlock() {
        super(Properties.of(Material.STONE)
                .sound(SoundType.STONE)
                .strength(3f, 10f)
                .requiresCorrectToolForDrops());
    }
}
