package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import morph.avaritia.api.ExtremeCraftingRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Block.BlockExtremeAutoCrafter;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ContainerExtremeAutoCrafter;

import java.time.chrono.IsoChronology;

public final class TileEntityExtremeAutoCrafter extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(164, ItemStack.EMPTY);
    private boolean isPowered;
    private String recipe;

    public TileEntityExtremeAutoCrafter(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
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

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("items", ContainerHelper.saveAllItems(tag, items));
        tag.putBoolean("isPowered", isPowered);
        tag.putString("recipe", recipe);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        isPowered = tag.getBoolean("isPowered");
        recipe = tag.getString("recipe");
    }

    @Override
    protected Component getDefaultName() {
        return Component.nullToEmpty("梦魇工作台");
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new ContainerExtremeAutoCrafter(i, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return 164;
    }

    @Override
    public boolean isEmpty() {
        return items.subList(0, 81).isEmpty();
    }

    @Override
    public ItemStack getItem(int i) {
        return items.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int i1) {
        return ContainerHelper.removeItem(items, i, i1);
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ContainerHelper.takeItem(items, i);
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        if ((i > 0 && i < 81) || i == 163)
            items.set(i, itemStack);
    }

    @Override
    public boolean stillValid(Player player) {
        BlockPos pos = this.getBlockPos();
        return player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= 64;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    public boolean isPowered() {
        return isPowered;
    }

    public void setPowered(boolean powered) {
        isPowered = powered;
    }

    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }
}