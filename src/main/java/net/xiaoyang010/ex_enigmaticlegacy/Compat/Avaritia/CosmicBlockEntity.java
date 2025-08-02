package net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.jetbrains.annotations.NotNull;

public class CosmicBlockEntity extends BlockEntity {

    public CosmicBlockEntity(@NotNull BlockEntityType<CosmicBlockEntity> BlockEntityType, BlockPos pos, BlockState blockState) {
        super(BlockEntityType, pos, blockState);
    }

    public float getAnimationTime(float partialTicks) {
        if (level != null) {
            return (level.getGameTime() + partialTicks) * 0.05f;
        }
        return 0;
    }
}