package net.xiaoyang010.ex_enigmaticlegacy.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.InfinityChestEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.PagedChestBlockEntity;

import java.util.Random;

public class PagedChestBlock extends BaseEntityBlock implements SimpleWaterloggedBlock{
    public static final DirectionProperty FACING;
    public static final BooleanProperty WATERLOGGED;
    protected static final VoxelShape SHAPE;
    //public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape  AABB = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

    public PagedChestBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    protected Stat<ResourceLocation> getOpenChestStat() {
        return Stats.CUSTOM.get(Stats.OPEN_CHEST);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return AABB;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PagedChestBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PagedChestBlockEntity) {
            NetworkHooks.openGui((ServerPlayer) player, (PagedChestBlockEntity) blockEntity, pos);
            player.awardStat(this.getOpenChestStat());
            PiglinAi.angerNearbyPiglins(player, true);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PagedChestBlockEntity) {
                ((PagedChestBlockEntity) blockEntity).dropContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        BlockEntity $$4 = pLevel.getBlockEntity(pPos);
        if ($$4 instanceof InfinityChestEntity) {
            ((InfinityChestEntity)$$4).recheckOpen();
        }

    }

    static {
        FACING = HorizontalDirectionalBlock.FACING;
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
        SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);
    }
}