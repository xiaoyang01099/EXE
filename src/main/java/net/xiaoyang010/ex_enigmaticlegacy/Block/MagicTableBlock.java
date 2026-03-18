package net.xiaoyang010.ex_enigmaticlegacy.Block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Container.MagicTableMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileMagicTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

public class MagicTableBlock extends BaseEntityBlock {
    private static final long EMC_CONVERT_THRESHOLD = 500;

    public MagicTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileMagicTable tile) {
                tile.setCraftingPlayer(player.getUUID());
                NetworkHooks.openGui((ServerPlayer) player, tile, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return ModBlockEntities.MAGIC_TABLE_TILE.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, ModBlockEntities.MAGIC_TABLE_TILE.get(),
                TileMagicTable::tick);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileMagicTable tile) {
            CompoundTag blockTag = stack.getTagElement("BlockEntityTag");
            if (blockTag != null) {
                tile.load(blockTag);
                tile.setChanged();
            }

            if (placer instanceof Player player) {
                tile.setCraftingPlayer(player.getUUID());
            }
        }
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileMagicTable tile && level instanceof ServerLevel serverLevel) {
                handleBlockBreak(tile, serverLevel, pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void handleBlockBreak(TileMagicTable tile, ServerLevel level, BlockPos pos) {
        ServerPlayer owner = null;
        if (tile.getCraftingPlayerUUID() != null && level.getServer() != null) {
            owner = level.getServer().getPlayerList().getPlayer(tile.getCraftingPlayerUUID());
        }

        boolean hasStoredItems = false;

        hasStoredItems |= handleSlotOnBreak(tile, level, pos, owner, true);
        hasStoredItems |= handleSlotOnBreak(tile, level, pos, owner, false);

        if (hasStoredItems) {
            dropBlockWithContents(tile, level, pos);
        }
    }

    private boolean handleSlotOnBreak(TileMagicTable tile, ServerLevel level, BlockPos pos, @Nullable ServerPlayer owner, boolean isInput) {
        long count = isInput ? tile.getInputCount() : tile.getOutputCount();
        ItemStack type = isInput ? tile.getInputType() : tile.getOutputType();

        if (count <= 0 || type.isEmpty()) return false;

        long emcValue = TileMagicTable.getItemEmcValue(type);

        if (count <= EMC_CONVERT_THRESHOLD) {
            dropItemsAsEntities(tile, level, pos, isInput);
            return false;
        }

        if (emcValue > 0 && owner != null) {
            BigInteger totalEmc = BigInteger.valueOf(emcValue).multiply(BigInteger.valueOf(count));
            TileMagicTable.addPlayerEmc(owner, totalEmc);

            String formattedEmc = MagicTableMenu.formatBigInteger(totalEmc);
            String itemName = type.getHoverName().getString();
            owner.displayClientMessage(
                    new TranslatableComponent("message.ex_enigmaticlegacy.magic_table.emc_converted",
                            count, itemName, formattedEmc),
                    false
            );

            if (isInput) {
                tile.clearInput();
            } else {
                tile.clearOutput();
            }
            return false;
        }

        long toDrop = Math.min(count, EMC_CONVERT_THRESHOLD);
        dropItemsAsEntitiesCount(type, level, pos, toDrop);

        if (isInput) {
            tile.shrinkInput(toDrop);
        } else {
            tile.shrinkOutput(toDrop);
        }

        long remaining = isInput ? tile.getInputCount() : tile.getOutputCount();
        return remaining > 0;
    }

    private void dropItemsAsEntities(TileMagicTable tile, ServerLevel level, BlockPos pos, boolean isInput) {
        long count = isInput ? tile.getInputCount() : tile.getOutputCount();
        ItemStack type = isInput ? tile.getInputType() : tile.getOutputType();

        if (count <= 0 || type.isEmpty()) return;

        dropItemsAsEntitiesCount(type, level, pos, count);

        if (isInput) {
            tile.clearInput();
        } else {
            tile.clearOutput();
        }
    }

    private void dropItemsAsEntitiesCount(ItemStack type, ServerLevel level, BlockPos pos, long totalCount) {
        int maxStack = type.getMaxStackSize();
        long remaining = totalCount;

        while (remaining > 0) {
            int dropCount = (int) Math.min(remaining, maxStack);
            ItemStack dropStack = type.copy();
            dropStack.setCount(dropCount);

            Containers.dropItemStack(level,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    dropStack);

            remaining -= dropCount;
        }
    }

    private void dropBlockWithContents(TileMagicTable tile, ServerLevel level, BlockPos pos) {
        ItemStack blockItem = new ItemStack(this);

        CompoundTag blockEntityTag = new CompoundTag();
        tile.saveToItemTag(blockEntityTag);

        if (!blockEntityTag.isEmpty()) {
            blockItem.addTagElement("BlockEntityTag", blockEntityTag);
        }

        ItemEntity entity = new ItemEntity(level,
                pos.getX() + 0.5, pos.getY() + 0.5,
                pos.getZ() + 0.5, blockItem);
        entity.setDefaultPickUpDelay();
        level.addFreshEntity(entity);

        tile.clearInput();
        tile.clearOutput();
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootContext.@NotNull Builder builder) {
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof TileMagicTable tile) {
            if (tile.getInputCount() > 0 || tile.getOutputCount() > 0) {
                return Collections.emptyList();
            }
        }

        return Collections.singletonList(new ItemStack(this));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        CompoundTag blockTag = stack.getTagElement("BlockEntityTag");
        if (blockTag == null) return;

        long inputCount = blockTag.getLong("inputCount");
        if (inputCount > 0 && blockTag.contains("inputType")) {
            ItemStack inputType = ItemStack.of(blockTag.getCompound("inputType"));
            if (!inputType.isEmpty()) {
                tooltip.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.magic_table.input",
                        inputType.getHoverName().getString(),
                        MagicTableMenu.formatLargeNumber(inputCount))
                        .withStyle(ChatFormatting.YELLOW));
            }
        }

        long outputCount = blockTag.getLong("outputCount");
        if (outputCount > 0 && blockTag.contains("outputType")) {
            ItemStack outputType = ItemStack.of(blockTag.getCompound("outputType"));
            if (!outputType.isEmpty()) {
                tooltip.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.magic_table.output",
                        outputType.getHoverName().getString(),
                        MagicTableMenu.formatLargeNumber(outputCount))
                        .withStyle(ChatFormatting.AQUA));
            }
        }

        if (inputCount > 0 || outputCount > 0) {
            tooltip.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.magic_table.restore_hint")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}