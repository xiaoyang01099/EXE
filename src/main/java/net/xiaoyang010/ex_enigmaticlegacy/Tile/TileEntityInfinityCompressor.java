package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import net.xiaoyang010.ex_enigmaticlegacy.api.CompressorRecipeField;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ContainerInfinityCompressor;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.*;

import javax.annotation.Nonnull;

public class TileEntityInfinityCompressor extends WTileEntity implements MenuProvider {
    private static final int[] slotsForFace = new int[243];

    public final CompressorRecipeField compressorRecipeField = new CompressorRecipeField(this);
    public final RedstoneControl redstoneControl;
    public final CheckBox trashBox = new CheckBox("ex_enigmaticlegacy.compressor.trashcan", true);

    public TileEntityInfinityCompressor(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        getController(ControlController.class).add((this.redstoneControl = new RedstoneControl(this)));
        final FieldController fieldController = getController(FieldController.class);
        fieldController.add(compressorRecipeField);
        fieldController.add(trashBox);
        addCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, new InvWrapper(this));
    }

    private boolean metConditions(@Nonnull final ItemStack stack) {
        ItemStack output;
        if (compressorRecipeField.isNull())
            compressorRecipeField.setCompressorRecipe(morph.avaritia.recipe.CompressorRecipeHelper.getRecipe(getLevel(), stack));
        else if (!trashBox.isChecked() && !compressorRecipeField.matches(stack))
            return false;
        return redstoneControl.canOperate() && !compressorRecipeField.isNull() && ((output = itemStacks.get(0)).isEmpty() || output.getCount() + compressorRecipeField.getCompressorRecipeOutput().getCount() <= output.getMaxStackSize());
    }

    @Override
    protected NonNullList<ItemStack> getItemStacks() {
        return NonNullList.withSize(1, ItemStack.EMPTY);
    }

    @Nonnull
    public int[] getSlotsForFace(@Nonnull final Direction side) {
        return slotsForFace.clone();
    }

    @Nonnull
    @Override
    public ItemStack removeItem(final int index, final int count) {
        return index == 0 ? super.removeItem(index, count) : ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack getItem(final int index) {
        return index == 0 ? super.getItem(index) : ItemStack.EMPTY;
    }

    @Override
    public boolean canPlaceItemThroughFace(final int index, @Nonnull final ItemStack itemStackIn, @Nonnull final Direction direction) {
        return index > 0 && this.canPlaceItem(index, itemStackIn);
    }

    @Override
    public boolean canPlaceItem(final int index, @Nonnull final ItemStack stack) {
        return index > 0 && metConditions(stack);
    }

    @Nonnull
    @Override
    public ItemStack removeItemNoUpdate(final int index) {
        return index == 0 ? super.removeItemNoUpdate(index) : ItemStack.EMPTY;
    }

    @Override
    public void setItem(final int index, @Nonnull final ItemStack stack) {
        if (index == 0)
            super.setItem(index, stack);
        else if (compressorRecipeField.matches(stack))
            compressorRecipeField.addProgress(stack.getCount());
    }

    @Override
    public boolean canTakeItemThroughFace(final int index, @Nonnull final ItemStack stack, @Nonnull final Direction direction) {
        return index == 0;
    }

    @Nonnull
    @Override
    public String getDefaultName() {
        return "container.ex_enigmaticlegacy.infinity_compressor.name";
    }

    @Override
    public Component getDisplayName() {
        return EComponent.translatable(getDefaultName());
    }

    @Override
    public int getContainerSize() {
        return 243;
    }

    static {
        for (int i = 0; i < slotsForFace.length; i++)
            slotsForFace[i] = i;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ContainerInfinityCompressor(containerId, playerInventory,this);
    }
}