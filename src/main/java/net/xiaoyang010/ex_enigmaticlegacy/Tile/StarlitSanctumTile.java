package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import com.google.common.base.Suppliers;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Container.StarlitSanctumMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.StarlitSanctumRecipe;
import net.xiaoyang010.ex_enigmaticlegacy.World.ritual.StructureParticleValidator;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.StarlitCraftingParticles;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.IManaSpark;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.common.block.ModBlocks;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.PatchouliAPI;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class StarlitSanctumTile extends RandomizableContainerBlockEntity implements WorldlyContainer, IManaReceiver, ISparkAttachable {
    public static final Supplier<IMultiblock> MULTIBLOCK = Suppliers.memoize(() -> PatchouliAPI.get().makeMultiblock(
            new String[][] {

                    {
                            "___________",
                            "_Y_______Y_",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "_Y_______Y_",
                            "___________",

                    },
                    {
                            "___________",
                            "_Q_______Q_",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "_Q_______Q_",
                            "___________",
                    },
                    {
                            "___________",
                            "_Q_______Q_",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "_Q_______Q_",
                            "___________",
                    },
                    {
                            "___________",
                            "_Q_______Q_",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "_Q_______Q_",
                            "___________",
                    },
                    {
                            "___________",
                            "_Q_______Q_",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "_Q_______Q_",
                            "___________",
                    },
                    {
                            "___________",
                            "_Q_______Q_",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "_Q_______Q_",
                            "___________",
                    },
                    {
                            "___________",
                            "_Q_______Q_",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "___________",
                            "_Q_______Q_",
                            "___________",

                    },
                    {
                            "_M_______M_",
                            "MAM_____MAM",
                            "_M_______M_",
                            "___________",
                            "___________",
                            "_____0_____",
                            "___________",
                            "___________",
                            "_M_______M_",
                            "MAM_____MAM",
                            "_M_______M_"
                    },
                    {
                            "___________",
                            "___LLLLL___",
                            "___________",
                            "_L_______L_",
                            "_L__TNT__L_",
                            "_L__NPN__L_",
                            "_L__TNT__L_",
                            "_L_______L_",
                            "___________",
                            "___LLLLL___",
                            "___________",

                    }
            },
            '0', ModBlockss.STARLIT_SANCTUM.get(),
            'P', ModBlockss.PRISMATICRADIANCEBLOCK.get(),
            'N', ModBlockss.BLOCKNATURE.get(),
            'T', ModBlocks.terrasteelBlock,
            'L', Blocks.OAK_LEAVES,
            'A', ModBlockss.ASTRAL_BLOCK.get(),
            'M', ModBlockss.MANA_BOX.get(),
            'Y', ModBlocks.naturaPylon,
            'Q', ModBlockss.LEBETHRON_CORE.get())
    );

    private static final int TOTAL_SLOTS = 489;
    private static final int MAIN_GRID_SLOTS = 486;
    private static final int INPUT_LEFT_SLOT = 486;
    private static final int INPUT_RIGHT_SLOT = 487;
    private static final int OUTPUT_SLOT = 488;

    private static final long MAX_MANA = Long.MAX_VALUE;
    private long currentMana = 0L;
    private int craftingProgress = 0;
    private int maxCraftingTime = 200;
    private boolean manaChanged = false;
    private int tickCounter = 0;
    private StarlitSanctumRecipe currentRecipe = null;

    private boolean isCrafting = false;
    private int craftingTick = 0;

    private boolean structureValid = false;
    private int structureCheckCooldown = 0;
    private static final int STRUCTURE_CHECK_INTERVAL = 100;
    private String lastStructureError = "";

    public static final TagKey<Item> STARLIT = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("ex_enigmaticlegacy", "starlit"));
    private final LazyOptional<IManaReceiver> manaReceiverCap = LazyOptional.of(() -> this);
    private final LazyOptional<ISparkAttachable> sparkAttachableCap = LazyOptional.of(() -> this);
    private NonNullList<ItemStack> stacks = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);
    private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new InvWrapper(this));

    public StarlitSanctumTile(BlockPos position, BlockState state) {
        super(ModBlockEntities.STARLIT_SANCTUM_OF_MYSTIQUE.get(), position, state);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.currentMana = compound.getLong("CurrentMana");
        this.craftingProgress = compound.getInt("CraftingProgress");
        this.maxCraftingTime = compound.getInt("MaxCraftingTime");
        this.structureValid = compound.getBoolean("StructureValid");
        this.lastStructureError = compound.getString("LastStructureError");
        this.isCrafting = compound.getBoolean("IsCrafting");
        this.craftingTick = compound.getInt("CraftingTick");
        if (!this.tryLoadLootTable(compound)) {
            this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
            if (compound.contains("Items", 9)) {
                loadAllItemsWithIntSlot(compound, this.stacks);
            }
        }
        if (level != null && !level.isClientSide) {
            System.out.println("[StarlitSanctum] Loaded: Mana=" + formatMana(currentMana) +
                    ", Items=" + countNonEmptySlots() +
                    ", Progress=" + craftingProgress +
                    ", Structure=" + (structureValid ? "Valid" : "Invalid"));
        }
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putLong("CurrentMana", this.currentMana);
        compound.putInt("CraftingProgress", this.craftingProgress);
        compound.putInt("MaxCraftingTime", this.maxCraftingTime);
        compound.putBoolean("StructureValid", this.structureValid);
        compound.putString("LastStructureError", this.lastStructureError);
        compound.putBoolean("IsCrafting", this.isCrafting);
        compound.putInt("CraftingTick", this.craftingTick);
        if (!this.trySaveLootTable(compound)) {
            saveAllItemsWithIntSlot(compound, this.stacks);
        }
    }

    private static void saveAllItemsWithIntSlot(CompoundTag compound, NonNullList<ItemStack> items) {
        ListTag listTag = new ListTag();
        for (int i = 0; i < items.size(); i++) {
            ItemStack itemStack = items.get(i);
            if (!itemStack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                itemStack.save(itemTag);
                listTag.add(itemTag);
            }
        }
        compound.put("Items", listTag);
    }

    private static void loadAllItemsWithIntSlot(CompoundTag compound, NonNullList<ItemStack> items) {
        ListTag listTag = compound.getList("Items", 10);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag itemTag = listTag.getCompound(i);
            int slot = itemTag.getInt("Slot");
            if (slot >= 0 && slot < items.size()) {
                items.set(slot, ItemStack.of(itemTag));
            }
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
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

        if (index == INPUT_LEFT_SLOT) {
            return stack.isEmpty() || stack.is(STARLIT);
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
        if (!this.remove && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == null) {
                return itemHandler.cast();
            }
            return handlers[facing.ordinal()].cast();
        }

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
        itemHandler.invalidate();
        manaReceiverCap.invalidate();
        sparkAttachableCap.invalidate();
    }

    @Override
    public int getCurrentMana() {
        return (int) Math.min(currentMana, Integer.MAX_VALUE);
    }

    public long getCurrentManaLong() {
        return currentMana;
    }

    @Override
    public boolean isFull() {
        return currentMana >= MAX_MANA;
    }

    @Override
    public void receiveMana(int mana) {
        if (mana <= 0) return;

        long oldMana = this.currentMana;

        try {
            this.currentMana = Math.addExact(this.currentMana, (long) mana);
            this.currentMana = Math.min(this.currentMana, MAX_MANA);
        } catch (ArithmeticException e) {
            this.currentMana = MAX_MANA;
        }

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
        long space = MAX_MANA - currentMana;
        return (int) Math.min(space, Integer.MAX_VALUE);
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

    public long getMaxMana() {
        return MAX_MANA;
    }

    public boolean consumeMana(long amount) {
        if (amount < 0) {
            return false;
        }

        if (currentMana >= amount) {
            try {
                this.currentMana = Math.subtractExact(this.currentMana, amount);
            } catch (ArithmeticException e) {
                this.currentMana = 0;
            }

            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            return true;
        }
        return false;
    }


    public boolean hasValidPlatform() {
        if (level == null) return false;
        return MULTIBLOCK.get().validate(level, getBlockPos()) != null;
    }

    private void checkStructureWithParticles() {
        if (level == null || level.isClientSide) return;

        boolean wasValid = this.structureValid;

        this.structureValid = hasValidPlatform();

        if (wasValid != this.structureValid) {
            if (this.structureValid) {
                System.out.println("[StarlitSanctum] Structure validation SUCCESS!");
                this.lastStructureError = "";

                StructureParticleValidator.spawnCompletionParticles(level, worldPosition);
            } else {
                System.out.println("[StarlitSanctum] Structure validation FAILED!");
                this.lastStructureError = "Ritual structure incomplete or incorrect";

                StructureParticleValidator.showStructureOutline(level, worldPosition);
            }

            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void showStructureOutline() {
        if (level != null && !level.isClientSide) {
            StructureParticleValidator.showStructureOutline(level, worldPosition);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, StarlitSanctumTile tile) {
        if (level.isClientSide) return;

        tile.tickCounter++;

        tile.structureCheckCooldown--;
        if (tile.structureCheckCooldown <= 0) {
            tile.checkStructureWithParticles();
            tile.structureCheckCooldown = STRUCTURE_CHECK_INTERVAL;
        }

        if (!tile.structureValid && tile.tickCounter % 20 == 0) {
            tile.showStructureOutline();
        }

        if (tile.manaChanged && tile.tickCounter % 20 == 0) {
            tile.syncManaToClient();
            tile.manaChanged = false;
        }

        if (level.getDayTime() % 10 != 0) return;

        if (!tile.structureValid) {
            if (tile.tickCounter % 100 == 0) {
                System.out.println("[StarlitSanctum] Cannot craft: structure invalid");
            }
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;

        if (tile.tickCounter % 2 == 0) {
            StarlitCraftingParticles.spawnArenaRingParticles(serverLevel, pos, tile.tickCounter);
        }

        if (tile.tickCounter % 3 == 0) {
            StarlitCraftingParticles.spawnPylonEnergyParticles(serverLevel, pos, tile.tickCounter);
        }
        Optional<StarlitSanctumRecipe> recipeOpt = level.getRecipeManager()
                .getRecipeFor(ModRecipes.STARLIT_TYPE, tile, level);
        if (recipeOpt.isPresent()) {
            StarlitSanctumRecipe recipe = recipeOpt.get();
            ItemStack resultItem = recipe.getResultItem();
            long requiredMana = recipe.getManaCost();
            if (tile.currentMana < requiredMana) {
                if (tile.isCrafting) {
                    tile.isCrafting = false;
                    tile.craftingTick = 0;
                }
                if (tile.tickCounter % 100 == 0) {
                    System.out.println("[StarlitSanctum] Insufficient mana: " +
                            formatMana(tile.currentMana) + " / " + formatMana(requiredMana));
                }
                return;
            }
            ItemStack output = tile.getItem(OUTPUT_SLOT);
            boolean canOutput = output.isEmpty() ||
                    (resultItem.getItem() == output.getItem() &&
                            output.getCount() + resultItem.getCount() <= output.getMaxStackSize());
            if (!canOutput) {
                if (tile.isCrafting) {
                    tile.isCrafting = false;
                    tile.craftingTick = 0;
                }
                return;
            }
            if (!tile.isCrafting) {
                tile.isCrafting = true;
                tile.craftingTick = 0;
                tile.maxCraftingTime = 200;
                System.out.println("[StarlitSanctum] Starting crafting: " + resultItem.getDisplayName().getString());
            }
            if (tile.isCrafting) {
                tile.craftingTick++;
                float progress = tile.craftingTick / (float) tile.maxCraftingTime;
                StarlitCraftingParticles.spawnAllCraftingParticles(
                        serverLevel,
                        pos,
                        tile.craftingTick,
                        progress
                );
                if (tile.craftingTick % 5 == 0) {
                    StarlitCraftingParticles.spawnPillarToCenterParticles(serverLevel, pos, tile.craftingTick);
                }
                if (tile.craftingTick % 10 == 0) {
                    StarlitCraftingParticles.spawnHexagramParticles(serverLevel, pos, tile.craftingTick);
                }
                if (tile.craftingTick >= tile.maxCraftingTime) {
                    StarlitCraftingParticles.spawnCompletionBurst(serverLevel, pos);
                    if (output.isEmpty()) {
                        tile.setItem(OUTPUT_SLOT, resultItem.copy());
                    } else {
                        output.grow(resultItem.getCount());
                    }
                    tile.consumeIngredients(recipe);
                    tile.consumeMana(requiredMana);
                    tile.isCrafting = false;
                    tile.craftingTick = 0;
                    System.out.println("[StarlitSanctum] Crafting completed: " +
                            resultItem.getDisplayName().getString() +
                            " (Mana used: " + formatMana(requiredMana) + ")");
                }
            }
        } else {

            if (tile.isCrafting) {
                tile.isCrafting = false;
                tile.craftingTick = 0;
            }
        }
    }

    private void consumeIngredients(StarlitSanctumRecipe recipe) {
        ItemStack leftStack = getItem(INPUT_LEFT_SLOT);
        leftStack.shrink(recipe.getLeftInputCount());
        setItem(INPUT_LEFT_SLOT, leftStack);

        ItemStack rightStack = getItem(INPUT_RIGHT_SLOT);
        rightStack.shrink(recipe.getRightInputCount());
        setItem(INPUT_RIGHT_SLOT, rightStack);

        consumePatternMaterials(recipe);

        setChanged();
    }

    private void consumePatternMaterials(StarlitSanctumRecipe recipe) {
        List<NonNullList<Ingredient>> patterns = recipe.getPatternGroups();
        int[] blockStarts = {0, 9, 18};

        for (int blockIndex = 0; blockIndex < 3; blockIndex++) {
            NonNullList<Ingredient> pattern = patterns.get(blockIndex);
            int startCol = blockStarts[blockIndex];

            for (int row = 0; row < 18; row++) {
                for (int col = 0; col < 9; col++) {
                    int patternIndex = row * 9 + col;
                    int slotIndex = row * 27 + (startCol + col);

                    Ingredient required = pattern.get(patternIndex);
                    if (required != Ingredient.EMPTY) {
                        ItemStack stack = getItem(slotIndex);
                        stack.shrink(1);
                        setItem(slotIndex, stack);
                    }
                }
            }
        }
    }

    private void syncManaToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public int getCraftingProgressPercent() {
        if (maxCraftingTime == 0) return 0;
        return (craftingProgress * 100) / maxCraftingTime;
    }

    private int countNonEmptySlots() {
        int count = 0;
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public static String formatMana(long mana) {
        if (mana >= 1_000_000_000_000L) {
            return String.format("%.2fT", mana / 1_000_000_000_000.0);
        } else if (mana >= 1_000_000_000L) {
            return String.format("%.2fB", mana / 1_000_000_000.0);
        } else if (mana >= 1_000_000L) {
            return String.format("%.2fM", mana / 1_000_000.0);
        } else if (mana >= 1_000L) {
            return String.format("%.2fK", mana / 1_000.0);
        }
        return String.valueOf(mana);
    }
}