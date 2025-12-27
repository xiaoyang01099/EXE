package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import morph.avaritia.api.ExtremeCraftingRecipe;
import morph.avaritia.init.AvaritiaModContent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.xiaoyang010.ex_enigmaticlegacy.Block.BlockExtremeAutoCrafter;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ContainerExtremeAutoCrafter;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import net.xiaoyang010.ex_enigmaticlegacy.api.ExtremeRecipeField;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.*;

import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IControl;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class TileEntityExtremeAutoCrafter extends WTileEntity implements MenuProvider {
    private static final int POWER_MULTIPLIER = 10;
    private static final int CAPACITY_MULTIPLIER = 100;

    public final int full = getContainerSize() - 1, half = full / 2, powerConsumption = half * POWER_MULTIPLIER;
    public final RedstoneControl redstoneControl;
    public final EnergyControl energyControl;
    private final ExtremeCraftingMatrix extremeCraftingMatrix = new ExtremeCraftingMatrix((int) Math.sqrt(half));
    private final ControlController controlController = getController(ControlController.class);
    private final Collection<IControl<?>> allControls = controlController.getInstances();
    private final ExtremeRecipeField extremeRecipeField = new ExtremeRecipeField();
    private Int2IntMap patternMap = null;

    public TileEntityExtremeAutoCrafter(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        controlController.add((this.redstoneControl = new RedstoneControl(this)));
        controlController.add((this.energyControl = new EnergyControl(powerConsumption * CAPACITY_MULTIPLIER, powerConsumption)));
        getController(FieldController.class).add(extremeRecipeField);
        addCapability(CapabilityEnergy.ENERGY, energyControl);
        addCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, new ItemHandlerExtremeAutoCrafter(this));
    }

    public static void tick(TileEntityExtremeAutoCrafter tileEntity)
    {
        tileEntity.serverTick();
    }

    public void serverTick() {
        if (level == null || level.isClientSide)
            return;
//        if (!allControls.stream().allMatch(IControl::canOperate))
//            return;
        BlockPos pos = this.getBlockPos();
        BlockState blockState = level.getBlockState(pos);
        Boolean value = blockState.getValue(BlockExtremeAutoCrafter.POWERED);
        if (!value) return;

        if (extremeRecipeField.isNull()) {
            if (patternMap != null)
                patternMap = null;
            return;
        }
        final ItemStack recipeStack = extremeRecipeField.getExtremeRecipeOutput();
        final ItemStack outputStack = itemStacks.get(163);
//        if (recipeStack.isEmpty()){
//            if (outputStack.isEmpty()){
//
//            }
//            itemStacks.set(162, ItemStack.EMPTY); //配方输出设为空
//        }
        if (recipeStack.isEmpty() || (!outputStack.isEmpty() && outputStack.getCount() == outputStack.getMaxStackSize()))
            return;
        if (patternMap == null)
            patternMap = MetaItem.getKeySizeMap(half, full, itemStacks);
        if (outputStack.isEmpty() && notMatches(MetaItem.getSmartKeySizeMap(0, half, itemStacks), patternMap))
            return;
        else if (!outputStack.isEmpty() && outputStack.getCount() + recipeStack.getCount() > outputStack.getMaxStackSize() || notMatches(MetaItem.getSmartKeySizeMap(0, half, itemStacks), patternMap))
            return;
        allControls.forEach(IControl::operate);
        cleanInput();
        if (outputStack.isEmpty())
            itemStacks.set(163, recipeStack.copy());
        else
            outputStack.setCount(outputStack.getCount() + recipeStack.getCount());
        setChanged();
    }

    public ExtremeRecipeField getExtremeRecipeField()
    {
        return extremeRecipeField;
    }

    @NotNull
    @Override
    public String getDefaultName() {
        return "container.extreme_auto_crafter.name";
    }

    @Override
    public Component getDisplayName() {
        return EComponent.translatable(getDefaultName());
    }

    private boolean notMatches(@NotNull final Int2IntMap inputMap, @NotNull final Int2IntMap patternMap) {
        if (inputMap.size() >= patternMap.size() && inputMap.keySet().containsAll(patternMap.keySet())) {
            for (final int key : patternMap.keySet())
                if (inputMap.get(key) < patternMap.get(key))
                    return true;
            return false;
        } else
            return true;
    }

    private void cleanInput() {
        final Int2IntMap patternMap = new Int2IntOpenHashMap(this.patternMap);
        for (int i = 0; i < half && !patternMap.isEmpty(); i++) {
            final ItemStack itemStack = itemStacks.get(i);
            final int key = MetaItem.get(itemStack);
            if (patternMap.containsKey(key)) {
                final int total = patternMap.get(key);
                final int dif = Mth.clamp(total, 1, itemStack.getCount());
                if (itemStack.getItem() == Items.WATER_BUCKET || itemStack.getItem() == Items.LAVA_BUCKET) {
                    setItem(i, new ItemStack(Items.BUCKET));
                }
                else if (itemStack.getItem().hasCraftingRemainingItem()) {
                    ItemStack remainingItem = itemStack.getItem().getCraftingRemainingItem().getDefaultInstance();
                    if (!remainingItem.isEmpty()) {
                        setItem(i, remainingItem);
                    } else {
                        itemStack.setCount(itemStack.getCount() - dif);
                    }
                }
                else {
                    itemStack.setCount(itemStack.getCount() - dif);
                }

                if (dif - total == 0)
                    patternMap.remove(key);
                else
                    patternMap.put(key, total - dif);
                if (itemStack.getCount() == 0)
                    itemStacks.set(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void readCustomNBT(@NotNull final CompoundTag compoundTag) {
        super.readCustomNBT(compoundTag);
        recipeShapeChanged();
    }

    @Override
    public int getContainerSize()
    {
        return 164;
    }

    public void recipeShapeChanged() {
        ExtremeCraftingRecipe matchedRecipe = null;
        if (level != null) {
            for (final ExtremeCraftingRecipe extremeRecipe : level.getRecipeManager().getAllRecipesFor(AvaritiaModContent.EXTREME_CRAFTING_RECIPE_TYPE.get())) {
                if (extremeRecipe.matches(extremeCraftingMatrix, level)) {
                    matchedRecipe = extremeRecipe;
                    break;
                }
            }
        }
        extremeRecipeField.setExtremeRecipe(matchedRecipe);
        if (matchedRecipe == null) itemStacks.set(162, ItemStack.EMPTY);
        patternMap = null;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ContainerExtremeAutoCrafter(containerId, playerInventory,this);
    }

    private final class ExtremeCraftingMatrix extends CraftingContainer {
        final int square;

        private ExtremeCraftingMatrix(final int squareRoot) {
            super(new AbstractContainerMenu(null, -1) {
                @Override
                public @NotNull ItemStack quickMoveStack(@NotNull final Player player, final int index) {
                    return ItemStack.EMPTY;
                }

                @Override
                public boolean stillValid(@NotNull final Player player) {
                    return false;
                }
            }, squareRoot, squareRoot);
            this.square = squareRoot * squareRoot;
        }

        @Override
        @NotNull
        public ItemStack getItem(final int slot)
        {
            return itemStacks.get(square + slot);
        }
    }

    private static class ItemHandlerExtremeAutoCrafter extends InvWrapper {
        private final TileEntityExtremeAutoCrafter tileEntityAutoBiggerCraftingTable;

        private ItemHandlerExtremeAutoCrafter(@NotNull final TileEntityExtremeAutoCrafter tileEntityAutoBiggerCraftingTable) {
            super(tileEntityAutoBiggerCraftingTable);
            this.tileEntityAutoBiggerCraftingTable = tileEntityAutoBiggerCraftingTable;
        }

        @NotNull
        @Override
        public ItemStack insertItem(final int slot, @NotNull final ItemStack stack, final boolean simulate) {
            return slot >= tileEntityAutoBiggerCraftingTable.half ? stack : super.insertItem(slot, stack, simulate);
        }

        @NotNull
        @Override
        public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
            boolean full = slot == tileEntityAutoBiggerCraftingTable.full;
            final ItemStack slotStack = simulate ? getStackInSlot(slot).copy() : getStackInSlot(slot);

            if (full || slotStack.getItem() == Items.BUCKET) {
                if (slotStack.isEmpty())
                    return ItemStack.EMPTY;
                final ItemStack newStack = slotStack.copy();
                final int newStackSize = Mth.clamp(amount, 1, newStack.getCount());
                newStack.setCount(newStackSize);
                slotStack.setCount(slotStack.getCount() - newStackSize);
                if (!simulate && slotStack.isEmpty()) {
                    setStackInSlot(slot, ItemStack.EMPTY);
                    getInv().setChanged();
                }
                return newStack;
            } else {
                return ItemStack.EMPTY;
            }
        }
    }
}