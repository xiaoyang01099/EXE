package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Container.MagicTableMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Container.slot.MagicTableItemHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Container.slot.MagicTableRecipeInput;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.MagicTableRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TileMagicTable extends BlockEntity implements MenuProvider {
    private final MagicTableItemHandler externalHandler = new MagicTableItemHandler(this);
    private final LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> externalHandler);
    private ItemStack inputType = ItemStack.EMPTY;
    private long inputCount = 0;
    private ItemStack outputType = ItemStack.EMPTY;
    private long outputCount = 0;
    private int progress = 0;
    private int progressMax = 0;
    private ResourceLocation currentRecipeId = null;
    private transient MagicTableRecipe cachedRecipe = null;
    private UUID craftingPlayerUUID = null;
    private int convertGear = 0;
    private long customConvertAmount = 1;

    public TileMagicTable(@NotNull BlockEntityType<TileMagicTable> tileType, BlockPos pos, BlockState state) {
        super(tileType, pos, state);
    }

    public ItemStack getInputDisplay() {
        if (inputCount <= 0 || inputType.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack display = inputType.copy();
        display.setCount((int) Math.min(inputCount, Integer.MAX_VALUE));
        return display;
    }

    public static Optional<MagicTableRecipe> findRecipe(Level level, ItemStack input) {
        if (level == null || input.isEmpty()) {
            return Optional.empty();
        }

        MagicTableRecipeInput container = new MagicTableRecipeInput(input);

        return level.getRecipeManager()
                .getRecipeFor(ModRecipes.MAGIC_TABLE_TYPE.get(), container, level);
    }

    public static List<MagicTableRecipe> getAllRecipes(Level level) {
        if (level == null) return List.of();

        return level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.MAGIC_TABLE_TYPE.get());
    }

    public static Optional<MagicTableRecipe> findById(Level level, String recipeId) {
        if (level == null) return Optional.empty();

        return getAllRecipes(level).stream()
                .filter(r -> r.getId().toString().equals(recipeId))
                .findFirst();
    }

    public ItemStack insertInput(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        if (inputType.isEmpty() || inputCount <= 0) {
            if (!simulate) {
                inputType = stack.copy();
                inputType.setCount(1);
                inputCount = stack.getCount();
                setChanged();
            }
            return ItemStack.EMPTY;
        }

        if (!canStack(inputType, stack)) {
            return stack;
        }

        long maxAdd = Long.MAX_VALUE - inputCount;
        int toAdd = (int) Math.min(stack.getCount(), maxAdd);

        if (!simulate) {
            inputCount += toAdd;
            setChanged();
        }

        if (toAdd >= stack.getCount()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack remainder = stack.copy();
            remainder.setCount(stack.getCount() - toAdd);
            return remainder;
        }
    }

    public ItemStack extractInput(int requestCount) {
        if (inputCount <= 0 || inputType.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int maxStack = inputType.getMaxStackSize();
        int actualExtract = (int) Math.min(requestCount, Math.min(maxStack, inputCount));

        if (actualExtract <= 0) return ItemStack.EMPTY;

        ItemStack result = inputType.copy();
        result.setCount(actualExtract);

        inputCount -= actualExtract;
        if (inputCount <= 0) {
            inputCount = 0;
            inputType = ItemStack.EMPTY;
        }

        setChanged();
        return result;
    }

    public ItemStack peekInput(int requestCount) {
        if (inputCount <= 0 || inputType.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int maxStack = inputType.getMaxStackSize();
        int actualCount = (int) Math.min(requestCount, Math.min(maxStack, inputCount));
        ItemStack result = inputType.copy();
        result.setCount(actualCount);
        return result;
    }

    public ItemStack getOutputDisplay() {
        if (outputCount <= 0 || outputType.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack display = outputType.copy();
        display.setCount((int) Math.min(outputCount, Integer.MAX_VALUE));
        return display;
    }

    public ItemStack extractOutput(int requestCount) {
        if (outputCount <= 0 || outputType.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int maxStack = outputType.getMaxStackSize();
        int actualExtract = (int) Math.min(requestCount, Math.min(maxStack, outputCount));

        if (actualExtract <= 0) return ItemStack.EMPTY;

        ItemStack result = outputType.copy();
        result.setCount(actualExtract);

        outputCount -= actualExtract;
        if (outputCount <= 0) {
            outputCount = 0;
            outputType = ItemStack.EMPTY;
        }

        setChanged();
        return result;
    }

    public ItemStack peekOutput(int requestCount) {
        if (outputCount <= 0 || outputType.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int maxStack = outputType.getMaxStackSize();
        int actualCount = (int) Math.min(requestCount, Math.min(maxStack, outputCount));
        ItemStack result = outputType.copy();
        result.setCount(actualCount);
        return result;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileMagicTable tile) {
        if (level.isClientSide) return;

        if (tile.inputCount <= 0 || tile.inputType.isEmpty()) {
            tile.resetCrafting();
            return;
        }

        ItemStack matchStack = tile.inputType.copy();
        matchStack.setCount((int) Math.min(tile.inputCount, Integer.MAX_VALUE));

        Optional<MagicTableRecipe> recipeOpt = findRecipe(level, matchStack);
        if (recipeOpt.isEmpty()) {
            tile.resetCrafting();
            return;
        }

        MagicTableRecipe recipe = recipeOpt.get();

        if (tile.inputCount < recipe.getInputCount()) {
            tile.resetCrafting();
            return;
        }

        if (tile.craftingPlayerUUID == null || level.getServer() == null) {
            tile.resetCrafting();
            return;
        }
        Player player = level.getServer().getPlayerList().getPlayer(tile.craftingPlayerUUID);
        if (player == null) {
            tile.resetCrafting();
            return;
        }

        if (tile.outputCount > 0 && !tile.outputType.isEmpty()) {
            if (!canStack(tile.outputType, recipe.getOutput())) {
                return;
            }
        }

        if (tile.currentRecipeId == null || !tile.currentRecipeId.equals(recipe.getId())) {
            tile.currentRecipeId = recipe.getId();
            tile.cachedRecipe = recipe;
            tile.progress = 0;
            tile.progressMax = recipe.getCraftTicks();
        }

        BigInteger playerEmc = getPlayerEmc(player);
        if (playerEmc.compareTo(recipe.getEmcCost()) < 0) {
            return;
        }

        tile.progress++;

        if (tile.progress >= tile.progressMax) {
            tile.completeCraft(player, recipe);
            tile.progress = 0;
        }

        tile.setChanged();
    }

    private void completeCraft(Player player, MagicTableRecipe recipe) {
        long consumeCount = recipe.getInputCount();
        if (inputCount < consumeCount) return;

        inputCount -= consumeCount;
        if (inputCount <= 0) {
            inputCount = 0;
            inputType = ItemStack.EMPTY;
        }

        removePlayerEmc(player, recipe.getEmcCost());

        long toAdd = recipe.getOutputCount();

        if (outputType.isEmpty() || outputCount <= 0) {
            outputType = recipe.getOutput();
            outputType.setCount(1);
            outputCount = 0;
        }

        if (toAdd > 0 && outputCount > Long.MAX_VALUE - toAdd) {
            outputCount = Long.MAX_VALUE;
        } else {
            outputCount += toAdd;
        }

        setChanged();
    }

    private void resetCrafting() {
        if (progress != 0 || progressMax != 0) {
            progress = 0;
            progressMax = 0;
            currentRecipeId = null;
            cachedRecipe = null;
            setChanged();
        }
    }

    public BigInteger convertOutputToEmc(Player player) {
        if (outputCount <= 0 || outputType.isEmpty()) return BigInteger.ZERO;

        long itemEmc = getItemEmcValue(outputType);
        if (itemEmc <= 0) return BigInteger.ZERO;

        long convertCount = getConvertCount();
        if (convertCount <= 0) return BigInteger.ZERO;

        long actualConvert = Math.min(convertCount, outputCount);

        BigInteger totalEmc = BigInteger.valueOf(itemEmc).multiply(BigInteger.valueOf(actualConvert));
        addPlayerEmc(player, totalEmc);

        outputCount -= actualConvert;
        if (outputCount <= 0) {
            outputCount = 0;
            outputType = ItemStack.EMPTY;
        }

        setChanged();
        return totalEmc;
    }

    public long getConvertCount() {
        switch (convertGear) {
            case 0: return 1;
            case 1: return 10;
            case 2: return 64;
            case 3: return 1000;
            case 4: return outputCount;
            case 5: return customConvertAmount;
            default: return 1;
        }
    }

    public static String getGearName(int gear) {
        switch (gear) {
            case 0: return "1x";
            case 1: return "10x";
            case 2: return "64x";
            case 3: return "1000x";
            case 4: return "ALL";
            case 5: return "Custom";
            default: return "1x";
        }
    }

    public void cycleGearForward() { convertGear = (convertGear + 1) % 6; setChanged(); }
    public void cycleGearBackward() { convertGear = (convertGear - 1 + 6) % 6; setChanged(); }
    public int getConvertGear() { return convertGear; }
    public void setConvertGear(int gear) { this.convertGear = Math.max(0, Math.min(5, gear)); setChanged(); }
    public long getCustomConvertAmount() { return customConvertAmount; }
    public void setCustomConvertAmount(long amount) { this.customConvertAmount = Math.max(1, amount); setChanged(); }

    private static boolean canStack(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) return true;
        return a.getItem() == b.getItem() && ItemStack.isSameItemSameTags(a, b);
    }

    public static BigInteger getPlayerEmc(Player player) {
        if (player instanceof ServerPlayer sp) {
            return sp.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY)
                    .map(IKnowledgeProvider::getEmc).orElse(BigInteger.ZERO);
        }
        try {
            return ProjectEAPI.getTransmutationProxy()
                    .getKnowledgeProviderFor(player.getUUID()).getEmc();
        } catch (Exception e) { return BigInteger.ZERO; }
    }

    public static void removePlayerEmc(Player player, BigInteger amount) {
        if (player instanceof ServerPlayer sp) {
            sp.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(p -> {
                p.setEmc(p.getEmc().subtract(amount).max(BigInteger.ZERO));
                p.syncEmc(sp);
            });
        }
    }

    public static void addPlayerEmc(Player player, BigInteger amount) {
        if (player instanceof ServerPlayer sp) {
            sp.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(p -> {
                p.setEmc(p.getEmc().add(amount));
                p.syncEmc(sp);
            });
        }
    }

    public static long getItemEmcValue(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        try { return ProjectEAPI.getEMCProxy().getValue(stack); }
        catch (Exception e) { return 0; }
    }

    public int getProgress() { return progress; }
    public int getProgressMax() { return progressMax; }
    public long getInputCount() { return inputCount; }
    public long getOutputCount() { return outputCount; }
    public ItemStack getInputType() { return inputType.copy(); }
    public ItemStack getOutputType() { return outputType.copy(); }
    public ResourceLocation getCurrentRecipeId() { return currentRecipeId; }
    public UUID getCraftingPlayerUUID() { return craftingPlayerUUID; }

    public void shrinkOutput(long amount) {
        this.outputCount -= amount;
        if (this.outputCount <= 0) {
            this.outputCount = 0;
            this.outputType = ItemStack.EMPTY;
        }
        setChanged();
    }

    public void shrinkInput(long amount) {
        this.inputCount -= amount;
        if (this.inputCount <= 0) {
            this.inputCount = 0;
            this.inputType = ItemStack.EMPTY;
        }
        setChanged();
    }

    public void clearOutput() {
        this.outputType = ItemStack.EMPTY;
        this.outputCount = 0;
        setChanged();
    }

    public void clearInput() {
        this.inputType = ItemStack.EMPTY;
        this.inputCount = 0;
        setChanged();
    }

    public void setCraftingPlayer(UUID uuid) {
        this.craftingPlayerUUID = uuid;
        setChanged();
    }

    public void saveToItemTag(CompoundTag tag) {
        if (!inputType.isEmpty() && inputCount > 0) {
            CompoundTag inputTag = new CompoundTag();
            inputType.save(inputTag);
            tag.put("inputType", inputTag);
            tag.putLong("inputCount", inputCount);
        }
        if (!outputType.isEmpty() && outputCount > 0) {
            CompoundTag outputTag = new CompoundTag();
            outputType.save(outputTag);
            tag.put("outputType", outputTag);
            tag.putLong("outputCount", outputCount);
        }
        if (craftingPlayerUUID != null) {
            tag.putUUID("craftingPlayer", craftingPlayerUUID);
        }
        tag.putInt("convertGear", convertGear);
        tag.putLong("customConvertAmount", customConvertAmount);
    }

    public MagicTableRecipe getCurrentRecipe() {
        if (cachedRecipe != null) return cachedRecipe;
        if (currentRecipeId != null && level != null) {
            cachedRecipe = findById(level, currentRecipeId.toString()).orElse(null);
        }
        return cachedRecipe;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);

        if (!inputType.isEmpty() && inputCount > 0) {
            CompoundTag inputTag = new CompoundTag();
            inputType.save(inputTag);
            tag.put("inputType", inputTag);
            tag.putLong("inputCount", inputCount);
        } else {
            tag.putLong("inputCount", 0);
        }

        if (!outputType.isEmpty() && outputCount > 0) {
            CompoundTag outputTag = new CompoundTag();
            outputType.save(outputTag);
            tag.put("outputType", outputTag);
            tag.putLong("outputCount", outputCount);
        } else {
            tag.putLong("outputCount", 0);
        }

        tag.putInt("progress", progress);
        tag.putInt("progressMax", progressMax);
        tag.putInt("convertGear", convertGear);
        tag.putLong("customConvertAmount", customConvertAmount);

        if (craftingPlayerUUID != null) tag.putUUID("craftingPlayer", craftingPlayerUUID);
        if (currentRecipeId != null) tag.putString("currentRecipeId", currentRecipeId.toString());
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);

        inputCount = tag.getLong("inputCount");
        if (inputCount > 0 && tag.contains("inputType")) {
            inputType = ItemStack.of(tag.getCompound("inputType"));
            inputType.setCount(1);
        } else {
            inputType = ItemStack.EMPTY;
            inputCount = 0;
        }

        outputCount = tag.getLong("outputCount");
        if (outputCount > 0 && tag.contains("outputType")) {
            outputType = ItemStack.of(tag.getCompound("outputType"));
            outputType.setCount(1);
        } else {
            outputType = ItemStack.EMPTY;
            outputCount = 0;
        }

        progress = tag.getInt("progress");
        progressMax = tag.getInt("progressMax");
        convertGear = tag.getInt("convertGear");
        customConvertAmount = tag.getLong("customConvertAmount");

        if (tag.hasUUID("craftingPlayer")) craftingPlayerUUID = tag.getUUID("craftingPlayer");
        if (tag.contains("currentRecipeId")) {
            currentRecipeId = new ResourceLocation(tag.getString("currentRecipeId"));
            cachedRecipe = null;
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return new TextComponent("");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        this.setCraftingPlayer(player.getUUID());
        return new MagicTableMenu(containerId, playerInventory, this);
    }
}