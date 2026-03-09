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
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.InfinityGaiaSpreaderTile;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import vazkii.botania.api.mana.ILens;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.common.block.BlockModWaterloggable;
import vazkii.botania.common.handler.ModSounds;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.item.ItemTwigWand;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class InfinityGaiaSpreader extends BlockModWaterloggable implements EntityBlock {
    private static final VoxelShape SHAPE = box(2.0, 2.0, 2.0, 14.0, 14.0, 14.0);
    private static final VoxelShape SHAPE_PADDING = box(1.0, 1.0, 1.0, 15.0, 15.0, 15.0);
    private static final VoxelShape SHAPE_SCAFFOLDING = box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    public final VariantE variant;

    public InfinityGaiaSpreader(VariantE v, Properties builder) {
        super(builder.requiresCorrectToolForDrops());
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(BotaniaStateProps.HAS_SCAFFOLDING, false));
        this.variant = v;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{BotaniaStateProps.HAS_SCAFFOLDING});
    }

    @Nonnull
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if ((Boolean)blockState.getValue(BotaniaStateProps.HAS_SCAFFOLDING)) {
            return SHAPE_SCAFFOLDING;
        } else {
            BlockEntity be = blockGetter.getBlockEntity(blockPos);
            VoxelShape var10000;
            if (be instanceof InfinityGaiaSpreaderTile) {
                InfinityGaiaSpreaderTile spreader = (InfinityGaiaSpreaderTile)be;
                if (spreader.paddingColor != null) {
                    var10000 = SHAPE_PADDING;
                    return var10000;
                }
            }

            var10000 = SHAPE;
            return var10000;
        }
    }

    @Nonnull
    public VoxelShape getOcclusionShape(BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        return SHAPE;
    }

    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction orientation = placer == null ? Direction.WEST : Direction.orderedByNearest(placer)[0].getOpposite();
        InfinityGaiaSpreaderTile spreader = (InfinityGaiaSpreaderTile)world.getBlockEntity(pos);
        switch (orientation) {
            case DOWN:
                spreader.rotationY = -90.0F;
                break;
            case UP:
                spreader.rotationY = 90.0F;
                break;
            case NORTH:
                spreader.rotationX = 270.0F;
                break;
            case SOUTH:
                spreader.rotationX = 90.0F;
            case WEST:
            default:
                break;
            case EAST:
                spreader.rotationX = 180.0F;
        }

    }

    @Nonnull
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof InfinityGaiaSpreaderTile spreader) {
            ItemStack heldItem = player.getItemInHand(hand);
            if (heldItem.getItem() instanceof ItemTwigWand) {
                return InteractionResult.PASS;
            } else {
                boolean mainHandEmpty = player.getMainHandItem().isEmpty();
                ItemStack lens = spreader.getItemHandler().getItem(0);
                boolean playerHasLens = heldItem.getItem() instanceof ILens;
                boolean lensIsSame = playerHasLens && ItemStack.isSameItemSameTags(heldItem, lens);
                ItemStack wool = spreader.paddingColor != null ? new ItemStack((ItemLike) ColorHelper.WOOL_MAP.apply(spreader.paddingColor)) : ItemStack.EMPTY;
                boolean playerHasWool = ColorHelper.isWool(Block.byItem(heldItem.getItem()));
                boolean woolIsSame = playerHasWool && ItemStack.isSameItemSameTags(heldItem, wool);
                boolean playerHasScaffolding = !heldItem.isEmpty() && heldItem.is(Items.SCAFFOLDING);
                boolean shouldInsert = playerHasLens && !lensIsSame || playerHasWool && !woolIsSame || playerHasScaffolding && !(Boolean)state.getValue(BotaniaStateProps.HAS_SCAFFOLDING);
                ItemStack scaffolding;
                if (shouldInsert) {
                    if (playerHasLens) {
                        scaffolding = heldItem.copy();
                        scaffolding.setCount(1);
                        heldItem.shrink(1);
                        if (!lens.isEmpty()) {
                            player.getInventory().placeItemBackInInventory(lens);
                        }

                        spreader.getItemHandler().setItem(0, scaffolding);
                        world.playSound(player, pos, ModSounds.spreaderAddLens, SoundSource.BLOCKS, 1.0F, 1.0F);
                    } else if (playerHasWool) {
                        Block woolBlock = Block.byItem(heldItem.getItem());
                        heldItem.shrink(1);
                        if (spreader.paddingColor != null) {
                            ItemStack spreaderWool = new ItemStack((ItemLike)ColorHelper.WOOL_MAP.apply(spreader.paddingColor));
                            player.getInventory().placeItemBackInInventory(spreaderWool);
                        }

                        spreader.paddingColor = ColorHelper.getWoolColor(woolBlock);
                        spreader.setChanged();
                        world.playSound(player, pos, ModSounds.spreaderCover, SoundSource.BLOCKS, 1.0F, 1.0F);
                    } else {
                        world.setBlockAndUpdate(pos, (BlockState)state.setValue(BotaniaStateProps.HAS_SCAFFOLDING, true));
                        world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
                        if (!player.getAbilities().instabuild) {
                            heldItem.shrink(1);
                        }

                        world.playSound(player, pos, ModSounds.spreaderScaffold, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }

                    return InteractionResult.SUCCESS;
                } else if ((Boolean)state.getValue(BotaniaStateProps.HAS_SCAFFOLDING) && player.isSecondaryUseActive()) {
                    if (!player.getAbilities().instabuild) {
                        scaffolding = new ItemStack(Items.SCAFFOLDING);
                        player.getInventory().placeItemBackInInventory(scaffolding);
                    }

                    world.setBlockAndUpdate(pos, (BlockState)state.setValue(BotaniaStateProps.HAS_SCAFFOLDING, false));
                    world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
                    world.playSound(player, pos, ModSounds.spreaderUnScaffold, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResult.SUCCESS;
                } else if (!lens.isEmpty() && (mainHandEmpty || lensIsSame)) {
                    player.getInventory().placeItemBackInInventory(lens);
                    spreader.getItemHandler().setItem(0, ItemStack.EMPTY);
                    world.playSound(player, pos, ModSounds.spreaderRemoveLens, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResult.SUCCESS;
                } else if (spreader.paddingColor == null || !mainHandEmpty && !woolIsSame) {
                    return InteractionResult.PASS;
                } else {
                    player.getInventory().placeItemBackInInventory(wool);
                    spreader.paddingColor = null;
                    spreader.setChanged();
                    world.playSound(player, pos, ModSounds.spreaderUncover, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResult.SUCCESS;
                }
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    public void onRemove(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (!(tile instanceof InfinityGaiaSpreaderTile)) {
                return;
            }

            InfinityGaiaSpreaderTile spreader = (InfinityGaiaSpreaderTile)tile;
            ItemStack scaffolding;
            if (spreader.paddingColor != null) {
                scaffolding = new ItemStack((ItemLike)ColorHelper.WOOL_MAP.apply(spreader.paddingColor));
                Containers.dropItemStack(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), scaffolding);
            }

            if ((Boolean)state.getValue(BotaniaStateProps.HAS_SCAFFOLDING)) {
                scaffolding = new ItemStack(Items.SCAFFOLDING);
                Containers.dropItemStack(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), scaffolding);
            }

            Containers.dropContents(world, pos, spreader.getItemHandler());
            super.onRemove(state, world, pos, newState, isMoving);
        }

    }

    @Nonnull
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new InfinityGaiaSpreaderTile(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.INFINITY_SPREADER.get(), InfinityGaiaSpreaderTile::commonTick);
    }

    public static enum VariantE {
        MANA(160, 1000, 2162464, 65280, 60, 4.0F, 1.0F),
        REDSTONE(160, 1000, 16719904, 16711680, 60, 4.0F, 1.0F),
        ELVEN(240, 1000, 16729540, 16711854, 80, 4.0F, 1.25F),
        INFINITY(214748364, 2147483647, 2162464, 65280, 120, 20.0F, 2.0F),
        GAIA(640, 6400, 2162464, 65280, 120, 20.0F, 2.0F);

        public final int burstMana;
        public final int manaCapacity;
        public final int color;
        public final int hudColor;
        public final int preLossTicks;
        public final float lossPerTick;
        public final float motionModifier;

        VariantE(int bm, int mc, int c, int hc, int plt, float lpt, float mm) {
            this.burstMana = bm;
            this.manaCapacity = mc;
            this.color = c;
            this.hudColor = hc;
            this.preLossTicks = plt;
            this.lossPerTick = lpt;
            this.motionModifier = mm;
        }
    }
}