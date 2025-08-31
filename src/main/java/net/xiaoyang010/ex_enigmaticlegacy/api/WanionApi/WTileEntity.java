package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IController;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class WTileEntity extends BlockEntity implements WorldlyContainer {
    private final Dependencies<IController<?, ?>> controllerHandler = new Dependencies<>();
    private final Collection<IController<?, ?>> controllers = controllerHandler.getInstances();
    private final Map<Capability<?>, LazyOptional<?>> capabilitiesMap = new HashMap<>();
    private String customName = null;
    protected final NonNullList<ItemStack> itemStacks = getItemStacks();

    public WTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        controllerHandler.subscribe(ControlController.class, () -> new ControlController(this));
        controllerHandler.subscribe(FieldController.class, () -> new FieldController(this));
        controllerHandler.subscribe(MatchingController.class, () -> new MatchingController(this));
    }

    protected NonNullList<ItemStack> getItemStacks() {
        return NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
    }

    public <C> void addCapability(@NotNull final Capability<C> capability, C cObj) {
        capabilitiesMap.put(capability, LazyOptional.of(() -> cObj));
    }

    @NotNull
    public abstract String getDefaultName();

    public final <A extends IController<?, ?>> A getController(@NotNull final Class<A> aClass) {
        return controllerHandler.get(aClass);
    }

    public final Collection<IController<?, ?>> getControllers() {
        return controllers;
    }

    public final <A extends IController<?, ?>> boolean hasController(@NotNull final Class<A> aClass) {
        return controllerHandler.contains(aClass);
    }

    @Override
    public final void load(@NotNull final CompoundTag compoundTag) {
        super.load(compoundTag);
        readCustomNBT(compoundTag);
    }

    public void readCustomNBT(@NotNull final CompoundTag compoundTag) {
        final CompoundTag displayTag = compoundTag.getCompound("display");
        if (displayTag.contains("Name"))
            this.customName = displayTag.getString("Name"); // 修正：直接存储String
        final ListTag listTag = compoundTag.getList("Contents", 10);
        for (int i = 0; i < listTag.size(); i++) {
            final CompoundTag slotCompound = listTag.getCompound(i);
            final int slot = slotCompound.getShort("Slot");
            if (slot >= 0 && slot < getContainerSize())
                setItem(slot, ItemStack.of(slotCompound));
        }
        controllers.forEach(controller -> controller.readNBT(compoundTag));
    }

    @Override
    public final void saveAdditional(@NotNull final CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        writeCustomNBT(compoundTag);
    }

    public CompoundTag writeCustomNBT(@NotNull final CompoundTag compoundTag) {
        if (customName != null) {
            final CompoundTag nameNBT = new CompoundTag();
            nameNBT.putString("Name", customName);
            compoundTag.put("display", nameNBT);
        }
        final ListTag listTag = new ListTag();
        final int max = Mth.clamp(getContainerSize(), 0, itemStacks.size());
        for (int i = 0; i < max; i++) {
            final ItemStack itemStack = getItem(i);
            if (itemStack.isEmpty())
                continue;
            final CompoundTag slotCompound = new CompoundTag();
            slotCompound.putShort("Slot", (short) i);
            itemStack.save(slotCompound);
            listTag.add(slotCompound);
        }
        controllers.forEach(controller -> compoundTag.merge(controller.writeNBT()));
        controllers.forEach(controller -> controller.afterWriteNBT(compoundTag));
        if (!listTag.isEmpty())
            compoundTag.put("Contents", listTag);
        return compoundTag;
    }

    @NotNull
    public Component getDisplayName() {
        return new TranslatableComponent(getName());
    }

    @NotNull
    public final String getName() {
        return hasCustomName() ? customName : getDefaultName();
    }

    @NotNull
    @Override
    public int[] getSlotsForFace(@NotNull final Direction side) {
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(final int index, @NotNull final ItemStack itemStackIn, @Nullable final Direction direction) {
        return canPlaceItem(index, itemStackIn);
    }

    @Override
    public boolean canTakeItemThroughFace(final int index, @NotNull final ItemStack stack, @NotNull final Direction direction) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return itemStacks.stream().allMatch(ItemStack::isEmpty);
    }

    @NotNull
    @Override
    public ItemStack getItem(final int index) {
        return itemStacks.get(index);
    }

    @NotNull
    @Override
    public ItemStack removeItem(final int index, final int count) {
        final ItemStack slotStack = itemStacks.get(index);
        if (slotStack.isEmpty())
            return ItemStack.EMPTY;
        final ItemStack result = slotStack.copy();
        result.setCount(Math.min(count, slotStack.getCount()));
        slotStack.shrink(result.getCount());
        if (slotStack.isEmpty())
            itemStacks.set(index, ItemStack.EMPTY);
        setChanged();
        return result;
    }

    @NotNull
    @Override
    public ItemStack removeItemNoUpdate(final int index) {
        final ItemStack itemStack = itemStacks.get(index);
        itemStacks.set(index, ItemStack.EMPTY);
        return itemStack;
    }

    @Override
    public void setItem(final int index, @NotNull final ItemStack stack) {
        itemStacks.set(index, stack);
        setChanged();
    }

    @Override
    public int getMaxStackSize()
    {
        return 64;
    }

    @Override
    public final boolean stillValid(@NotNull final Player player) {
        return level.getBlockEntity(worldPosition) == this &&
                player.distanceToSqr((double) worldPosition.getX() + 0.5D,
                        (double) worldPosition.getY() + 0.5D,
                        (double) worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void startOpen(@NotNull final Player player) {}

    @Override
    public void stopOpen(@NotNull final Player player) {}

    @Override
    public boolean canPlaceItem(final int index, @NotNull final ItemStack stack)
    {
        return true;
    }

    @Override
    public final void clearContent()
    {
        itemStacks.clear();
    }

    public final boolean hasCustomName()
    {
        return customName != null;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull final Capability<T> capability, @Nullable final Direction facing) {
        if (capabilitiesMap.containsKey(capability)) {
            @SuppressWarnings("unchecked")
            LazyOptional<T> result = (LazyOptional<T>) capabilitiesMap.get(capability);
            return result;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        capabilitiesMap.values().forEach(LazyOptional::invalidate);
    }
}