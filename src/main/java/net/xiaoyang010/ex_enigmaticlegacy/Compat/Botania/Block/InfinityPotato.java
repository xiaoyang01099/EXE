package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlocks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
import static vazkii.botania.common.block.BlockMod.createTickerHelper;

public class InfinityPotato extends Block implements SimpleWaterloggedBlock, EntityBlock {

    protected static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 12.0D, 12.0D);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty FACING = HORIZONTAL_FACING;

    public InfinityPotato() {
        super(Properties.of(Material.STONE).strength(1.0f));
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
    }

    // 设置初始状态
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING);
    }

    // 放置方块时的方向与水合属性
    @Nonnull
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER)
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    // 水合状态
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.defaultFluidState() : Fluids.EMPTY.defaultFluidState();
    }

    // 方块状态变化时更新水合状态
    //@Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, Level level, BlockPos pos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level)); // 确保水合状态更新
        }
        return super.updateShape(state, facing, facingState, level, pos, facingPos); // 返回父类的更新形状方法
    }

    // 方块移除时处理物品掉落
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity tileEntity = world.getBlockEntity(pos);
            if (tileEntity instanceof Container) {
                Containers.dropContents(world, pos, (Container) tileEntity);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    // 右键交互触发
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
            BlockEntity tileEntity = world.getBlockEntity(pos);
            if (tileEntity instanceof InfinityPotatoEntity tile) {
                tile.interact(player, hand);
                tile.jumpTicks = 20;

            }
        return InteractionResult.SUCCESS;
    }

    // 设置方块的碰撞箱
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // 方块放置后设置自定义名称
    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity living, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            BlockEntity tileEntity = world.getBlockEntity(pos);
            if (tileEntity instanceof InfinityPotatoEntity) {
                ((InfinityPotatoEntity) tileEntity).setCustomName(stack.getHoverName());
            }
        }
    }

    // 定义渲染方式，使用实体渲染
    @Nonnull
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    // 创建方块实体
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfinityPotatoEntity(pos, state); // 创建并返回一个新的方块实体
    }

    // 设置方块实体的 Ticker
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.INFINITY_POTATO.get(), InfinityPotatoEntity::tick);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, net.minecraft.world.entity.player.Player player) {
        return true;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
        return new ItemStack(ModBlocks.INFINITY_POTATO.get());
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return Collections.singletonList(new ItemStack(ModBlocks.INFINITY_POTATO.get()));
    }
}