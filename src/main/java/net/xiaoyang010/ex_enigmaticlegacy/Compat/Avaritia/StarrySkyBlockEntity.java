package net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.jetbrains.annotations.NotNull;

public class StarrySkyBlockEntity extends BlockEntity {

    public StarrySkyBlockEntity(@NotNull BlockEntityType<StarrySkyBlockEntity> starrySkyBlockEntityBlockEntityType, BlockPos pos, BlockState state) {
        super(ModBlockEntities.STARRY_SKY_BLOCK_ENTITY.get(), pos, state);
    }

    public float getAnimationTime(float partialTick) {
        if (level == null) return 0;
        return (level.getGameTime() + partialTick) / 20.0f;
    }
}
