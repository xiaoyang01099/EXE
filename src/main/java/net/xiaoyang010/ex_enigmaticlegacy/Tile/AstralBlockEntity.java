package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class AstralBlockEntity extends BlockEntity {
    public AstralBlockEntity(@NotNull BlockEntityType<AstralBlockEntity> EntityBlockEntityType, BlockPos blockPos, BlockState blockState) {
        super(EntityBlockEntityType, blockPos, blockState);
    }
}
