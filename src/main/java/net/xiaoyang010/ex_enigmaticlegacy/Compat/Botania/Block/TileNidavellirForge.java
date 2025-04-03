package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.RecipeAdvancedPlate;
import net.xiaoyang010.ex_enigmaticlegacy.api.AdvancedBotanyAPI;
import vazkii.botania.api.item.ISparkEntity;
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

public class TileNidavellirForge extends BlockEntity implements ISparkAttachable, IManaReceiver {
    private int mana;
    public int manaToGet;
    private RecipeAdvancedPlate currentRecipe;
    private int recipeID;
    public boolean requestUpdate = false;

    private final ItemStackHandler inventory = createInventory();
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> inventory);

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> TileNidavellirForge.this.mana;
                case 1 -> TileNidavellirForge.this.manaToGet;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> TileNidavellirForge.this.mana = value;
                case 1 -> TileNidavellirForge.this.manaToGet = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public TileNidavellirForge(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NIDAVELLIR_FORGE_TILE.get(), pos, state);
    }

    private ItemStackHandler createInventory() {
        return new ItemStackHandler(4) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (level != null) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                }
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (slot == 0) return false;
                for (int i = 1; i < getSlots(); i++) {
                    ItemStack slotStack = getStackInSlot(i);
                    if (!slotStack.isEmpty() && slotStack.getCount() == slotStack.getMaxStackSize()
                            && ItemStack.matches(stack, slotStack)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }
        return super.getCapability(cap, side);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileNidavellirForge tile) {
        tile.tick();
    }

    private void tick() {
        if (level == null || level.isClientSide) return;

        updateServer();

        IManaSpark spark = getAttachedSpark();
        if (spark != null) {
            for (IManaSpark otherSpark : SparkHelper.getSparksAround(level, worldPosition.getX() + 0.5,
                    worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5)) {
                if (spark != otherSpark && otherSpark.getAttachedTile() != null
                        && otherSpark.getAttachedTile() instanceof IManaPool) {
                }
            }
        }
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
        int recipeId = 0;

        for (RecipeAdvancedPlate recipe : AdvancedBotanyAPI.advancedPlateRecipes) {
            if (recipe.matches((Container) this)) {
                this.recipeID = recipeId;
                if (mana > 0 && isFull()) {
                    // 执行合成
                    ItemStack output = recipe.getOutput().copy();
                    receiveMana(-recipe.getManaUsage());
                    manaToGet = 0;

                    // 消耗输入物品
                    for (int i = 1; i < inventory.getSlots(); i++) {
                        ItemStack slotStack = inventory.getStackInSlot(i);
                        if (!slotStack.isEmpty()) {
                            if (slotStack.getCount() > 1) {
                                slotStack.shrink(1);
                            } else {
                                inventory.setStackInSlot(i, ItemStack.EMPTY);
                            }
                        }
                    }

                    ItemStack currentOutput = inventory.getStackInSlot(0);
                    if (!currentOutput.isEmpty()) {
                        currentOutput.grow(1);
                    } else {
                        inventory.setStackInSlot(0, output);
                    }

                    hasUpdate = true;
                    level.playSound(null, worldPosition,
                            ModBlocks.terraPlate.getSoundType(getBlockState()).getPlaceSound(),
                            net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 2.0F);
                    break;
                }

                ItemStack slot0 = inventory.getStackInSlot(0);
                if (slot0.isEmpty() || (ItemStack.matches(recipe.getOutput(), slot0)
                        && slot0.getCount() < slot0.getMaxStackSize())) {
                    manaToGet = recipe.getManaUsage();
                    currentRecipe = recipe;
                    hasCraft = true;
                    break;
                }
            }
            recipeId++;
        }

        if (!hasCraft) {
            currentRecipe = null;
            mana = 0;
            manaToGet = 0;
        }

        if (manaToGet != wasManaToGet) {
            hasUpdate = true;
        }

        if (hasUpdate) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
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

    private int addItemStack(ItemStack stack) {
        for (int i = 1; i < inventory.getSlots(); i++) {
            ItemStack slotStack = inventory.getStackInSlot(i);
            if (slotStack.isEmpty()) {
                ItemStack copy = stack.copy();
                copy.setCount(Math.min(stack.getCount(), stack.getMaxStackSize()));
                inventory.setStackInSlot(i, copy);
                return copy.getCount();
            }

            if (ItemStack.matches(stack, slotStack) && slotStack.getCount() < slotStack.getMaxStackSize()) {
                int count = Math.min(stack.getCount(), slotStack.getMaxStackSize() - slotStack.getCount());
                slotStack.grow(count);
                return count;
            }
        }
        return 0;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        mana = tag.getInt("mana");
        manaToGet = tag.getInt("manaToGet");
        requestUpdate = tag.getBoolean("requestUpdate");

        recipeID = tag.getInt("recipeID");
        if (recipeID == -1) {
            currentRecipe = null;
        } else if (recipeID < AdvancedBotanyAPI.advancedPlateRecipes.size()) {
            currentRecipe = AdvancedBotanyAPI.advancedPlateRecipes.get(recipeID);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("mana", mana);
        tag.putInt("manaToGet", manaToGet);
        tag.putBoolean("requestUpdate", requestUpdate);
        tag.putInt("recipeID", currentRecipe == null ? -1 : recipeID);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
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
        return mana >= manaToGet;
    }

    @Override
    public void receiveMana(int mana) {
        this.mana = Math.min(this.mana + mana, manaToGet);
        setChanged();
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return !isFull();
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
            if (entity instanceof IManaSpark spark) {
                return spark;
            }
        }
        return null;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return isFull();
    }

    @Override
    public int getAvailableSpaceForMana() {
        return Math.max(0, manaToGet - getCurrentMana());
    }

    public ItemStack getItem(int slot) {
        return inventory.getStackInSlot(slot);
    }

    public void setItem(int slot, ItemStack stack) {
        inventory.setStackInSlot(slot, stack);
    }

    public int getContainerSize() {
        return inventory.getSlots();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handler.invalidate();
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public boolean hasValidRecipe() {
        return currentRecipe != null;
    }

    public RecipeAdvancedPlate getCurrentRecipe() {
        return currentRecipe;
    }

    public float getManaFillPercentage() {
        return manaToGet > 0 ? (float) mana / manaToGet : 0;
    }
}