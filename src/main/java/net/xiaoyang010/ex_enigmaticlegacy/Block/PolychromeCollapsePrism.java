package net.xiaoyang010.ex_enigmaticlegacy.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.PolychromeCollapsePrismTile;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.block.BlockModWaterloggable;

import javax.annotation.Nonnull;
import java.util.Random;

public class PolychromeCollapsePrism extends BlockModWaterloggable implements EntityBlock {
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 3, 16);

    public PolychromeCollapsePrism(Properties builder) {
        super(builder);
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRandom) {
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (blockEntity instanceof PolychromeCollapsePrismTile prismTile) {
            if (prismTile.isRecipeAltarBlock(prismTile.getAltarBlocks(pPos))){
                for (int i = 0; i < 10; i++) {
                    pLevel.addAlwaysVisibleParticle(ParticleTypes.FIREWORK, pPos.getX() + 0.5d, pPos.getY() + 0.5d, pPos.getZ() + 0.5d, 0.01, 0.05, 0.01);
                }
            }
        }
        super.animateTick(pState, pLevel, pPos, pRandom);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack itemInHand = player.getItemInHand(hand);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PolychromeCollapsePrismTile prismTile && !level.isClientSide()) {
            if (player.isCrouching() && itemInHand.isEmpty()){
                ItemStack out = prismTile.getItem(4);
                if (!out.isEmpty()){ //优先取出输出
                    prismTile.setItem(4, ItemStack.EMPTY);
//                    NetworkHandler.CHANNEL.sendToServer(new PrismRenderPacket(pos, ItemStack.EMPTY));
                    player.addItem(out);
                    return InteractionResult.SUCCESS;
                }

                for (int i = 3 ; i >= 0 ; i--) {
                    ItemStack stack = prismTile.getItem(i);
                    if (!stack.isEmpty() && hand == InteractionHand.MAIN_HAND){
                        prismTile.setItem(i, ItemStack.EMPTY);
                        player.addItem(stack);
                        return InteractionResult.SUCCESS;
                    }
                }
            }else { //非潜行放入
                if (!itemInHand.isEmpty()) {
                    if (prismTile.getItem(4).isEmpty()){
                        ItemStack stack = new ItemStack(itemInHand.getItem(), 1);
                        if (prismTile.addItem(stack)){
                            itemInHand.shrink(1);
                            return InteractionResult.SUCCESS;
                        }
                    }else player.displayClientMessage(new TranslatableComponent("已有输出！！！"), true);
                }
            }
        }
        return super.use(state, level, pos, player, hand, hitResult);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity instanceof PolychromeCollapsePrismTile) {
                Containers.dropContents(pLevel, pPos, (Container)blockentity);
                pLevel.updateNeighbourForOutputSignal(pPos, this);
            }

            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }

    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new PolychromeCollapsePrismTile(ModBlockEntities.POLYCHROME_COLLAPSE_PRISM_TILE.get(), pos, state);
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public boolean isPathfindable(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, PathComputationType type) {
        return false;
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide) {
            return createTickerHelper(type, ModBlockEntities.POLYCHROME_COLLAPSE_PRISM_TILE.get(), PolychromeCollapsePrismTile::serverTick);
        }
        return null;
    }

    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> typeToCheck, BlockEntityType<E> typeExpected, BlockEntityTicker<? super E> ticker) {
        return typeToCheck == typeExpected ? (BlockEntityTicker<A>) ticker : null;
    }
}