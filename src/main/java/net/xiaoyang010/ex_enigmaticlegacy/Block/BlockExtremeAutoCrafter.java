package net.xiaoyang010.ex_enigmaticlegacy.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileEntityExtremeAutoCrafter;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.WrenchHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BlockExtremeAutoCrafter extends BaseEntityBlock {
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(blockEntityType, ModBlockEntities.EXTREME_AUTO_CRAFTER_TILE.get(),
                (level1, pos, state1, blockEntity) -> TileEntityExtremeAutoCrafter.tick(blockEntity));
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                 @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!level.isClientSide && !player.isCrouching() && !WrenchHelper.INSTANCE.isWrench(player.getItemInHand(hand))) {
            final BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileEntityExtremeAutoCrafter) {
                NetworkHooks.openGui((ServerPlayer) player, (MenuProvider) blockEntity, pos);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.FAIL;
        } else if (!level.isClientSide && player.isCrouching() && WrenchHelper.INSTANCE.isWrench(player.getItemInHand(hand)) &&
                level.getBlockEntity(pos) instanceof TileEntityExtremeAutoCrafter) {
            level.destroyBlock(pos, false);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                            @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        if (level == null)
            return;
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityExtremeAutoCrafter && stack.hasTag()) {
            ((TileEntityExtremeAutoCrafter) blockEntity).readCustomNBT(stack.getTag());
            ((TileEntityExtremeAutoCrafter) blockEntity).recipeShapeChanged();
        }
    }

    @NotNull
    @Override
    public RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                         @NotNull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            final TileEntityExtremeAutoCrafter tileEntity = (TileEntityExtremeAutoCrafter) level.getBlockEntity(pos);
            if (tileEntity != null) {
                final ItemStack droppedStack = new ItemStack(this);
                final CompoundTag compoundTag = tileEntity.writeCustomNBT(new CompoundTag());
                if (!compoundTag.getList("Contents", 10).isEmpty())
                    droppedStack.setTag(compoundTag);

                double x = pos.getX() + RANDOM.nextFloat() * 0.8F + 0.1F;
                double y = pos.getY() + RANDOM.nextFloat() * 0.8F + 0.1F;
                double z = pos.getZ() + RANDOM.nextFloat() * 0.8F + 0.1F;

                ItemEntity itemEntity = new ItemEntity(level, x, y, z, droppedStack);
                level.addFreshEntity(itemEntity);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> serverType, BlockEntityType<E> clientType, BlockEntityTicker<? super E> ticker) {
        return clientType == serverType ? (BlockEntityTicker<A>) ticker : null;
    }
}