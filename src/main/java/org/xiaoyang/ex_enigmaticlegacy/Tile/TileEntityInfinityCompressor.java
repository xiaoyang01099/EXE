package org.xiaoyang.ex_enigmaticlegacy.Tile;

import com.yuo.endless.recipe.EndlessRecipes;
import com.yuo.endless.recipe.NeutroniumRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;
import org.xiaoyang.ex_enigmaticlegacy.Block.BlockInfinityCompressor;
import org.xiaoyang.ex_enigmaticlegacy.Container.ContainerInfinityCompressor;

import java.util.Optional;

public class TileEntityInfinityCompressor extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int OUTPUT_SLOT = 0;
    public static final int INPUT_SLOT = 1;
    private static final int SLOT_COUNT = 243;
    public static final int CONSUME_TICKS = 10;
    private static final int[] ALL_SLOTS = new int[SLOT_COUNT];
    private static final String TAG_PROGRESS = "CompressionProgress";
    private static final String TAG_RECIPE = "CompressionRecipe";
    private static final String TAG_CONSUMPTION = "ConsumptionProgress";
    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress;
    private int consumptionProgress;
    private ResourceLocation recipeId;
    private boolean trashInvalid = true;
    private int redstoneMode;
    private LazyOptional<? extends IItemHandler>[] sidedHandlers;

    public TileEntityInfinityCompressor(BlockEntityType<TileEntityInfinityCompressor> type,
                                        BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileEntityInfinityCompressor tile) {
        boolean working = tile.processOneItem();
        if (state.getValue(BlockInfinityCompressor.ACTIVE) != working) {
            level.setBlock(pos, state.setValue(BlockInfinityCompressor.ACTIVE, working), 3);
        }
    }

    private boolean processOneItem() {
        if (level == null || level.isClientSide) return false;
        if (!canOperate()) return false;

        NeutroniumRecipe recipe = findFirstRecipe();
        if (recipe == null || !canAcceptRecipe(recipe)) {
            consumptionProgress = 0;
            return false;
        }
        if (recipeId == null) recipeId = recipe.getId();

        ItemStack result = recipe.getResultItem().copy();
        ItemStack output = getItem(OUTPUT_SLOT);
        if (!canStore(output, result)) {
            return false;
        }

        consumptionProgress++;
        if (consumptionProgress < CONSUME_TICKS) return true;
        consumptionProgress = 0;

        int consumed = 0;
        for (int slot = INPUT_SLOT; slot < SLOT_COUNT; slot++) {
            ItemStack input = getItem(slot);
            NeutroniumRecipe inputRecipe = input.isEmpty() ? null : findRecipe(input);
            if (inputRecipe != null && recipe.getId().equals(inputRecipe.getId())) {
                consumed += input.getCount();
                setItem(slot, ItemStack.EMPTY);
            }
        }
        if (consumed == 0) return false;

        int cost = Math.max(1, recipe.getRecipeCount());
        progress += consumed;
        if (progress >= cost) {
            progress -= cost;
            if (output.isEmpty()) setItem(OUTPUT_SLOT, result);
            else output.grow(result.getCount());
            if (progress == 0) recipeId = null;
        }
        setChanged();
        syncToClient();
        return true;
    }

    @Nullable
    private NeutroniumRecipe findFirstRecipe() {
        for (int slot = INPUT_SLOT; slot < SLOT_COUNT; slot++) {
            ItemStack stack = getItem(slot);
            if (!stack.isEmpty()) {
                NeutroniumRecipe recipe = findRecipe(stack);
                if (recipe != null && (recipeId == null || recipeId.equals(recipe.getId()))) return recipe;
            }
        }
        return null;
    }

    private boolean canAcceptRecipe(NeutroniumRecipe recipe) {
        if (recipeId == null) return true;
        return recipeId.equals(recipe.getId());
    }

    @Nullable
    private NeutroniumRecipe findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) return null;
        Container input = new net.minecraft.world.SimpleContainer(stack);
        Optional<NeutroniumRecipe> recipe = level.getRecipeManager().getRecipeFor(
                EndlessRecipes.NEUTRONIUM_RECIPE.get(), input, level);
        return recipe.orElse(null);
    }

    private static boolean canStore(ItemStack output, ItemStack result) {
        if (output.isEmpty()) return true;
        return ItemStack.isSameItemSameTags(output, result)
                && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public int getProgress() {
        return progress;
    }

    public int getConsumptionProgress() {
        return consumptionProgress;
    }

    public int getProgressTarget() {
        if (recipeId == null || level == null) return 0;
        NeutroniumRecipe recipe = findRecipeForId(recipeId);
        return recipe == null ? 0 : Math.max(1, recipe.getRecipeCount());
    }

    @Nullable
    private NeutroniumRecipe findRecipeForId(ResourceLocation id) {
        if (level == null) return null;
        for (NeutroniumRecipe recipe : level.getRecipeManager().getAllRecipesFor(EndlessRecipes.NEUTRONIUM_RECIPE.get())) {
            if (id.equals(recipe.getId())) return recipe;
        }
        return null;
    }

    public ItemStack getPreviewResult() {
        if (recipeId == null) return ItemStack.EMPTY;
        NeutroniumRecipe recipe = findRecipeForId(recipeId);
        return recipe == null ? ItemStack.EMPTY : recipe.getResultItem().copy();
    }

    public boolean isTrashInvalid() { return trashInvalid; }
    public int getRedstoneMode() { return redstoneMode; }
    public boolean canOperate() {
        boolean powered = level != null && level.hasNeighborSignal(worldPosition);
        return redstoneMode == 0 || (redstoneMode == 1 ? !powered : powered);
    }
    public void clearRecipe() {
        recipeId = null;
        progress = 0;
        consumptionProgress = 0;
        setChanged();
        syncToClient();
    }
    public void setRecipe(ResourceLocation id) {
        if (level == null || id == null) return;
        if (findRecipeForId(id) != null) {
            recipeId = id;
            progress = 0;
            consumptionProgress = 0;
            setChanged();
            syncToClient();
        }
    }
    public void toggleTrashInvalid() {
        trashInvalid = !trashInvalid;
        setChanged();
        syncToClient();
    }
    public void cycleRedstoneMode() {
        redstoneMode = (redstoneMode + 1) % 3;
        setChanged();
        syncToClient();
    }

    /** Matches WanionLib's right-click behaviour for RedstoneControlWButton. */
    public void cycleRedstoneModeBackward() {
        redstoneMode = (redstoneMode + 2) % 3;
        setChanged();
        syncToClient();
    }

    public void dropContents() {
        if (level != null && !level.isClientSide) {
            Containers.dropContents(level, worldPosition, this);
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.ex_enigmaticlegacy.infinity_compressor.name");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new ContainerInfinityCompressor(id, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack item : items) if (!item.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != OUTPUT_SLOT && !stack.isEmpty() && trashInvalid) {
            NeutroniumRecipe recipe = findRecipe(stack);
            if (recipe == null || (recipeId != null && !canAcceptRecipe(recipe))) {
                items.set(slot, ItemStack.EMPTY);
                setChanged();
                return;
            }
        }
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return ALL_SLOTS.clone();
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot != OUTPUT_SLOT && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == OUTPUT_SLOT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == OUTPUT_SLOT || stack.isEmpty()) return false;
        NeutroniumRecipe recipe = findRecipe(stack);
        if (recipe == null) return trashInvalid;
        return recipeId == null || canAcceptRecipe(recipe) || trashInvalid;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) items.set(i, ItemStack.EMPTY);
        progress = 0;
        consumptionProgress = 0;
        recipeId = null;
        setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        progress = tag.getInt(TAG_PROGRESS);
        consumptionProgress = tag.getInt(TAG_CONSUMPTION);
        trashInvalid = !tag.contains("TrashInvalid") || tag.getBoolean("TrashInvalid");
        redstoneMode = tag.getInt("RedstoneMode");
        if (tag.contains(TAG_RECIPE, Tag.TAG_STRING)) {
            recipeId = ResourceLocation.tryParse(tag.getString(TAG_RECIPE));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt(TAG_PROGRESS, progress);
        tag.putInt(TAG_CONSUMPTION, consumptionProgress);
        tag.putBoolean("TrashInvalid", trashInvalid);
        tag.putInt("RedstoneMode", redstoneMode);
        if (recipeId != null) tag.putString(TAG_RECIPE, recipeId.toString());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
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
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            if (sidedHandlers == null) {
                sidedHandlers = SidedInvWrapper.create(this, Direction.values());
            }
            return (side == null ? LazyOptional.of(() -> new net.minecraftforge.items.wrapper.InvWrapper(this))
                    : sidedHandlers[side.ordinal()]).cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        if (sidedHandlers != null) {
            for (LazyOptional<? extends IItemHandler> handler : sidedHandlers) handler.invalidate();
            sidedHandlers = null;
        }
    }

    static {
        for (int i = 0; i < ALL_SLOTS.length; i++) ALL_SLOTS[i] = i;
    }
}
