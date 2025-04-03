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
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.TileInventory;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class NidavellirForgeBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    protected static final VoxelShape SHAPE_NORTH = Block.box(3, 0, 1, 13, 12, 16);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(3, 0, 0, 13, 12, 15);
    protected static final VoxelShape SHAPE_EAST = Block.box(0, 0, 3, 15, 12, 13);
    protected static final VoxelShape SHAPE_WEST = Block.box(1, 0, 3, 16, 12, 13);

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
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player.isShiftKeyDown()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileNidavellirForge tile) {
                // 处理主物品槽
                if (!tile.getItem(0).isEmpty()) {
                    dropItemWithDirection(level, player, tile.getItem(0).copy());
                    tile.setItem(0, ItemStack.EMPTY);
                    tile.requestUpdate = true;
                    level.updateNeighbourForOutputSignal(pos, this);
                    return InteractionResult.SUCCESS;
                }

                // 处理其他物品槽
                for (int i = tile.getContainerSize() - 1; i > 0; i--) {
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
            if (blockEntity instanceof TileInventory inventory) {
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
        return new TileNidavellirForge(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType,
                ModBlockEntities.NIDAVELLIR_FORGE_TILE.get(),
                TileNidavellirForge::serverTick);
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
}