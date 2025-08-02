package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.TileEngineerHopper;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.block.IWandable;
import vazkii.botania.common.handler.ModSounds;
import vazkii.botania.common.item.ItemTwigWand;

import javax.annotation.Nullable;

public class BlockEngineerHopper extends BaseEntityBlock{

    private static final VoxelShape SHAPE = Block.box(2.0D, 1.0D, 2.0D, 14.0D, 15.0D, 14.0D);

    public BlockEngineerHopper(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEngineerHopper(ModBlockEntities.ENGINEER_HOPPER_TILE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.ENGINEER_HOPPER_TILE.get(),
                (lvl, pos, st, tile) -> tile.serverTick());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileEngineerHopper hopper) {
                if (!level.isClientSide) {
                    Containers.dropContents(level, pos, hopper);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (heldItem.getItem() instanceof ItemTwigWand) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof TileEngineerHopper hopper && !player.isShiftKeyDown()) {
                hopper.changeBindType();
                if (!level.isClientSide) {
                    level.playSound(null, pos, ModSounds.ding,
                            SoundSource.BLOCKS, 0.11F, 1.0F);
                }
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TileEngineerHopper hopper) {
            ItemStack stack = hopper.getItem(0);
            if (stack.isEmpty()) {
                return 0;
            }
            return 1 + (stack.getCount() * 14) / Math.max(stack.getMaxStackSize(), 1);
        }
        return 0;
    }
}