package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.NidavellirForgeRecipe;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.IManaSpark;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.SparkHelper;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.block.ModBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.Random;

public class NidavellirForgeTile extends BlockEntity implements ISparkAttachable, IManaReceiver, Container {
    private int mana;
    public int manaToGet;
    private NidavellirForgeRecipe currentRecipe;
    private int recipeID;
    public boolean requestUpdate = false;

    public static final int INPUT_STACK_LIMIT = 256;  // 输入槽堆叠上限：256个
    public static final int OUTPUT_STACK_LIMIT = 128; // 输出槽堆叠上限：128个

    public static final int INVENTORY_SIZE = 7; // 7（1个输出槽 + 6个输入槽）
    public static final int OUTPUT_SLOT = 0;
    public static final int FIRST_INPUT_SLOT = 1;

    private final LazyOptional<IManaReceiver> manaReceiverCap = LazyOptional.of(() -> this);
    private final LazyOptional<ISparkAttachable> sparkAttachableCap = LazyOptional.of(() -> this);
    private final ItemStackHandler inventory = createInventory();
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> inventory);

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> NidavellirForgeTile.this.mana;
                case 1 -> NidavellirForgeTile.this.manaToGet;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> NidavellirForgeTile.this.mana = value;
                case 1 -> NidavellirForgeTile.this.manaToGet = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public NidavellirForgeTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NIDAVELLIR_FORGE_TILE.get(), pos, state);
    }

    private ItemStackHandler createInventory() {
        return new ItemStackHandler(INVENTORY_SIZE) {
            @Override
            protected void onContentsChanged(int slot) {
                NidavellirForgeTile.this.onInventoryChanged(slot);
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return slot != OUTPUT_SLOT;
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == OUTPUT_SLOT ? OUTPUT_STACK_LIMIT : INPUT_STACK_LIMIT;
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (stack.isEmpty()) return ItemStack.EMPTY;
                if (!isItemValid(slot, stack)) return stack;

                ItemStack existing = this.stacks.get(slot);
                int limit = getSlotLimit(slot);

                if (!existing.isEmpty()) {
                    if (!NidavellirForgeTile.this.canStackTogether(stack, existing)) {
                        return stack;
                    }

                    int maxStackSize = Math.min(limit, existing.getMaxStackSize());
                    int availableSpace = maxStackSize - existing.getCount();

                    if (availableSpace <= 0) {
                        return stack;
                    }

                    int insertCount = Math.min(stack.getCount(), availableSpace);

                    if (!simulate) {
                        existing.grow(insertCount);
                        onContentsChanged(slot);
                    }

                    if (insertCount >= stack.getCount()) {
                        return ItemStack.EMPTY;
                    } else {
                        ItemStack remainder = stack.copy();
                        remainder.shrink(insertCount);
                        return remainder;
                    }
                } else {
                    int maxStackSize = Math.min(limit, stack.getMaxStackSize());
                    int insertCount = Math.min(stack.getCount(), maxStackSize);

                    if (!simulate) {
                        ItemStack newStack = stack.copy();
                        newStack.setCount(insertCount);
                        this.stacks.set(slot, newStack);
                        onContentsChanged(slot);
                    }

                    if (insertCount >= stack.getCount()) {
                        return ItemStack.EMPTY;
                    } else {
                        ItemStack remainder = stack.copy();
                        remainder.shrink(insertCount);
                        return remainder;
                    }
                }
            }
        };
    }

    private void onInventoryChanged(int slot) {
        setChanged();
        if (level != null && !level.isClientSide) {
            syncToClient();
        }
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            setChanged();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, NidavellirForgeTile tile) {
        tile.tick();
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientTick(Level level, BlockPos pos, BlockState state, NidavellirForgeTile tile) {
        tile.updateClient();
    }

    private void tick() {
        if (level == null || level.isClientSide) return;
        updateServer();
        IManaSpark spark = getAttachedSpark();
        if (spark != null && manaToGet > 0 && mana < manaToGet) {
            SparkHelper.getSparksAround(level, worldPosition.getX() + 0.5,
                            worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, spark.getNetwork())
                    .filter(otherSpark -> spark != otherSpark &&
                            otherSpark.getAttachedManaReceiver() instanceof IManaPool)
                    .forEach(os -> os.registerTransfer(spark));
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handler.invalidate();
        manaReceiverCap.invalidate();
        sparkAttachableCap.invalidate();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }

        if (cap == BotaniaForgeCapabilities.MANA_RECEIVER) {
            return manaReceiverCap.cast();
        }

        if (cap == BotaniaForgeCapabilities.SPARK_ATTACHABLE) {
            return sparkAttachableCap.cast();
        }

        return super.getCapability(cap, side);
    }

    private void updateServer() {
        boolean hasUpdate = false;

        AABB bounds = new AABB(worldPosition, worldPosition.offset(1, 1, 1));
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, bounds);

        for (ItemEntity item : items) {
            if (!item.isRemoved() && !item.getItem().isEmpty()) {
                ItemStack stack = item.getItem();
                int splitCount = addItemStack(stack);
                if (splitCount > 0) {
                    stack.shrink(splitCount);
                    if (stack.isEmpty()) {
                        item.discard();
                    }
                    hasUpdate = true;
                    break;
                }
            }
        }

        int wasManaToGet = manaToGet;
        boolean hasCraft = false;
        NidavellirForgeRecipe foundRecipe = null;

        if (level != null) {
            for (NidavellirForgeRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipes.NIDAVELLIR_FORGE_TYPE)) {
                if (recipe.matches(this, level)) {
                    foundRecipe = recipe;

                    ItemStack output = recipe.getResultItem();
                    ItemStack currentOutput = inventory.getStackInSlot(0);

                    boolean canOutput = currentOutput.isEmpty() ||
                            (ItemStack.isSameItemSameTags(output, currentOutput) &&
                                    currentOutput.getCount() + output.getCount() <= OUTPUT_STACK_LIMIT);

                    if (canOutput) {
                        manaToGet = recipe.getManaUsage();
                        currentRecipe = recipe;
                        hasCraft = true;

                        if (mana >= manaToGet && manaToGet > 0) {
                            performCrafting(recipe);
                            hasUpdate = true;
                        }
                        break;
                    }
                }
            }
        }

        if (!hasCraft) {
            currentRecipe = null;
            mana = 0;
            manaToGet = 0;
        }

        if (manaToGet != wasManaToGet || (currentRecipe != null && !hasUpdate)) {
            hasUpdate = true;
        }

        if (hasUpdate) {
            syncToClient();
        }
    }

    private void performCrafting(NidavellirForgeRecipe recipe) {
        mana -= recipe.getManaUsage();

        List<ItemStack> requiredInputs = recipe.getInputs();

        for (ItemStack requiredInput : requiredInputs) {
            int neededCount = requiredInput.getCount();

            for (int i = FIRST_INPUT_SLOT; i < inventory.getSlots() && neededCount > 0; i++) {
                ItemStack slotStack = inventory.getStackInSlot(i);
                if (!slotStack.isEmpty() &&
                        slotStack.getItem() == requiredInput.getItem() &&
                        (slotStack.getDamageValue() == requiredInput.getDamageValue() || requiredInput.getDamageValue() == 32767)) {

                    int consumeCount = Math.min(neededCount, slotStack.getCount());
                    slotStack.shrink(consumeCount);
                    neededCount -= consumeCount;

                    if (slotStack.isEmpty()) {
                        inventory.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            }
        }

        ItemStack output = recipe.getResultItem().copy();
        ItemStack currentOutput = inventory.getStackInSlot(OUTPUT_SLOT);
        if (currentOutput.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, output);
        } else {
            currentOutput.grow(output.getCount());
        }

        manaToGet = 0;
        currentRecipe = null;

        if (level != null) {
            level.playSound(null, worldPosition,
                    ModBlocks.terraPlate.getSoundType(getBlockState()).getPlaceSound(),
                    SoundSource.BLOCKS, 1.0F, 2.0F);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void updateClient() {
        if (mana > 0 && level != null) {
            Random rand = new Random(worldPosition.getX() ^ worldPosition.getY() ^ worldPosition.getZ());
            float indetY = (float) (Math.sin(ClientTickHandler.ticksInGame / 18.0F) / 24.0F);
            float ticks = 100.0F * getCurrentMana() / manaToGet;
            int totalSpiritCount = 3;
            double tickIncrement = 360.0F / totalSpiritCount;
            int speed = 5;
            double wticks = ticks * speed - tickIncrement;
            double r = Math.sin((ticks - 100.0F) / 10.0F) * 0.5F;
            double g = Math.sin(wticks * Math.PI / 180.0F * 0.55);
            float size = 0.4F;

            for (int i = 0; i < totalSpiritCount; i++) {
                double x = worldPosition.getX() + Math.sin(wticks * Math.PI / 180.0F) * r + 0.5F;
                double y = worldPosition.getY() - indetY + 0.85 + Math.abs(r) * 0.7;
                double z = worldPosition.getZ() + Math.cos(wticks * Math.PI / 180.0F) * r + 0.5F;
                wticks += tickIncrement;

                int color = currentRecipe != null ? currentRecipe.getColor() : 0x241E00;
                float[] hsb = Color.RGBtoHSB(color & 255, color >> 8 & 255, color >> 16 & 255, null);
                int color1 = Color.HSBtoRGB(hsb[0], hsb[1], ticks / 100.0F);
                float[] colorsfx = new float[]{
                        (color1 & 255) / 255.0F,
                        (color1 >> 8 & 255) / 255.0F,
                        (color1 >> 16 & 255) / 255.0F
                };

                WispParticleData data = WispParticleData.wisp(0.85F * size, colorsfx[0], colorsfx[1], colorsfx[2]);
                level.addParticle(data, x, y, z, (float) g * 0.05F, 0.0F, 0.25F);

                data = WispParticleData.wisp(
                        (float) Math.random() * 0.1F + 0.1F * size,
                        colorsfx[0], colorsfx[1], colorsfx[2], 0.9F);
                level.addParticle(data, x, y, z,
                        (float) (Math.random() - 0.5F) * 0.05F,
                        (float) (Math.random() - 0.5F) * 0.05F,
                        (float) (Math.random() - 0.5F) * 0.05F);

                if (ticks == 100.0F) {
                    for (int j = 0; j < 12; j++) {
                        data = WispParticleData.wisp(
                                (float) Math.random() * 0.15F + 0.15F * size,
                                colorsfx[0], colorsfx[1], colorsfx[2], 0.8F);
                        level.addParticle(data,
                                worldPosition.getX() + 0.5F,
                                worldPosition.getY() + 1.1 - indetY,
                                worldPosition.getZ() + 0.5F,
                                (float) (Math.random() - 0.5F) * 0.125F * size,
                                (float) (Math.random() - 0.5F) * 0.125F * size,
                                (float) (Math.random() - 0.5F) * 0.125F * size);
                    }
                }
            }
        }
    }

    private boolean canStackTogether(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty()) {
            return false;
        }

        if (stack1.getItem() != stack2.getItem()) {
            return false;
        }

        if (stack1.getDamageValue() != stack2.getDamageValue()) {
            return false;
        }

        CompoundTag nbt1 = stack1.getTag();
        CompoundTag nbt2 = stack2.getTag();

        if (nbt1 == null && nbt2 == null) {
            return true;
        }

        if (nbt1 == null || nbt2 == null) {
            return false;
        }

        return nbt1.equals(nbt2);
    }

    private int addItemStack(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        int totalAdded = 0;
        int remainingCount = stack.getCount();

        for (int i = FIRST_INPUT_SLOT; i < inventory.getSlots() && remainingCount > 0; i++) {
            ItemStack slotStack = inventory.getStackInSlot(i);

            if (!slotStack.isEmpty() && canStackTogether(stack, slotStack)) {
                int slotLimit = inventory.getSlotLimit(i);
                int currentCount = slotStack.getCount();
                int maxPossible = Math.min(slotLimit, slotStack.getMaxStackSize());
                int availableSpace = maxPossible - currentCount;

                if (availableSpace > 0) {
                    int insertCount = Math.min(remainingCount, availableSpace);
                    slotStack.grow(insertCount);
                    remainingCount -= insertCount;
                    totalAdded += insertCount;
                    onInventoryChanged(i);
                }
            }
        }

        for (int i = FIRST_INPUT_SLOT; i < inventory.getSlots() && remainingCount > 0; i++) {
            ItemStack slotStack = inventory.getStackInSlot(i);

            if (slotStack.isEmpty()) {
                int slotLimit = inventory.getSlotLimit(i);
                int maxPossible = Math.min(slotLimit, stack.getMaxStackSize());
                int insertCount = Math.min(remainingCount, maxPossible);

                ItemStack newStack = stack.copy();
                newStack.setCount(insertCount);
                inventory.setStackInSlot(i, newStack);

                remainingCount -= insertCount;
                totalAdded += insertCount;
                onInventoryChanged(i);
            }
        }

        return totalAdded;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        mana = tag.getInt("mana");
        manaToGet = tag.getInt("manaToGet");
        requestUpdate = tag.getBoolean("requestUpdate");

        String recipeIdString = tag.getString("currentRecipeId");
        if (!recipeIdString.isEmpty() && level != null) {
            ResourceLocation recipeId = new ResourceLocation(recipeIdString);
            currentRecipe = level.getRecipeManager().byKey(recipeId)
                    .filter(recipe -> recipe instanceof NidavellirForgeRecipe)
                    .map(recipe -> (NidavellirForgeRecipe) recipe)
                    .orElse(null);
        } else {
            currentRecipe = null;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("mana", mana);
        tag.putInt("manaToGet", manaToGet);
        tag.putBoolean("requestUpdate", requestUpdate);

        if (currentRecipe != null) {
            tag.putString("currentRecipeId", currentRecipe.getId().toString());
        } else {
            tag.putString("currentRecipeId", "");
        }
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
        setChanged();
        if (level != null && !level.isClientSide) {
            syncToClient();
        }
    }

    @Override
    public int getContainerSize() {
        return inventory.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == OUTPUT_SLOT) return false;

        for (int i = FIRST_INPUT_SLOT; i < inventory.getSlots(); i++) {
            ItemStack slotStack = inventory.getStackInSlot(i);
            int slotLimit = inventory.getSlotLimit(i);
            if (!slotStack.isEmpty() && slotStack.getCount() >= slotLimit
                    && ItemStack.matches(stack, slotStack)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = inventory.extractItem(slot, amount, false);
        if (!result.isEmpty()) {
            setChanged();
            if (level != null && !level.isClientSide) {
                syncToClient();
            }
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = inventory.getStackInSlot(slot);
        inventory.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.setStackInSlot(slot, stack);
        if (level != null && !level.isClientSide) {
            syncToClient();
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        load(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            handleUpdateTag(tag);
        }
    }

    @Override
    public Level getManaReceiverLevel() {
        return level;
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return worldPosition;
    }

    @Override
    public int getCurrentMana() {
        return mana;
    }

    @Override
    public boolean isFull() {
        return manaToGet == 0 || mana >= manaToGet;
    }

    @Override
    public void receiveMana(int mana) {
        if (manaToGet > 0) {
            this.mana = Math.min(this.mana + mana, manaToGet);
            setChanged();
            if (level != null && !level.isClientSide) {
                syncToClient();
            }
        }
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return manaToGet > 0 && mana < manaToGet;
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public void attachSpark(IManaSpark entity) {
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
        return isFull();
    }

    @Override
    public int getAvailableSpaceForMana() {
        if (manaToGet <= 0) return 0;
        return Math.max(0, manaToGet - getCurrentMana());
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }
}