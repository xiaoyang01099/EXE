package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.FullAltarTile;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import vazkii.botania.api.block.IPetalApothecary;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.common.block.BlockMod;
import vazkii.botania.common.block.tile.TileSimpleInventory;
import vazkii.botania.common.helper.InventoryHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FullAltarBlock extends BlockMod implements EntityBlock, LiquidBlockContainer {

    public static final EnumProperty<IPetalApothecary.State> FLUID = EnumProperty.create("fluid", IPetalApothecary.State.class);
    private static final VoxelShape BASE = Block.box(2, 0, 2, 14, 2, 14);
    private static final VoxelShape PILLAR = Block.box(4, 2, 4, 12, 11, 12);
    private static final VoxelShape TOP = Block.box(2, 11, 2, 14, 16, 14);
    private static final VoxelShape TOP_CUTOUT = Block.box(3, 12, 3, 13, 16, 13);
    private static final VoxelShape SHAPE = Shapes.or(Shapes.or(BASE, PILLAR), Shapes.join(TOP, TOP_CUTOUT, BooleanOp.ONLY_FIRST));

    public FullAltarBlock(Properties builder) {
        super(builder.requiresCorrectToolForDrops());
        registerDefaultState(defaultBlockState().setValue(FLUID, IPetalApothecary.State.WATER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FLUID);
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (!world.isClientSide && entity instanceof ItemEntity itemEntity) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof FullAltarTile tile) {
                if (tile.collideEntityItem(itemEntity)) {
                    VanillaPacketDispatcher.dispatchTEToNearbyPlayers(tile);
                }
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof FullAltarTile apothecary)) {
            return InteractionResult.PASS;
        }

        boolean mainHandEmpty = player.getMainHandItem().isEmpty();

        if (apothecary.canAddLastRecipe() && mainHandEmpty) {
            if (!world.isClientSide) {
                apothecary.trySetLastRecipe(player);
            }
            return InteractionResult.SUCCESS;
        }
        else if (!apothecary.isEmpty() && mainHandEmpty) {
            if (!world.isClientSide) {
                InventoryHelper.withdrawFromInventory(apothecary, player);
                VanillaPacketDispatcher.dispatchTEToNearbyPlayers(apothecary);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void handlePrecipitation(BlockState state, Level world, BlockPos pos, Biome.Precipitation precipitation) {
    }

    @Nonnull
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new FullAltarTile(ModBlockEntities.FULL_ALTAR_TILE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return createTickerHelper(type, ModBlockEntities.FULL_ALTAR_TILE.get(), FullAltarTile::clientTick);
        } else {
            return createTickerHelper(type, ModBlockEntities.FULL_ALTAR_TILE.get(), FullAltarTile::serverTick);
        }
    }

    @Override
    public void onRemove(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        boolean blockChanged = !state.is(newState.getBlock());
        if (blockChanged) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof TileSimpleInventory inventory) {
                Containers.dropContents(world, pos, inventory.getItemHandler());
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public boolean canPlaceLiquid(@Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Fluid fluid) {
        return false;
    }

    @Override
    public boolean placeLiquid(@Nonnull LevelAccessor level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull FluidState fluidState) {
        return false;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return 15;
    }
}