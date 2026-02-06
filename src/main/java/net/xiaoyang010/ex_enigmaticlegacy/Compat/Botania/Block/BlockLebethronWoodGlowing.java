package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockLebethronWoodGlowing extends Block {

    public BlockLebethronWoodGlowing(Properties properties) {
        super(properties
                .sound(SoundType.WOOD)
                .strength(6.0F)
                .lightLevel(state -> 12));
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 12;
    }
}
