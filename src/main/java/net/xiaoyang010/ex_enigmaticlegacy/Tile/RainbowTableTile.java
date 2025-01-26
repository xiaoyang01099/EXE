package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Container.RainbowTableContainer;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.RainbowTableRecipe;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaReceiver;

import java.util.Optional;

public class RainbowTableTile extends BlockEntity implements MenuProvider, Container, IManaReceiver {
    private ContainerData data = new SimpleContainerData(2);
    private final ItemStackHandler itemHandler = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot != 4;
        }
    };
    private static final int SHRINK_MANA = 100000;

    private LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);

    public RainbowTableTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RAINBOW_TABLE_TILE.get(), pos, state);
    }

    public ContainerData getDate() {
        return data;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RainbowTableTile tile) {
        if (level.isClientSide) {
            return;
        }

        BlockEntity entity = level.getBlockEntity(pos.below());
        if (entity instanceof IManaPool manaPool) {
            int mana = manaPool.getCurrentMana();
            if (level.getGameTime() % 20 == 0){
                if (mana >= SHRINK_MANA){
                    manaPool.receiveMana(-SHRINK_MANA);
                    tile.receiveMana(SHRINK_MANA);
                    tile.setChanged();
                }
            }

            tile.receiveMana(-10000); //tick消耗

            Optional<RainbowTableRecipe> recipeFor = level.getRecipeManager().getRecipeFor(ModRecipes.RAINBOW_TABLE_TYPE, tile, level);
            if (recipeFor.isPresent() && tile.getCurrentMana() >= SHRINK_MANA) {
                tile.data.set(0, tile.data.get(0) + 1);
                ItemStack resultItem = recipeFor.get().getResultItem();
                ItemStack out = tile.itemHandler.getStackInSlot(4);
                if (out.isEmpty() && tile.data.get(0) >= 40) {
                    NonNullList<Integer> list = recipeFor.get().getInputCounts();
                    for (int i = 0; i < 4; i++) {
                        tile.itemHandler.extractItem(i, list.get(i), false);
                    }
                    tile.itemHandler.setStackInSlot(4, resultItem);
                    tile.data.set(0, 0);
                    manaPool.receiveMana(-SHRINK_MANA);
                }else {
                    if (ItemStack.isSame(out, resultItem) && out.getCount() + resultItem.getCount() <= out.getMaxStackSize()  && tile.data.get(0) >= 40) {
                        NonNullList<Integer> list = recipeFor.get().getInputCounts();
                        for (int i = 0; i < 4; i++) {
                            tile.itemHandler.extractItem(i, list.get(i), false);
                        }
                        out.setCount(out.getCount() + resultItem.getCount());
                        tile.itemHandler.setStackInSlot(4, out);
                        tile.data.set(0, 0);
                        manaPool.receiveMana(-SHRINK_MANA);
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ?
                handler.cast() : super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handler.invalidate();
    }

    @Override
    public int getContainerSize() {
        return 5;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        return itemHandler.extractItem(slot, count, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        int count = itemHandler.getStackInSlot(slot).getCount();
        return itemHandler.extractItem(slot, count, false);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return getCurrentMana() >= SHRINK_MANA;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("block.ex_enigmaticlegacy.rainbow_table");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new RainbowTableContainer(ModMenus.RAINBOW_TABLE_CONTAINER,
                containerId, inventory, this, worldPosition);
    }

    @Override
    public Level getManaReceiverLevel() {
        return null;
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return null;
    }

    @Override
    public int getCurrentMana() {
        return this.data.get(1);
    }

    @Override
    public boolean isFull() {
        return this.data.get(1) == 500000;
    }

    @Override
    public void receiveMana(int i) {
        this.data.set(1, this.data.get(1) + i);
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }
}