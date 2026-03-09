package net.xiaoyang010.ex_enigmaticlegacy.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Container.NeutroniumDecompressorMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.NeutroniumDecompressorTile;
import org.jetbrains.annotations.Nullable;

public class NeutroniumDecompressorBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public NeutroniumDecompressorBlock() {
        super(Properties.of(Material.METAL)
                .strength(50.0F, 1200.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion());

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof NeutroniumDecompressorTile tile) {
            MenuProvider provider = new SimpleMenuProvider(
                    (id, playerInv, $) -> new NeutroniumDecompressorMenu(id, playerInv, tile),
                    TextComponent.EMPTY
            );

            NetworkHooks.openGui((ServerPlayer) player, provider, buf -> buf.writeBlockPos(pos));
        }

        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NeutroniumDecompressorTile(ModBlockEntities.NEUTRONIUM_DECOMPRESSOR_TILE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }

        if (type == ModBlockEntities.NEUTRONIUM_DECOMPRESSOR_TILE.get()) {
            return (lvl, pos, st, be) -> {
                NeutroniumDecompressorTile tile = (NeutroniumDecompressorTile) be;
                tile.tick();

                boolean isWorking = tile.isWorking();
                if (st.getValue(ACTIVE) != isWorking) {
                    lvl.setBlock(pos, st.setValue(ACTIVE, isWorking), 3);
                }
            };
        }

        return null;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof NeutroniumDecompressorTile tile) {
                Containers.dropContents(level, pos, new SimpleContainer(
                        tile.getInventory().getStackInSlot(0),
                        tile.getInventory().getStackInSlot(1)
                ));
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof NeutroniumDecompressorTile tile) {
            int progress = tile.getProgress();
            int maxProgress = tile.getMaxProgress();
            if (maxProgress > 0) {
                return (int) (15.0F * progress / maxProgress);
            }
        }
        return 0;
    }
}