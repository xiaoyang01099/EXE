package net.xiaoyang010.ex_enigmaticlegacy.Block;

import io.redspace.ironsspellbooks.util.Component;
import morph.avaritia.block.MachineBlock;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ContainerInfinityCompressor;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileEntityInfinityCompressor;

public class BlockInfinityCompressor extends MachineBlock {
    public BlockInfinityCompressor(Properties properties) {
        super(properties, ModBlockEntities.INFINITY_COMPRESSOR_TILE::get);
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, Direction.NORTH)).setValue(ACTIVE, false));
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityInfinityCompressor tile) {
            // 使用NetworkHooks打开GUI
            NetworkHooks.openGui((ServerPlayer) player, new SimpleMenuProvider(
                    (windowId, playerInventory, playerEntity) ->
                            new ContainerInfinityCompressor(windowId, playerInventory, tile),
                    Component.literal("Infinity Compressor")
            ), buf -> buf.writeBlockPos(pos));

            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity var7 = level.getBlockEntity(pos);
            if (var7 instanceof TileEntityInfinityCompressor tile) {
                tile.dropContents();
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }
}