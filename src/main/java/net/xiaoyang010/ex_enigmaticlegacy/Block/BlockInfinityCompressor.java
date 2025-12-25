package net.xiaoyang010.ex_enigmaticlegacy.Block;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileEntityInfinityCompressor;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.WrenchHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockInfinityCompressor extends BaseEntityBlock {
    public static final Property<Direction> FACING;

    public BlockInfinityCompressor(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new TileEntityInfinityCompressor(ModBlockEntities.INFINITY_COMPRESSOR_TILE.get(), pos, state);
    }


    @Nonnull
    @Override
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state,
                            @Nullable LivingEntity placer, @Nonnull ItemStack itemStack) {
        if (level.isClientSide || placer == null)
            return;

        Direction facing = Direction.fromYRot(placer.getYRot()).getOpposite();
        level.setBlock(pos, state.setValue(FACING, facing), 3);

        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityInfinityCompressor) {
            final CompoundTag stackNBT = itemStack.getTag();
            if (stackNBT != null)
                ((TileEntityInfinityCompressor) blockEntity).readCustomNBT(stackNBT);
        }
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nonnull
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Nonnull
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nonnull
    @Override
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
                                 @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (!level.isClientSide && !player.isShiftKeyDown() && !WrenchHelper.INSTANCE.isWrench(player.getItemInHand(hand))) {
            final BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MenuProvider) {
                NetworkHooks.openGui((ServerPlayer) player, (MenuProvider) blockEntity, pos);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
                         @Nonnull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            final TileEntityInfinityCompressor tileEntity = (TileEntityInfinityCompressor) level.getBlockEntity(pos);
            if (tileEntity != null) {
                final ItemStack droppedStack = new ItemStack(this, 1);
                final CompoundTag nbtTagCompound = tileEntity.writeCustomNBT(new CompoundTag());
                if (!nbtTagCompound.isEmpty())
                    droppedStack.setTag(nbtTagCompound);

                ItemEntity itemEntity = new ItemEntity(level,
                        pos.getX() + level.random.nextFloat() * 0.8F + 0.1F,
                        pos.getY() + level.random.nextFloat() * 0.8F + 0.1F,
                        pos.getZ() + level.random.nextFloat() * 0.8F + 0.1F,
                        droppedStack);
                level.addFreshEntity(itemEntity);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    static {
        FACING = BlockStateProperties.HORIZONTAL_FACING;
    }
}