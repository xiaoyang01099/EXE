package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI.StarlitSanctumCategory;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Container.StarlitSanctumMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.StarlitSanctumRecipe;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.IntStream;

public class StarlitSanctumTile extends RandomizableContainerBlockEntity implements WorldlyContainer {
    // 489个槽位：486个主要区域 + 2个输入槽 + 1个输出槽
    private static final int TOTAL_SLOTS = 489;
    private static final int MAIN_GRID_SLOTS = 486;
    private static final int INPUT_LEFT_SLOT = 486;
    private static final int INPUT_RIGHT_SLOT = 487;
    private static final int OUTPUT_SLOT = 488;

    private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(TOTAL_SLOTS, ItemStack.EMPTY);
    private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new InvWrapper(this));

    public StarlitSanctumTile(BlockPos position, BlockState state) {
        super(ModBlockEntities.STARLIT_SANCTUM_OF_MYSTIQUE.get(), position, state);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (!this.tryLoadLootTable(compound))
            this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compound, this.stacks);
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        if (!this.trySaveLootTable(compound)) {
            ContainerHelper.saveAllItems(compound, this.stacks);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithFullMetadata();
    }

    @Override
    public int getContainerSize() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.stacks)
            if (!itemstack.isEmpty())
                return false;
        return true;
    }

    @Override
    public Component getDefaultName() {
        return new TextComponent("starlit_sanctum_of_mystique");
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new StarlitSanctumMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.worldPosition));
    }

    @Override
    public Component getDisplayName() {
        return new TextComponent("Starlit Sanctum Of Mystique");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (index == OUTPUT_SLOT) {
            return false;
        }
        return true;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        switch (side) {
            case UP:
                return IntStream.concat(
                        IntStream.range(0, MAIN_GRID_SLOTS),
                        IntStream.of(INPUT_LEFT_SLOT, INPUT_RIGHT_SLOT)
                ).toArray();
            case DOWN:
                return new int[]{OUTPUT_SLOT};
            default:
                return IntStream.range(0, this.getContainerSize()).toArray();
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        if (direction == Direction.UP) {
            return index != OUTPUT_SLOT && this.canPlaceItem(index, stack);
        }
        if (direction == Direction.DOWN) {
            return false;
        }
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        if (direction == Direction.DOWN) {
            return index == OUTPUT_SLOT;
        }
        return true;
    }

    /**
     * 设置输出槽位的物品
     */
    public void setOutput(ItemStack stack) {
        this.setItem(OUTPUT_SLOT, stack);
    }

    /**
     * 获取输出槽位的物品
     */
    public ItemStack getOutput() {
        return this.getItem(OUTPUT_SLOT);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (!this.remove && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == null) {
                return itemHandler.cast();
            }
            return handlers[facing.ordinal()].cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        for (LazyOptional<? extends IItemHandler> handler : handlers)
            handler.invalidate();
        itemHandler.invalidate();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, StarlitSanctumTile tile) {
        ItemStack output = tile.getItem(488);

        if (level.getDayTime() % 10 != 0) return; //每0.5s运行一次

        Optional<StarlitSanctumRecipe> recipeFor = level.getRecipeManager().getRecipeFor(ModRecipes.STARLIT_TYPE, tile, level);
        if (recipeFor.isPresent()) {
            StarlitSanctumRecipe recipe = recipeFor.get();
            ItemStack resultItem = recipe.getResultItem();

            if (output.isEmpty()){
                tile.setItem(488, resultItem);
                for (int i = 0; i < 488; i++){
                    tile.getItem(i).shrink(1);
                }
            }else {
                if (resultItem.getItem() == output.getItem() && output.getCount() < output.getMaxStackSize()){
                    output.grow(1);
                    for (int i = 0; i < 488; i++){
                        tile.getItem(i).shrink(1);
                    }
                }
            }
        }
    }
}