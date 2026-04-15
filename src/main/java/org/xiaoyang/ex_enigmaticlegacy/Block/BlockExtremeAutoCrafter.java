package org.xiaoyang.ex_enigmaticlegacy.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.xiaoyang.ex_enigmaticlegacy.Tile.TileEntityExtremeAutoCrafter;

public class BlockExtremeAutoCrafter extends BaseEntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public BlockExtremeAutoCrafter(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        boolean pow = pLevel.hasNeighborSignal(pPos);
        if (pow){
            pLevel.setBlock(pPos, pState.setValue(POWERED, true), 2);
        }else pLevel.setBlock(pPos, pState.setValue(POWERED, false), 2);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TileEntityExtremeAutoCrafter(ModBlockEntities.EXTREME_AUTO_CRAFTER_TILE.get(), pos, state);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(POWERED);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return createTickerHelper(entityType, ModBlockEntities.EXTREME_AUTO_CRAFTER_TILE.get(), TileEntityExtremeAutoCrafter::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileEntityExtremeAutoCrafter) {
                NetworkHooks.openScreen((ServerPlayer) player, (MenuProvider) blockEntity, pos);
            }

            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @NotNull
    @Override
    public RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState blockState, boolean b) {
        if (!state.is(blockState.getBlock())) {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof TileEntityExtremeAutoCrafter tile) {
                tile.dropContents();
                level.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, level, pos, blockState, b);
        }

    }
}