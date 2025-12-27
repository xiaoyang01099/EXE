package net.xiaoyang010.ex_enigmaticlegacy.Block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.xiaoyang010.ex_enigmaticlegacy.api.IFE.INoEMCItem;

public class AerialiteBlock extends Block {
    public AerialiteBlock() {
        super(BlockBehaviour.Properties.of(Material.STONE).sound(SoundType.STONE).strength(2f, 10f).requiresCorrectToolForDrops());
    }
}
