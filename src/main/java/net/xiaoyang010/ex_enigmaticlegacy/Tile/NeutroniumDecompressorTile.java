package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Util.DecompressorManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NeutroniumDecompressorTile extends BlockEntity {

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int DECOMPRESS_TICKS = 100;

    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot == INPUT_SLOT) {
                resetProgress();
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == INPUT_SLOT) {
                return DecompressorManager.instance.canDecompress(stack);
            }
            return false;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == INPUT_SLOT ? 1 : 64;
        }
    };

    private LazyOptional<IItemHandler> handler = LazyOptional.of(() -> inventory);
    private int decompressProgress = 0;

    public NeutroniumDecompressorTile(@NotNull BlockEntityType<NeutroniumDecompressorTile> ne, BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEUTRONIUM_DECOMPRESSOR_TILE.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean wasWorking = isWorking();

        if (canDecompress()) {
            decompressProgress++;

            if (decompressProgress >= DECOMPRESS_TICKS) {
                finishDecompress();
                decompressProgress = 0;
            }
        } else {
            resetProgress();
        }

        if (wasWorking != isWorking()) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private boolean canDecompress() {
        ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
        if (input.isEmpty()) return false;

        DecompressorManager.DecompressRecipeData recipe =
                DecompressorManager.instance.getRecipe(input);

        if (recipe == null) return false;

        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        ItemStack wouldProduce = recipe.getResult(1);

        if (output.isEmpty()) {
            return true;
        }

        if (!output.sameItem(wouldProduce)) {
            return false;
        }

        return output.getCount() + wouldProduce.getCount() <= output.getMaxStackSize();
    }

    private void finishDecompress() {
        ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);

        DecompressorManager.DecompressRecipeData recipe =
                DecompressorManager.instance.getRecipe(input);

        if (recipe == null) return;

        ItemStack result = recipe.getResult(1);

        if (output.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, result);
        } else {
            output.grow(result.getCount());
        }

        input.shrink(1);
    }

    private void resetProgress() {
        if (decompressProgress > 0) {
            decompressProgress = 0;
            setChanged();
        }
    }

    public boolean isWorking() {
        return decompressProgress > 0;
    }

    public int getProgress() {
        return decompressProgress;
    }

    public int getMaxProgress() {
        return DECOMPRESS_TICKS;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("Progress", decompressProgress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        decompressProgress = tag.getInt("Progress");
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handler.invalidate();
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Nullable
    public DecompressorManager.DecompressRecipeData getCurrentRecipe() {
        ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
        return DecompressorManager.instance.getRecipe(input);
    }
}