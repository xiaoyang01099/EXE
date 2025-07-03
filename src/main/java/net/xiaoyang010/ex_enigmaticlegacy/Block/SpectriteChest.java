package net.xiaoyang010.ex_enigmaticlegacy.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Container.SpectriteChestContainer;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.SpectriteChestTile;
import org.jetbrains.annotations.Nullable;

import java.awt.Container;
import java.util.Optional;

public class SpectriteChest extends ChestBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<ChestType> TYPE = BlockStateProperties.CHEST_TYPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape AABB = Block.box(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);


    public SpectriteChest() {
        super(Properties.of(Material.WOOD)
                        .strength(2.5F)
                        .sound(SoundType.WOOD),
                ModBlockEntities.SPECTRITE_CHEST_TILE::get);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TYPE, ChestType.SINGLE)
                .setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? createTickerHelper(blockEntityType, ModBlockEntities.SPECTRITE_CHEST_TILE.get(), SpectriteChestTile::lidAnimateTick) : null;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpectriteChestTile(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return AABB;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return new TranslatableComponent("container.spectrite_chest");
            }

            @Override
            public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
                DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> result =
                        combine(state, level, pos, false);

                net.minecraft.world.Container container = result.apply(MENU_COMBINER).orElse(null);
                if (container instanceof net.minecraft.world.CompoundContainer) {
                    return new SpectriteChestContainer(windowId, playerInventory, container);
                } else if (container instanceof SpectriteChestTile) {
                    return new SpectriteChestContainer(windowId, playerInventory, (SpectriteChestTile)container);
                }
                return null;
            }
        };
    }

    private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<? extends net.minecraft.world.Container>> MENU_COMBINER =
            new DoubleBlockCombiner.Combiner<>() {
                @Override
                public Optional<net.minecraft.world.Container> acceptDouble(ChestBlockEntity first, ChestBlockEntity second) {
                    return Optional.of(new net.minecraft.world.CompoundContainer(first, second));
                }

                @Override
                public Optional<net.minecraft.world.Container> acceptSingle(ChestBlockEntity single) {
                    return Optional.of(single);
                }

                @Override
                public Optional<net.minecraft.world.Container> acceptNone() {
                    return Optional.empty();
                }
            };

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        MenuProvider menuProvider = this.getMenuProvider(state, level, pos);
        if (menuProvider != null) {
            NetworkHooks.openGui((ServerPlayer) player, menuProvider, pos);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (neighborState.getBlock() instanceof SpectriteChest &&
                direction.getAxis().isHorizontal()) {
            ChestType type = state.getValue(TYPE);
            if (type == ChestType.SINGLE && neighborState.getValue(TYPE) == ChestType.SINGLE &&
                    state.getValue(FACING) == neighborState.getValue(FACING) &&
                    getDirectionToAttached(state) == direction.getOpposite()) {
                return state.setValue(TYPE, getAttachedChestType(direction));
            }
        } else if (getDirectionToAttached(state) == direction) {
            return state.setValue(TYPE, ChestType.SINGLE);
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    private static Direction getDirectionToAttached(BlockState state) {
        Direction direction = state.getValue(FACING);
        return state.getValue(TYPE) == ChestType.LEFT ? direction.getClockWise() : direction.getCounterClockWise();
    }

    private static ChestType getAttachedChestType(Direction direction) {
        return direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? ChestType.RIGHT : ChestType.LEFT;
    }
}