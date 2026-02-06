package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.inventory.InventorySimple;
import codechicken.lib.inventory.InventoryUtils;
import codechicken.lib.util.ItemUtils;
import com.google.common.collect.ImmutableList;
import morph.avaritia.api.CompressorRecipe;
import morph.avaritia.recipe.CompressorRecipeHelper;
import morph.avaritia.tile.MachineTileBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TileEntityInfinityCompressor extends MachineTileBase {
    public static int CONSUME_TICKS = 1;
    public final Inventory inventory = new Inventory();
    private ItemStack targetStack;
    private Ingredient inputStack;
    private int compressionTarget;
    private int consumptionProgress;
    private int compressionProgress;
    private List<ItemStack> c_InputItems;
    private LazyOptional<IItemHandler> nullWrapper;
    private LazyOptional<IItemHandlerModifiable>[] sidedWrappers;

    public TileEntityInfinityCompressor(@NotNull BlockEntityType<TileEntityInfinityCompressor> tileEntityInfinityCompressorBlockEntityType, BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFINITY_COMPRESSOR_TILE.get(), pos, state);
        this.targetStack = ItemStack.EMPTY;
        this.inputStack = Ingredient.EMPTY;
        this.c_InputItems = ImmutableList.of();
        this.nullWrapper = LazyOptional.of(() -> {
            return new InvWrapper(this.inventory);
        });
        this.sidedWrappers = SidedInvWrapper.create(this.inventory, Direction.values());
    }

    public void onLoad() {
        super.onLoad();
        if (this.compressionTarget == -1) {
            CompressorRecipe recipe = CompressorRecipeHelper.getRecipeFromResult(this.level, this.targetStack);
            if (recipe == null) {
                this.targetStack = ItemStack.EMPTY;
                this.compressionTarget = 0;
                this.consumptionProgress = 0;
                this.compressionProgress = 0;
            } else {
                this.compressionTarget = recipe.getCost();
                this.inputStack = recipe.getIngredients().get(0);
            }
        }

    }

    protected void doWork() {
        if (this.targetStack.isEmpty() || this.inputStack.isEmpty()) {
            this.fullContainerSync = true;
            CompressorRecipe recipe = null;
            if (this.inventory.getInput().isEmpty()){
                BlockEntity blockEntity = level.getBlockEntity(getBlockPos().above());
                if (blockEntity instanceof RandomizableContainerBlockEntity chest){
                    for (int i = 0; i < chest.getContainerSize(); i++) {
                        ItemStack stack = chest.getItem(i);
                        if (!stack.isEmpty()) {
                            recipe = CompressorRecipeHelper.getRecipe(this.level, stack);
                        }
                    }
                }
            }else {
                recipe = CompressorRecipeHelper.getRecipe(this.level, this.inventory.getInput());
            }
            this.targetStack = recipe.getResultItem();
            inputStack = recipe.getIngredients().get(0);
            this.compressionTarget = recipe.getCost();
        }

        Level world = this.getLevel();
        if (world != null && world.getGameTime() % 5 == 0){
            BlockPos above = this.getBlockPos().above();
            BlockEntity blockEntity = world.getBlockEntity(above);
            if (blockEntity instanceof RandomizableContainerBlockEntity chest && !inputStack.isEmpty()){
                for (int i = 0; i < chest.getContainerSize(); i++){
                    ItemStack stack = chest.getItem(i);
                    if (!stack.isEmpty() && inputStack.test(stack)){
                        ItemStack input = this.inventory.getInput();
                        if (input.isEmpty()){
                            inventory.setItem(0, stack.copy());
                            stack.setCount(0);
                        }else {
                            int num = input.getMaxStackSize() - input.getCount();
                            input.grow(num);
                            stack.shrink(num);
                        }
                        break;
                    }
                }
            }
        }

        ++this.consumptionProgress;
        if (this.consumptionProgress >= CONSUME_TICKS) {
            this.consumptionProgress = 0;
            ItemStack input = this.inventory.getInput();
            this.compressionProgress += input.getCount();
            input.setCount(0);
            this.inventory.setItem(0, ItemStack.EMPTY);
        }

        if (this.compressionProgress >= this.compressionTarget) {
            ItemStack output = this.inventory.getOutput();
            if (output.isEmpty()) {
                this.inventory.setItem(1, ItemUtils.copyStack(this.targetStack, 1));
                this.compressionProgress -= this.compressionTarget;
                this.targetStack = ItemStack.EMPTY;
                this.fullContainerSync = true;
            } else {
                if (!output.equals(targetStack, true)){
                } else {
                    this.inventory.setItem(1, ItemUtils.copyStack(output, output.getCount() + 1));
                    this.compressionProgress -= this.compressionTarget;
                    this.targetStack = ItemStack.EMPTY;
                    this.fullContainerSync = true;
                }
            }
        }
    }

    protected void onWorkStopped() {
        this.consumptionProgress = 0;
    }

    public void dropContents() {
        BlockPos pos = this.getBlockPos();
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), this.inventory.getItem(1));
        if (!inputStack.isEmpty()) {
            ItemStack stack = inputStack.getItems()[0];
            stack.setCount(this.compressionProgress);
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
    }

    protected boolean canWork() {
        ItemStack input = this.inventory.getInput();
        if (input.isEmpty()) {
            BlockEntity blockEntity = this.level.getBlockEntity(this.getBlockPos().above());
            if (blockEntity instanceof RandomizableContainerBlockEntity chest) {
                return !chest.isEmpty();
            }
            return false;
        } else if (!this.targetStack.isEmpty()) {
            return CompressorRecipeHelper.inputMatchesRecipeForOutput(this.level, input, this.targetStack);
        } else {
            CompressorRecipe recipe = CompressorRecipeHelper.getRecipe(this.level, input);
            if (recipe == null) {
                return false;
            } else {
                ItemStack output = this.inventory.getOutput();
                if (output.isEmpty()) {
                    return true;
                } else {
                    return recipe.getResultItem().sameItem(output) && output.getCount() < Math.min(output.getMaxStackSize(), this.inventory.getMaxStackSize());
                }
            }
        }
    }

    public void writeGuiData(MCDataOutput packet, boolean isFullSync) {
        packet.writeVarInt(this.consumptionProgress);
        packet.writeVarInt(this.compressionProgress);
        if (isFullSync) {
            packet.writeItemStack(this.targetStack);
            packet.writeVarInt(this.compressionTarget);
        }

    }

    public void readGuiData(MCDataInput packet, boolean isFullSync) {
        this.consumptionProgress = packet.readVarInt();
        this.compressionProgress = packet.readVarInt();
        if (isFullSync) {
            this.targetStack = packet.readItemStack();
            this.compressionTarget = packet.readVarInt();
            List<ItemStack> inputs = new LinkedList();
            if (!this.targetStack.isEmpty()) {
                List<Ingredient> ings = CompressorRecipeHelper.getRecipeFromResult(this.level, this.targetStack).getIngredients();
                Iterator var5 = ings.iterator();

                while(var5.hasNext()) {
                    Ingredient ing = (Ingredient)var5.next();
                    Collections.addAll(inputs, ing.getItems());
                }
            }

            this.c_InputItems = inputs;
        }

    }

    public int getCompressionProgress() {
        return this.compressionProgress;
    }

    public int getCompressionTarget() {
        return this.compressionTarget;
    }

    public int getConsumptionProgress() {
        return this.consumptionProgress;
    }

    public int getConsumptionTarget() {
        return this.compressionProgress;
    }

    public ItemStack getTargetStack() {
        return this.targetStack;
    }

    public List<ItemStack> getInputItems() {
        return this.c_InputItems;
    }

    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", InventoryUtils.writeItemStacksToTag(this.inventory.items));
        tag.put("target", this.targetStack.save(new CompoundTag()));
        tag.putInt("consumptionProgress", this.consumptionProgress);
        tag.putInt("compressionProgress", this.compressionProgress);
    }

    public void load(CompoundTag tag) {
        super.load(tag);
        InventoryUtils.readItemStacksFromTag(this.inventory.items, tag.getList("inventory", 10));
        this.consumptionProgress = tag.getInt("consumptionProgress");
        this.compressionProgress = tag.getInt("compressionProgress");
        this.targetStack = ItemStack.of(tag.getCompound("target"));
        this.compressionTarget = -1;
        this.fullContainerSync = true;
    }

    public void invalidateCaps() {
        super.invalidateCaps();
        this.nullWrapper.invalidate();
        LazyOptional[] var1 = this.sidedWrappers;
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            LazyOptional<IItemHandlerModifiable> sidedWrapper = var1[var3];
            sidedWrapper.invalidate();
        }

    }

    public void reviveCaps() {
        super.reviveCaps();
        this.nullWrapper = LazyOptional.of(() -> {
            return new InvWrapper(this.inventory);
        });
        this.sidedWrappers = SidedInvWrapper.create(this.inventory, Direction.values());
    }

    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (!this.remove && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return side == null ? this.nullWrapper.cast() : this.sidedWrappers[side.ordinal()].cast();
        } else {
            return super.getCapability(cap, side);
        }
    }

    public class Inventory extends InventorySimple implements WorldlyContainer {
        public Inventory() {
            super(2);
        }

        public ItemStack getInput() {
            return this.items[0];
        }

        public ItemStack getOutput() {
            return this.items[1];
        }

        public void setChanged() {
            TileEntityInfinityCompressor.this.setChanged();
        }

        public int[] getSlotsForFace(Direction side) {
            return side == Direction.UP ? new int[]{0} : new int[]{1};
        }

        public boolean canPlaceItem(int slot, ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            } else if (slot != 0) {
                return false;
            } else if (TileEntityInfinityCompressor.this.targetStack.isEmpty()) {
                return CompressorRecipeHelper.getRecipe(TileEntityInfinityCompressor.this.level, stack) != null;
            } else {
                return CompressorRecipeHelper.inputMatchesRecipeForOutput(TileEntityInfinityCompressor.this.level, stack, TileEntityInfinityCompressor.this.targetStack);
            }
        }

        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
            return this.canPlaceItem(slot, stack);
        }

        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
            return slot == 1 && side != Direction.UP;
        }
    }
}