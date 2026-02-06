package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.TileLebethronCore;
import vazkii.botania.api.internal.VanillaPacketDispatcher;

import javax.annotation.Nullable;

public class BlockLebethronCore extends BaseEntityBlock {

    public BlockLebethronCore(Properties properties) {
        super(properties
                .sound(SoundType.WOOD)
                .strength(6.0F));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileLebethronCore(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.LEBETHRON_CORE.get(),
                level.isClientSide ? TileLebethronCore::clientTick : TileLebethronCore::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TileLebethronCore core)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!(heldItem.getItem() instanceof BlockItem blockItem)) {
            return InteractionResult.PASS;
        }

        Block block = blockItem.getBlock();
        if (!block.defaultBlockState().is(net.minecraft.tags.BlockTags.LEAVES)) {
            return InteractionResult.PASS;
        }

        if (heldItem.hasTag() && heldItem.getTag().contains("BlockEntityTag")) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            core.updateStructure();
            if (core.getValidTree() && core.setBlock(player, block)) {
                heldItem.shrink(1);
                VanillaPacketDispatcher.dispatchTEToNearbyPlayers(core);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileLebethronCore core && !level.isClientSide) {
                Block storedBlock = core.getStoredBlock();
                if (storedBlock != null) {
                    ItemStack stack = new ItemStack(storedBlock);

                    double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;
                    double y = pos.getY() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;
                    double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;

                    ItemEntity itemEntity = new ItemEntity(level, x, y, z, stack);
                    itemEntity.setDefaultPickUpDelay();

                    double motion = 0.05;
                    itemEntity.setDeltaMovement(
                            level.random.nextGaussian() * motion,
                            level.random.nextGaussian() * motion + 0.2,
                            level.random.nextGaussian() * motion
                    );

                    level.addFreshEntity(itemEntity);
                }
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }
}
