package net.xiaoyang010.ex_enigmaticlegacy.api.test.res;

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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import vazkii.botania.api.mana.ILens;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.common.block.BlockModWaterloggable;
import vazkii.botania.common.handler.ModSounds;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.item.ItemTwigWand;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockCursedManaSpreader extends BlockModWaterloggable implements EntityBlock {

    private static final VoxelShape SHAPE = box(2, 2, 2, 14, 14, 14);
    private static final VoxelShape SHAPE_PADDING = box(1, 1, 1, 15, 15, 15);
    private static final VoxelShape SHAPE_SCAFFOLDING = box(0, 0, 0, 16, 16, 16);

    public enum Variant {
        CURSED(1600, 10000, 0x8B00FF, 0x6A0DAD, 60, 400f, 1f),          // 诅咒发射器
        CORRUPTED(2400, 100000, 0x4B0082, 0x2E0854, 80, 400f, 1.25f),   // 腐化发射器
        VOID(6400, 64000000, 0x1A0033, 0x0D001A, 120, 600f, 2f);         // 虚空发射器

        public final int burstMana;
        public final int manaCapacity;
        public final int color;
        public final int hudColor;
        public final int preLossTicks;
        public final float lossPerTick;
        public final float motionModifier;

        Variant(int bm, int mc, int c, int hc, int plt, float lpt, float mm) {
            burstMana = bm;
            manaCapacity = mc;
            color = c;
            hudColor = hc;
            preLossTicks = plt;
            lossPerTick = lpt;
            motionModifier = mm;
        }
    }

    public final Variant variant;

    public BlockCursedManaSpreader(Variant v, Properties builder) {
        super(builder);
        registerDefaultState(defaultBlockState().setValue(BotaniaStateProps.HAS_SCAFFOLDING, false));
        this.variant = v;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BotaniaStateProps.HAS_SCAFFOLDING);
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (blockState.getValue(BotaniaStateProps.HAS_SCAFFOLDING)) {
            return SHAPE_SCAFFOLDING;
        }
        BlockEntity be = blockGetter.getBlockEntity(blockPos);
        return be instanceof TileCursedManaSpreader spreader && spreader.paddingColor != null ? SHAPE_PADDING : SHAPE;
    }

    @Nonnull
    @Override
    public VoxelShape getOcclusionShape(BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        return SHAPE;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction orientation = placer == null ? Direction.WEST : Direction.orderedByNearest(placer)[0].getOpposite();
        BlockEntity tile = world.getBlockEntity(pos);

        if (tile instanceof TileCursedManaSpreader spreader) {
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

    @Nonnull
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (!(tile instanceof TileCursedManaSpreader spreader)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() instanceof ItemTwigWand) {
            return InteractionResult.PASS;
        }

        boolean mainHandEmpty = player.getMainHandItem().isEmpty();
        IItemHandlerModifiable itemHandler = spreader.getItemHandler();
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
            if (tile instanceof TileCursedManaSpreader spreader) {

                if (spreader.paddingColor != null) {
                    ItemStack padding = new ItemStack(ColorHelper.WOOL_MAP.apply(spreader.paddingColor));
                    Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), padding);
                }

                if (state.getValue(BotaniaStateProps.HAS_SCAFFOLDING)) {
                    ItemStack scaffolding = new ItemStack(Items.SCAFFOLDING);
                    Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), scaffolding);
                }

                IItemHandlerModifiable itemHandler = spreader.getItemHandler();
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

    @Nonnull
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new TileCursedManaSpreader(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.CURSED_SPREADER.get(), TileCursedManaSpreader::commonTick);
    }
}
