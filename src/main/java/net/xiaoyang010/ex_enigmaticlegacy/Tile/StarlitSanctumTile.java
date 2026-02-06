package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Container.StarlitSanctumMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.StarlitSanctumRecipe;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.IManaSpark;
import vazkii.botania.api.mana.spark.ISparkAttachable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class StarlitSanctumTile extends RandomizableContainerBlockEntity implements WorldlyContainer, IManaReceiver, ISparkAttachable  {
    private static final int TOTAL_SLOTS = 489;
    private static final int MAIN_GRID_SLOTS = 486;
    private static final int INPUT_LEFT_SLOT = 486;
    private static final int OUTPUT_SLOT = 487;
    private static final int INPUT_RIGHT_SLOT = 488;
    private static final int MAX_MANA = 100000;
    private int currentMana = 0;
    private int craftingProgress = 0;
    private int maxCraftingTime = 200;
    private boolean manaChanged = false;
    private int tickCounter = 0;
    private StarlitSanctumRecipe currentRecipe = null;
    private final LazyOptional<IManaReceiver> manaReceiverCap = LazyOptional.of(() -> this);
    private final LazyOptional<ISparkAttachable> sparkAttachableCap = LazyOptional.of(() -> this);
    private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(TOTAL_SLOTS, ItemStack.EMPTY);
    private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());

    public StarlitSanctumTile(BlockPos position, BlockState state) {
        super(ModBlockEntities.STARLIT_SANCTUM_OF_MYSTIQUE.get(), position, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, StarlitSanctumTile tile) {
        if (level.isClientSide) return;
        tile.tickCounter++;

        if (tile.manaChanged && tile.tickCounter % 20 == 0) {
            tile.syncManaToClient();
            tile.manaChanged = false;
        }

        if (tile.currentRecipe == null) {
            tile.tryStartCrafting();
        }

        if (tile.currentRecipe != null) {
            tile.processCrafting();
        }

    }

    private void syncManaToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void tryStartCrafting() {
        if (!getOutput().isEmpty()) {
            return;
        }

        Optional<StarlitSanctumRecipe> recipe = findMatchingRecipe();
        if (recipe.isEmpty()) {
            return;
        }

        StarlitSanctumRecipe foundRecipe = recipe.get();
        if (currentMana < foundRecipe.getManaCost()) {
            return;
        }

        this.currentRecipe = foundRecipe;
        this.craftingProgress = 0;
        setChanged();
    }

    private void processCrafting() {
        if (currentRecipe == null) return;
        craftingProgress++;
        if (craftingProgress >= maxCraftingTime) {
            finishCrafting();
            return;
        }
        if (craftingProgress % 10 == 0) {
            int manaPerTick = currentRecipe.getManaCost() / (maxCraftingTime / 10);
            if (!consumeMana(manaPerTick)) {
                cancelCrafting();
                return;
            }
        }
        setChanged();
    }

    private void finishCrafting() {
        if (currentRecipe == null) return;
        ItemStack leftStack = getItem(INPUT_LEFT_SLOT);
        leftStack.shrink(currentRecipe.getLeftInputCount());
        setItem(INPUT_LEFT_SLOT, leftStack);
        ItemStack rightStack = getItem(INPUT_RIGHT_SLOT);
        rightStack.shrink(currentRecipe.getRightInputCount());
        setItem(INPUT_RIGHT_SLOT, rightStack);
        consumePatternMaterials();
        setOutput(currentRecipe.getResultItem().copy());
        this.currentRecipe = null;
        this.craftingProgress = 0;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void cancelCrafting() {
        this.currentRecipe = null;
        this.craftingProgress = 0;
        setChanged();
    }

    private void consumePatternMaterials() {
        if (currentRecipe == null) return;
        List<NonNullList<net.minecraft.world.item.crafting.Ingredient>> patterns = currentRecipe.getPatternGroups();
        int[] blockStarts = {0, 9, 18};
        for (int blockIndex = 0; blockIndex < 3; blockIndex++) {
            NonNullList<net.minecraft.world.item.crafting.Ingredient> pattern = patterns.get(blockIndex);
            int startCol = blockStarts[blockIndex];
            for (int row = 0; row < 18; row++) {
                for (int col = 0; col < 9; col++) {
                    int patternIndex = row * 9 + col;
                    int slotIndex = row * 27 + (startCol + col);
                    net.minecraft.world.item.crafting.Ingredient required = pattern.get(patternIndex);
                    if (required != net.minecraft.world.item.crafting.Ingredient.EMPTY) {
                        ItemStack stack = getItem(slotIndex);
                        stack.shrink(1);
                        setItem(slotIndex, stack);
                    }
                }
            }
        }
    }

    private Optional<StarlitSanctumRecipe> findMatchingRecipe() {
        if (level == null) return Optional.empty();
        SimpleContainer tempContainer = new SimpleContainer(TOTAL_SLOTS);
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            tempContainer.setItem(i, getItem(i).copy());
        }
        return level.getRecipeManager()
                .getAllRecipesFor(StarlitSanctumRecipe.Type.INSTANCE)
                .stream()
                .filter(recipe -> recipe.matches(tempContainer, level))
                .findFirst();
    }

    public int getCraftingProgressPercent() {
        if (maxCraftingTime == 0) return 0;
        return (craftingProgress * 100) / maxCraftingTime;
    }

    @Override
    public int getCurrentMana() {
        return currentMana;
    }

    @Override
    public boolean isFull() {
        return currentMana >= MAX_MANA;
    }

    @Override
    public void receiveMana(int mana) {
        int oldMana = this.currentMana;
        this.currentMana = Math.min(this.currentMana + mana, MAX_MANA);
        if (oldMana != this.currentMana) {
            this.manaChanged = true;
            setChanged();
        }
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }

    @Override
    public Level getManaReceiverLevel() {
        return this.level;
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return this.worldPosition;
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public void attachSpark(IManaSpark spark) {
    }

    @Override
    public int getAvailableSpaceForMana() {
        return Math.max(0, MAX_MANA - currentMana);
    }

    @Override
    public IManaSpark getAttachedSpark() {
        if (level == null) return null;

        List<Entity> sparks = level.getEntitiesOfClass(Entity.class,
                new AABB(worldPosition.above(), worldPosition.above().offset(1, 1, 1)),
                entity -> entity instanceof IManaSpark);

        if (!sparks.isEmpty()) {
            Entity entity = sparks.get(0);
            return (IManaSpark) entity;
        }
        return null;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return false;
    }

    public int getMaxMana() {
        return MAX_MANA;
    }

    public boolean consumeMana(int amount) {
        if (currentMana >= amount) {
            currentMana -= amount;
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            return true;
        }
        return false;
    }

    @Override
    public void load(CompoundTag compound) {
        this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(compound)) {
            ContainerHelper.loadAllItems(compound, this.stacks);
        }

        this.currentMana = compound.getInt("Mana");
        this.craftingProgress = compound.getInt("CraftingProgress");
        this.maxCraftingTime = compound.getInt("MaxCraftingTime");

        this.currentRecipe = null;
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        if (!this.trySaveLootTable(compound)) {
            ContainerHelper.saveAllItems(compound, this.stacks);
        }

        compound.putInt("Mana", currentMana);
        compound.putInt("CraftingProgress", craftingProgress);
        compound.putInt("MaxCraftingTime", maxCraftingTime);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
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

    public void setOutput(ItemStack stack) {
        this.setItem(OUTPUT_SLOT, stack);
    }

    public ItemStack getOutput() {
        return this.getItem(OUTPUT_SLOT);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (!this.remove && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return handlers[facing.ordinal()].cast();

        if (capability == BotaniaForgeCapabilities.MANA_RECEIVER) {
            return manaReceiverCap.cast();
        }
        if (capability == BotaniaForgeCapabilities.SPARK_ATTACHABLE) {
            return sparkAttachableCap.cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        for (LazyOptional<? extends IItemHandler> handler : handlers)
            handler.invalidate();
    }
}