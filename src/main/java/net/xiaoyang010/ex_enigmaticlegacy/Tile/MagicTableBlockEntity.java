package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.BlockEntityBase;

public class MagicTableBlockEntity extends BlockEntityBase {

    public MagicTableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Capability<?>... caps) {
        super(type, pos, state, caps);
    }
}