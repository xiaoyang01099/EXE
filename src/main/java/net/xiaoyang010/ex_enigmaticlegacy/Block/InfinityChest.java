package net.xiaoyang010.ex_enigmaticlegacy.Block;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.ILidBlock;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.InfinityChestEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Menu.InfinityChestMenu;

import java.util.Collections;
import java.util.List;

import static net.minecraft.world.level.block.ChestBlock.getConnectedDirection;

public class InfinityChest extends Block implements EntityBlock, ILidBlock {

    protected static final int AABB_OFFSET = 1;
    protected static final int AABB_HEIGHT = 14;
    protected static final VoxelShape NORTH_AABB = Block.box(1.0D, 0.0D, 0.0D, 15.0D, 14.0D, 15.0D);
    protected static final VoxelShape SOUTH_AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
    protected static final VoxelShape EAST_AABB = Block.box(1.0D, 0.0D, 1.0D, 16.0D, 14.0D, 15.0D);
    protected static final VoxelShape AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    ////////////////////////////////////////
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING; // 定义方向属性
    public static final EnumProperty<ChestType> TYPE = BlockStateProperties.CHEST_TYPE; // 定义箱子类型属性
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED; // 定义水浸属性

    public InfinityChest() {
        super(BlockBehaviour.Properties.of(Material.STONE).sound(SoundType.STONE)
                .strength(1f, 10f)
                .isValidSpawn((state, level, pos, entityType) -> false)
                .isRedstoneConductor((state, level, pos) -> true)
                .isViewBlocking((state, level, pos) -> false));

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TYPE, ChestType.SINGLE)
                .setValue(WATERLOGGED, Boolean.FALSE)); // 设置默认状态

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, WATERLOGGED); // 添加状态定义
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 0; // 最大光阻挡
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        if (!dropsOriginal.isEmpty())
            return dropsOriginal;
        return Collections.singletonList(new ItemStack(this, 1)); // 返回自身
    }

    @Override
    public InteractionResult use(BlockState blockstate, Level world, BlockPos pos, Player entity, InteractionHand hand, BlockHitResult hit) {
        if (entity instanceof ServerPlayer player) {
            world.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f); // 播放开箱音效

            NetworkHooks.openGui(player, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return new TextComponent("Infinity Chest");
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                    return new InfinityChestMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(pos));
                }
            }, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider menuProvider ? menuProvider : null;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfinityChestEntity(pos, state); // 创建新方块实体
    }

    @Override
    public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int eventID, int eventParam) {
        super.triggerEvent(state, world, pos, eventID, eventParam);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity == null ? false : blockEntity.triggerEvent(eventID, eventParam); // 触发事件
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED; // 使用实体方块动画渲染
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof InfinityChestEntity be) {
                Containers.dropContents(world, pos, be); // 掉落物品
                world.updateNeighbourForOutputSignal(pos, this); // 更新红石信号
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true; // 启用红石输出信号
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        BlockEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof InfinityChestEntity be)
            return AbstractContainerMenu.getRedstoneSignalFromContainer(be); // 获取红石信号强度
        else
            return 0;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); // 设置放置方向
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;}

    public BlockState updateShape(BlockState p_51555_, Direction p_51556_, BlockState p_51557_, LevelAccessor p_51558_, BlockPos p_51559_, BlockPos p_51560_) {
        if (p_51555_.getValue(WATERLOGGED)) {
            p_51558_.scheduleTick(p_51559_, Fluids.WATER, Fluids.WATER.getTickDelay(p_51558_));
        }

        if (p_51557_.is(this) && p_51556_.getAxis().isHorizontal()) {
            ChestType chesttype = p_51557_.getValue(TYPE);
            if (p_51555_.getValue(TYPE) == ChestType.SINGLE && chesttype != ChestType.SINGLE && p_51555_.getValue(FACING) == p_51557_.getValue(FACING) && getConnectedDirection(p_51557_) == p_51556_.getOpposite()) {
                return p_51555_.setValue(TYPE, chesttype.getOpposite());
            }
        } else if (getConnectedDirection(p_51555_) == p_51556_) {
            return p_51555_.setValue(TYPE, ChestType.SINGLE);
        }

        return super.updateShape(p_51555_, p_51556_, p_51557_, p_51558_, p_51559_, p_51560_);
    }

    public VoxelShape getShape(BlockState p_51569_, BlockGetter p_51570_, BlockPos p_51571_, CollisionContext p_51572_) {
        if (p_51569_.getValue(TYPE) == ChestType.SINGLE) {
            return AABB;
        } else {
            switch(getConnectedDirection(p_51569_)) {
                case NORTH:
                default:
                    return NORTH_AABB;
                case SOUTH:
                    return SOUTH_AABB;
                case WEST:
                    return WEST_AABB;
                case EAST:
                    return EAST_AABB;
            }
        }
    }
    public float getOpenNess(float v) {
        return 0.0F; // 默认关闭状态
    }

    public static DoubleBlockCombiner.Combiner<ChestBlockEntity, Float2FloatFunction> opennessCombiner(final InfinityChestEntity chestEntity) {
        return new DoubleBlockCombiner.Combiner<ChestBlockEntity, Float2FloatFunction>() {
            @Override
            public Float2FloatFunction acceptDouble(ChestBlockEntity chest1, ChestBlockEntity chest2) {
                return (float tick) -> Math.max(chest1.getOpenNess(tick), chest2.getOpenNess(tick)); // 处理双箱子开合状态
            }

            @Override
            public Float2FloatFunction acceptSingle(ChestBlockEntity chest) {
                return chest::getOpenNess;
            }

            @Override
            public Float2FloatFunction acceptNone() {
                return chestEntity::getOpenNess;
            }
        };
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.getItem() instanceof TieredItem) {
            TieredItem tool = (TieredItem) heldItem.getItem();
            return tool.getTier().getLevel() >= 2;
        }

        return false;
    }
}
