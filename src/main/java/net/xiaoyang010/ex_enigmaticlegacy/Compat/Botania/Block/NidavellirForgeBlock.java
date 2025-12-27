package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.NidavellirForgeTile;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;

import javax.annotation.Nullable;

public class NidavellirForgeBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public NidavellirForgeBlock(Properties properties) {
        super(Properties.of(Material.METAL)
                .strength(3.0f, 10.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
                .requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> Block.box(1, 0, 4, 15, 10, 12);
            case EAST -> Block.box(4, 0, 0, 12, 10, 15);
            case WEST -> Block.box(4, 0, 1, 12, 10, 15);
            default -> Block.box(0, 0, 4, 15, 10, 12);
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player.isShiftKeyDown()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof NidavellirForgeTile tile) {

                if (!tile.getItem(NidavellirForgeTile.OUTPUT_SLOT).isEmpty()) {
                    dropItemWithDirection(level, player, tile.getItem(NidavellirForgeTile.OUTPUT_SLOT).copy());
                    tile.setItem(NidavellirForgeTile.OUTPUT_SLOT, ItemStack.EMPTY);
                    tile.requestUpdate = true;
                    level.updateNeighbourForOutputSignal(pos, this);
                    return InteractionResult.SUCCESS;
                }

                for (int i = tile.getContainerSize() - 1; i >= NidavellirForgeTile.FIRST_INPUT_SLOT; i--) {
                    ItemStack stack = tile.getItem(i);
                    if (!stack.isEmpty()) {
                        dropItemWithDirection(level, player, stack.copy());
                        tile.setItem(i, ItemStack.EMPTY);
                        tile.requestUpdate = true;
                        level.updateNeighbourForOutputSignal(pos, this);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }

        return InteractionResult.PASS;
    }

    private void dropItemWithDirection(Level level, Player player, ItemStack stack) {
        Vec3 lookVec = player.getViewVector(1.0F);
        ItemEntity itemEntity = new ItemEntity(level,
                player.getX() + lookVec.x,
                player.getY() + 1.2D,
                player.getZ() + lookVec.z,
                stack);
        level.addFreshEntity(itemEntity);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof NidavellirForgeTile inventory) {
                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    ItemStack stack = inventory.getItem(i);
                    if (!stack.isEmpty()) {
                        float xOffset = level.random.nextFloat() * 0.8F + 0.1F;
                        float yOffset = level.random.nextFloat() * 0.8F + 0.1F;
                        float zOffset = level.random.nextFloat() * 0.8F + 0.1F;

                        ItemEntity itemEntity = new ItemEntity(level,
                                pos.getX() + xOffset,
                                pos.getY() + yOffset,
                                pos.getZ() + zOffset,
                                stack.copy());

                        float motion = 0.05F;
                        itemEntity.setDeltaMovement(
                                level.random.nextGaussian() * motion,
                                level.random.nextGaussian() * motion + 0.2F,
                                level.random.nextGaussian() * motion
                        );

                        level.addFreshEntity(itemEntity);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NidavellirForgeTile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType,
                ModBlockEntities.NIDAVELLIR_FORGE_TILE.get(),
                level.isClientSide ? NidavellirForgeTile::clientTick : NidavellirForgeTile::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int eventID, int eventParam) {
        super.triggerEvent(state, level, pos, eventID, eventParam);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(eventID, eventParam);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof NidavellirForgeTile tile && !level.isClientSide) {
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
}