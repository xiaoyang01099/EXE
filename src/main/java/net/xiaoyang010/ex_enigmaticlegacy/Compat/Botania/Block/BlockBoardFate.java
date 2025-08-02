package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.TileBoardFate;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.TileGameBoard;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModSounds;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;

import javax.annotation.Nullable;
import java.util.List;

public class BlockBoardFate extends BaseEntityBlock {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 1);
    private static final VoxelShape SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.1875, 1.0);

    public BlockBoardFate(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, 0));
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        ItemStack hand = pContext.getItemInHand();
        if (hand.getItem() == ModItems.BOARD_FATE.get())
            return this.defaultBlockState().setValue(VARIANT, 1);
        return this.defaultBlockState().setValue(VARIANT, 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        int variant = state.getValue(VARIANT);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (variant == 0 && blockEntity instanceof TileGameBoard tile) {
            return handleGameBoardInteraction(tile, player, pos, level);
        } else if (variant == 1 && blockEntity instanceof TileBoardFate tile) {
            return handleBoardFateInteraction(tile, player, hand, pos, level);
        }

        return InteractionResult.PASS;
    }

    private InteractionResult handleGameBoardInteraction(TileGameBoard tile, Player player, BlockPos pos, Level level) {
        if (player.isShiftKeyDown() && !tile.hasGame()) {
            tile.isSingleGame = !tile.isSingleGame;
            level.playSound(null, pos, SoundEvents.NOTE_BLOCK_PLING, SoundSource.BLOCKS, 0.11F, 0.8F);
            return InteractionResult.SUCCESS;
        } else {
            if (!tile.hasGame()) {
                tile.setPlayer(player);
            } else {
                if (tile.isSingleGame || tile.playersName[1].isEmpty() ||
                        tile.playersName[0].equals(player.getName().getString())) {
                    return tile.dropDice(player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
                }
                tile.setPlayer(player);
            }
            return InteractionResult.SUCCESS;
        }
    }

    private InteractionResult handleBoardFateInteraction(TileBoardFate tile, Player player, InteractionHand hand, BlockPos pos, Level level) {
        if (player.isShiftKeyDown()) {
            return tile.spawnRelic(player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        } else {
            ItemStack heldItem = player.getItemInHand(hand);
            if (!heldItem.isEmpty() && TileBoardFate.isDice(heldItem)) {
                for (int i = 0; i < tile.getContainerSize(); ++i) {
                    ItemStack slotStack = tile.getItem(i);
                    if (slotStack.isEmpty()) {
                        ItemStack copy = heldItem.copy();
                        copy.setCount(1);
                        tile.setItem(i, copy);
                        tile.slotChance[i] = (byte)(level.random.nextInt(6) + 1);
                        tile.requestUpdate = true;

                        heldItem.shrink(1);

                        level.playSound(null, pos, ModSounds.BOARD_CUBE, SoundSource.BLOCKS, 0.6F, 1.0F);
                        return InteractionResult.SUCCESS;
                    }
                }
                return InteractionResult.PASS;
            }
            return InteractionResult.PASS;
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                BlockEntity blockEntity = level.getBlockEntity(pos);

                if (state.getValue(VARIANT) == 1 && blockEntity instanceof TileBoardFate tile) {
                    for (int i = 0; i < tile.getContainerSize(); ++i) {
                        ItemStack stack = tile.getItem(i);
                        if (!stack.isEmpty()) {
                            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                        }
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        int variant = state.getValue(VARIANT);
        if (variant == 0) {
            return new TileGameBoard(getGameBoardType(), pos, state);
        } else {
            return new TileBoardFate(getBoardFateType(), pos, state);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        int variant = state.getValue(VARIANT);

        if (variant == 0) {
            if (level.isClientSide) {
                return createTickerHelper(type, getGameBoardType(), TileGameBoard::clientTick);
            } else {
                return createTickerHelper(type, getGameBoardType(), TileGameBoard::serverTick);
            }
        } else {
            if (level.isClientSide) {
                return createTickerHelper(type, getBoardFateType(), TileBoardFate::clientTick);
            } else {
                return createTickerHelper(type, getBoardFateType(), TileBoardFate::serverTick);
            }
        }
    }

    private BlockEntityType<TileGameBoard> getGameBoardType() {
        return ModBlockEntities.GAME_BOARD_TILE.get();
    }

    private BlockEntityType<TileBoardFate> getBoardFateType() {
        return ModBlockEntities.BOARD_FATE_TILE.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        int variant = getVariantFromItemStack(stack);

        if (variant == 0) {

        } else {
            tooltip.add(EComponent.translatable("tooltip.ex_enigmaticlegacy.board_fate.usage"));
        }
    }

    private int getVariantFromItemStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("BlockStateTag")) {
            CompoundTag stateTag = tag.getCompound("BlockStateTag");
            if (stateTag.contains("variant")) {
                try {
                    return Integer.parseInt(stateTag.getString("variant"));
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    public static ItemStack createItemStack(int variant) {
        ItemStack stack = new ItemStack(getBlockInstance());
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag stateTag = new CompoundTag();
        stateTag.putString("variant", String.valueOf(variant));
        tag.put("BlockStateTag", stateTag);
        return stack;
    }

    private static Block getBlockInstance() {
        return ModBlockss.BOARD_FATE.get();
    }

    public BlockState getStateForVariant(int variant) {
        return this.defaultBlockState().setValue(VARIANT, Math.max(0, Math.min(1, variant)));
    }

    public void triggerPickupAdvancement(Player player, ItemStack stack) {
        int variant = getVariantFromItemStack(stack);

        if (variant == 1) {
            // 触发命运板相关的进度
            // 例如：
            // AdvancementTrigger.trigger(player, "picked_up_board_fate");
        }
    }

    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootContext.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);

        int variant = state.getValue(VARIANT);
        if (!drops.isEmpty()) {
            ItemStack drop = drops.get(0);
            if (drop.getItem() == this.asItem()) {
                drops.set(0, createItemStack(variant));
            }
        }

        return drops;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE;
    }

    // 是否阻挡光线
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    // 光照等级
    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }
}