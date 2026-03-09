package net.xiaoyang010.ex_enigmaticlegacy.api.test.res;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import vazkii.botania.common.block.BlockModWaterloggable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 诅咒魔力池方块
 */
public class BlockCursedManaPool extends BlockModWaterloggable implements EntityBlock {
    private static final VoxelShape REAL_SHAPE;
    private static final VoxelShape BURST_SHAPE;

    static {
        VoxelShape slab = box(0, 0, 0, 16, 8, 16);
        VoxelShape cutout = box(1, 1, 1, 15, 8, 15);
        VoxelShape cutoutBurst = box(1, 6, 1, 15, 8, 15);
        BURST_SHAPE = Shapes.join(slab, cutoutBurst, BooleanOp.ONLY_FIRST);
        REAL_SHAPE = Shapes.join(slab, cutout, BooleanOp.ONLY_FIRST);
    }

    public enum Variant {
        DEFAULT,      // 普通诅咒魔力池
        CREATIVE,     // 创造模式诅咒魔力池
        DILUTED,      // 稀释诅咒魔力池
        CORRUPTED     // 腐化诅咒魔力池
    }

    public final Variant variant;

    public BlockCursedManaPool(Variant v, Properties builder) {
        super(builder);
        this.variant = v;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        if (variant == Variant.CREATIVE) {
            tooltip.add(new TranslatableComponent("block.ex_enigmaticlegacy.cursed_mana_pool.creative.tooltip.0")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(new TranslatableComponent("block.ex_enigmaticlegacy.cursed_mana_pool.creative.tooltip.1")
                    .withStyle(ChatFormatting.GRAY));
        } else if (variant == Variant.CORRUPTED) {
            tooltip.add(new TranslatableComponent("block.ex_enigmaticlegacy.cursed_mana_pool.corrupted.tooltip")
                    .withStyle(ChatFormatting.DARK_RED));
        }
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return REAL_SHAPE;
    }

    @Nonnull
    @Override
    public InteractionResult use(@Nonnull BlockState state, Level world, @Nonnull BlockPos pos,
                                 Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        BlockEntity te = world.getBlockEntity(pos);
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof DyeItem dye && te instanceof TileCursedManaPool pool) {
            DyeColor color = dye.getDyeColor();
            if (color != pool.getCursedColor()) {
                pool.setCursedColor(color);
                stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
        }

        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext econtext
                && econtext.getEntity() instanceof EntityCursedManaBurst) {
            return BURST_SHAPE;
        } else {
            return super.getCollisionShape(state, world, pos, context);
        }
    }

    @Nonnull
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new TileCursedManaPool(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.CURSED_MANA_POOL.get(),
                level.isClientSide ? TileCursedManaPool::clientTick : TileCursedManaPool::serverTick);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (entity instanceof ItemEntity item) {
            TileCursedManaPool tile = (TileCursedManaPool) world.getBlockEntity(pos);
            if (tile != null) {
                tile.collideEntityItem(item);
            }
        }
    }

    @Nonnull
    @Override
    public RenderShape getRenderShape(BlockState state) {
        if (variant == Variant.CORRUPTED) {
            return RenderShape.ENTITYBLOCK_ANIMATED;
        } else {
            return RenderShape.MODEL;
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        TileCursedManaPool pool = (TileCursedManaPool) world.getBlockEntity(pos);
        if (pool == null) return 0;
        return TileCursedManaPool.calculateComparatorLevel(pool.getCurrentCursedMana(), pool.getMaxCursedMana());
    }
}
