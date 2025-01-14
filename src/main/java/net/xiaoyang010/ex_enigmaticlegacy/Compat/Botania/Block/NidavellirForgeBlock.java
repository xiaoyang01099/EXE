package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;/*package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;

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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.TileInventory;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlocks;

import javax.annotation.Nullable;

public class NidavellirForgeBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    protected static final VoxelShape SHAPE_NORTH = Block.box(3, 0, 1, 13, 12, 16);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(3, 0, 0, 13, 12, 15);
    protected static final VoxelShape SHAPE_EAST = Block.box(0, 0, 3, 15, 12, 13);
    protected static final VoxelShape SHAPE_WEST = Block.box(1, 0, 3, 16, 12, 13);

    public NidavellirForgeBlock() {
        super(Properties.of(Material.METAL)
                .strength(3.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
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
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_WEST;
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof NidavellirForgeTile tile)) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            if (tile.getItem(0) != null) {
                dropItemFromInventory(level, player, tile, 0);
                return InteractionResult.SUCCESS;
            }

            for (int i = tile.getContainerSize() - 1; i > 0; i--) {
                ItemStack stack = tile.getItem(i);
                if (stack != null) {
                    dropItemFromInventory(level, player, tile, i);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    private void dropItemFromInventory(Level level, Player player, NidavellirForgeTile tile, int slot) {
        ItemStack copy = tile.getItem(slot).copy();
        Vec3 lookVec = player.getLookAngle();
        ItemEntity itemEntity = new ItemEntity(level,
                player.getX() + lookVec.x,
                player.getY() + 1.2D,
                player.getZ() + lookVec.z,
                copy);
        level.addFreshEntity(itemEntity);
        tile.setItem(slot, ItemStack.EMPTY);
        tile.requestUpdate = true;
        level.updateNeighbourForOutputSignal(tile.getBlockPos(), this);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileInventory inv) {
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (!stack.isEmpty()) {
                        double x = pos.getX() + level.random.nextFloat() * 0.8F + 0.1F;
                        double y = pos.getY() + level.random.nextFloat() * 0.8F + 0.1F;
                        double z = pos.getZ() + level.random.nextFloat() * 0.8F + 0.1F;
                        ItemEntity itemEntity = new ItemEntity(level, x, y, z, stack.copy());
                        float speed = 0.05F;
                        itemEntity.setDeltaMovement(
                                level.random.nextGaussian() * speed,
                                level.random.nextGaussian() * speed + 0.2F,
                                level.random.nextGaussian() * speed
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

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlocks.NIDAVELLIR_FORGE_TYPE.get(),
                NidavellirForgeTile::tick);
    }
}*/