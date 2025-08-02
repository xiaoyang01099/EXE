package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.IManaPool;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class ManaBracketTile extends BlockEntity {
    private static final int RATE = 1500;
    private static final Random rand = new Random();
    public int _rotation = rand.nextInt(360);
    private ItemHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public ManaBracketTile(@NotNull BlockEntityType<ManaBracketTile> BlockEntityType, BlockPos pos, BlockState state) {
        super(BlockEntityType, pos, state);
    }

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, ManaBracketTile blockEntity) {
        if (level.isClientSide) return;

        ItemStack stack = blockEntity.getItemHandler().getStackInSlot(0);
        if (stack.isEmpty()) return;

        LazyOptional<IManaItem> manaItemCap = stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM);
        if (manaItemCap.isPresent()) {
            IManaItem manaItem = manaItemCap.orElse(null);
            if (manaItem == null) return;

            BlockEntity tilePool = level.getBlockEntity(pos.below());

            if (tilePool instanceof IManaPool) {
                IManaPool pool = (IManaPool) tilePool;

                if (pool.isOutputtingPower()) {
                    if (manaItem.canReceiveManaFromPool(tilePool) && manaItem.getMana() != manaItem.getMaxMana() && pool.getCurrentMana() > 0) {
                        int mana = Math.min(manaItem.getMaxMana() - manaItem.getMana(), RATE);
                        mana = Math.min(pool.getCurrentMana(), mana);
                        pool.receiveMana(-mana);
                        manaItem.addMana(mana);
                        blockEntity.setChanged();
                    }
                } else {
                    if (manaItem.canExportManaToPool(tilePool)) {
                        int currentManaInStack = manaItem.getMana();
                        if (!pool.isFull() && currentManaInStack > 0) {
                            int mana = Math.min(currentManaInStack, RATE);
                            pool.receiveMana(mana);
                            manaItem.addMana(-mana);
                            blockEntity.setChanged();
                        }
                    }
                }
            }
        }
    }

    public boolean handleClick(Player playerIn, InteractionHand hand) {
        if (playerIn.isShiftKeyDown()) return false;

        ItemStack heldItem = playerIn.getItemInHand(hand);
        IItemHandler itemHandler = getItemHandler();
        if (!heldItem.isEmpty() && heldItem.getCapability(BotaniaForgeCapabilities.MANA_ITEM).isPresent()) {
            playerIn.setItemInHand(hand, itemHandler.insertItem(0, heldItem, false));
            return true;
        } else if (heldItem.isEmpty()) {
            ItemHandlerHelper.giveItemToPlayer(playerIn, itemHandler.extractItem(0, 1, false));
            return true;
        } else return false;
    }

    public int getComparatorOutput() {
        ItemStack stack = getItemHandler().getStackInSlot(0);
        if (stack.isEmpty())
            return 0;

        LazyOptional<IManaItem> manaItemCap = stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM);
        if (manaItemCap.isPresent()) {
            IManaItem manaItem = manaItemCap.orElse(null);
            if (manaItem == null) return 0;

            int currentMana = manaItem.getMana();
            int maxMana = manaItem.getMaxMana();

            if (maxMana < 1 || currentMana < 1) return 1;

            return 1 + (int) ((currentMana / (float) maxMana) * 14) + 1;
        }
        return 0;
    }

    public ItemHandler getItemHandler() {
        if (itemHandler == null) {
            itemHandler = new ItemHandler(this);
        }
        return itemHandler;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> getItemHandler());
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        getItemHandler().write(compound);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        getItemHandler().read(compound);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && !this.remove)
            return lazyItemHandler.cast();
        return super.getCapability(cap, side);
    }

    public static class ItemHandler implements IItemHandler {
        private final ManaBracketTile tile;
        private ItemStack item = ItemStack.EMPTY;

        public ItemHandler(ManaBracketTile tile) {
            this.tile = tile;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return item;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (isItemValid(slot, stack)) {
                if (!item.isEmpty())
                    return stack;
                else {
                    ItemStack ret = stack.copy();
                    ret.shrink(1);
                    if (!simulate) {
                        ItemStack toInsert = stack.copy();
                        toInsert.setCount(1);
                        item = toInsert;
                        onContentChanged();
                    }
                    return ret;
                }
            }
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (item.isEmpty())
                return ItemStack.EMPTY;
            else {
                ItemStack ret = item;
                if (!simulate) {
                    item = ItemStack.EMPTY;
                    onContentChanged();
                }
                return ret;
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM).isPresent();
        }

        private CompoundTag write(CompoundTag nbt) {
            nbt.put("Item", item.serializeNBT());
            return nbt;
        }

        private void read(CompoundTag nbt) {
            item = ItemStack.of(nbt.getCompound("Item"));
        }

        private void onContentChanged() {
            tile.setChanged();
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(tile);
        }
    }
}