package org.xiaoyang.ex_enigmaticlegacy.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.xiaoyang.ex_enigmaticlegacy.Tile.AstralBlockEntity;

public class AstralBlock extends Block implements EntityBlock {
    public AstralBlock(Properties properties) {
        super(properties.explosionResistance(120000000000.0F).lightLevel(state -> 7));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new AstralBlockEntity(ModBlockEntities.ASTRAL_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);
    }
}