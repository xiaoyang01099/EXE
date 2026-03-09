package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.ManaContainerTile;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import vazkii.botania.common.block.BlockModWaterloggable;
import vazkii.botania.common.entity.EntityManaBurst;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ManaContainerBlock extends BlockModWaterloggable implements EntityBlock {

    private static final VoxelShape SHAPE = Block.box(
            1.28, 0.64, 1.28,
            14.72, 15.36, 14.72
    );

    public final Variant variant;

    public enum Variant {
        DEFAULT,
        CREATIVE,
        DILUTED
    }

    public ManaContainerBlock(Variant v, Properties builder) {
        super(Properties.of(Material.STONE)
                .strength(9.0F)
                .requiresCorrectToolForDrops());
        this.variant = v;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        if (variant == Variant.CREATIVE) {
            for (int i = 0; i < 2; i++) {
                tooltip.add(new TranslatableComponent("botaniamisc.creativePool" + i).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext econtext
                && econtext.getEntity() instanceof EntityManaBurst) {
            return SHAPE;
        } else {
            return super.getCollisionShape(state, world, pos, context);
        }
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (entity instanceof ItemEntity item) {
            ManaContainerTile tile = (ManaContainerTile) world.getBlockEntity(pos);
            if (tile != null) {
                tile.collideEntityItem(item);
            }
        }
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        ManaContainerTile pool = (ManaContainerTile) world.getBlockEntity(pos);
        if (pool != null) {
            return ManaContainerTile.calculateComparatorLevel(pool.getCurrentMana(), pool.manaCap);
        }
        return 0;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return false;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaContainerTile(ModBlockEntities.MANA_CONTAINER_TILE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.MANA_CONTAINER_TILE.get(),
                level.isClientSide ? ManaContainerTile::clientTick : ManaContainerTile::serverTick);
    }

    @Nonnull
    @Override
    public InteractionResult use(@Nonnull BlockState state, Level world, @Nonnull BlockPos pos, Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        BlockEntity te = world.getBlockEntity(pos);
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof DyeItem dye && te instanceof ManaContainerTile pool) {
            DyeColor color = dye.getDyeColor();
            if (color != pool.getColor()) {
                pool.setColor(color);
                stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Nonnull
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}