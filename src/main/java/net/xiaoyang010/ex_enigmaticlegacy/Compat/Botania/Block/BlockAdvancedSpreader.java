package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.TileAdvancedSpreader;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.mana.ILens;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.common.block.BlockModWaterloggable;
import vazkii.botania.common.handler.ModSounds;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.item.ItemTwigWand;

import javax.annotation.Nonnull;

public class BlockAdvancedSpreader extends BlockModWaterloggable implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final VoxelShape SHAPE = Shapes.box(0.0625, 0.0625, 0.0625, 0.9375, 0.9375, 0.9375);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public final VariantN variant;

    public enum VariantN {
        NATURE(32000, 128000, 0x9ACD32, 0xADFF2F, 200, 30f, 2.7f);

        public final int burstMana;
        public final int manaCapacity;
        public final int color;
        public final int hudColor;
        public final int preLossTicks;
        public final float lossPerTick;
        public final float motionModifier;

        VariantN(int bm, int mc, int c, int hc, int plt, float lpt, float mm) {
            this.burstMana = bm;
            this.manaCapacity = mc;
            this.color = c;
            this.hudColor = hc;
            this.preLossTicks = plt;
            this.lossPerTick = lpt;
            this.motionModifier = mm;
        }
    }

    public BlockAdvancedSpreader(VariantN v, Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false)
                .setValue(BotaniaStateProps.HAS_SCAFFOLDING, false));
        this.variant = v;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, BotaniaStateProps.HAS_SCAFFOLDING);
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
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @javax.annotation.Nullable LivingEntity placer, ItemStack stack) {
        Direction orientation = placer == null ? Direction.WEST : Direction.orderedByNearest(placer)[0].getOpposite();
        BlockEntity tile = world.getBlockEntity(pos);

            if (tile instanceof TileAdvancedSpreader spreader) {
            switch (orientation) {
                case DOWN:
                    spreader.rotationY = -90F;
                    break;
                case UP:
                    spreader.rotationY = 90F;
                    break;
                case NORTH:
                    spreader.rotationX = 270F;
                    break;
                case SOUTH:
                    spreader.rotationX = 90F;
                    break;
                case WEST:
                    break;
                case EAST:
                    spreader.rotationX = 180F;
                    break;
            }
            spreader.setChanged();
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (!(tile instanceof TileAdvancedSpreader spreader)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() instanceof ItemTwigWand) {
            return InteractionResult.PASS;
        }

        boolean mainHandEmpty = player.getMainHandItem().isEmpty();
        IItemHandlerModifiable itemHandler = spreader.getItemHandlerModifiable();
        ItemStack lens = itemHandler.getStackInSlot(0);

        boolean playerHasLens = heldItem.getItem() instanceof ILens;
        boolean lensIsSame = playerHasLens && ItemStack.isSameItemSameTags(heldItem, lens);

        ItemStack wool = spreader.paddingColor != null
                ? new ItemStack(ColorHelper.WOOL_MAP.apply(spreader.paddingColor))
                : ItemStack.EMPTY;
        boolean playerHasWool = ColorHelper.isWool(Block.byItem(heldItem.getItem()));
        boolean woolIsSame = playerHasWool && ItemStack.isSameItemSameTags(heldItem, wool);

        boolean playerHasScaffolding = !heldItem.isEmpty() && heldItem.is(Items.SCAFFOLDING);
        boolean shouldInsert = (playerHasLens && !lensIsSame)
                || (playerHasWool && !woolIsSame)
                || (playerHasScaffolding && !state.getValue(BotaniaStateProps.HAS_SCAFFOLDING));

        if (shouldInsert) {
            if (playerHasLens) {
                ItemStack toInsert = heldItem.copy();
                toInsert.setCount(1);

                heldItem.shrink(1);
                if (!lens.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(lens);
                }

                itemHandler.setStackInSlot(0, toInsert);
                world.playSound(player, pos, ModSounds.spreaderAddLens, SoundSource.BLOCKS, 1F, 1F);
                spreader.setChanged();
                return InteractionResult.sidedSuccess(world.isClientSide);

            } else if (playerHasWool) {
                Block woolBlock = Block.byItem(heldItem.getItem());

                heldItem.shrink(1);
                if (spreader.paddingColor != null) {
                    ItemStack spreaderWool = new ItemStack(ColorHelper.WOOL_MAP.apply(spreader.paddingColor));
                    player.getInventory().placeItemBackInInventory(spreaderWool);
                }

                spreader.paddingColor = ColorHelper.getWoolColor(woolBlock);
                spreader.setChanged();
                world.playSound(player, pos, ModSounds.spreaderCover, SoundSource.BLOCKS, 1F, 1F);
                return InteractionResult.sidedSuccess(world.isClientSide);

            } else {
                world.setBlockAndUpdate(pos, state.setValue(BotaniaStateProps.HAS_SCAFFOLDING, true));
                world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));

                if (!player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }

                world.playSound(player, pos, ModSounds.spreaderScaffold, SoundSource.BLOCKS, 1F, 1F);
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }

        if (state.getValue(BotaniaStateProps.HAS_SCAFFOLDING) && player.isSecondaryUseActive()) {
            if (!player.getAbilities().instabuild) {
                ItemStack scaffolding = new ItemStack(Items.SCAFFOLDING);
                player.getInventory().placeItemBackInInventory(scaffolding);
            }
            world.setBlockAndUpdate(pos, state.setValue(BotaniaStateProps.HAS_SCAFFOLDING, false));
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));

            world.playSound(player, pos, ModSounds.spreaderUnScaffold, SoundSource.BLOCKS, 1F, 1F);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        if (!lens.isEmpty() && (mainHandEmpty || lensIsSame)) {
            player.getInventory().placeItemBackInInventory(lens);
            itemHandler.setStackInSlot(0, ItemStack.EMPTY);

            world.playSound(player, pos, ModSounds.spreaderRemoveLens, SoundSource.BLOCKS, 1F, 1F);
            spreader.setChanged();
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        if (spreader.paddingColor != null && (mainHandEmpty || woolIsSame)) {
            player.getInventory().placeItemBackInInventory(wool);
            spreader.paddingColor = null;
            spreader.setChanged();

            world.playSound(player, pos, ModSounds.spreaderUncover, SoundSource.BLOCKS, 1F, 1F);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileAdvancedSpreader  spreader) {

                if (spreader.paddingColor != null) {
                    ItemStack padding = new ItemStack(ColorHelper.WOOL_MAP.apply(spreader.paddingColor));
                    Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), padding);
                }

                if (state.getValue(BotaniaStateProps.HAS_SCAFFOLDING)) {
                    ItemStack scaffolding = new ItemStack(Items.SCAFFOLDING);
                    Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), scaffolding);
                }

                IItemHandlerModifiable itemHandler = spreader.getItemHandlerModifiable();
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    ItemStack stack = itemHandler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                    }
                }
            }

            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 12;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileAdvancedSpreader(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> {
            if (be instanceof TileAdvancedSpreader spreader) {
                TileAdvancedSpreader.commonTick(lvl, pos, st, spreader);
            }
        };
    }
}