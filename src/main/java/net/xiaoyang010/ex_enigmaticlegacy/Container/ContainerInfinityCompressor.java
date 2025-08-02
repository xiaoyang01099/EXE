package net.xiaoyang010.ex_enigmaticlegacy.Container;

import morph.avaritia.recipe.CompressorRecipeHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileEntityInfinityCompressor;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IResourceShapedContainer;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.WContainer;

import javax.annotation.Nonnull;

public class ContainerInfinityCompressor extends WContainer<TileEntityInfinityCompressor> implements IResourceShapedContainer {
    public ContainerInfinityCompressor(MenuType<?> menuType, int containerId, @Nonnull final TileEntityInfinityCompressor wTileEntity, final Inventory playerInventory) {
        super(menuType, containerId, wTileEntity);

        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 9; x++)
                addSlot(new Slot(playerInventory, 9 + y * 9 + x, 8 + (18 * x), 84 + (18 * y)));

        for (int i = 0; i < 9; i++)
            addSlot(new Slot(playerInventory, i, 8 + (18 * i), 142));
    }

    public ContainerInfinityCompressor(int containerId, Inventory playerInventory, @Nonnull final TileEntityInfinityCompressor wTileEntity) {
        this(ModMenus.INFINITY_COMPRESSOR_MENU, containerId, wTileEntity, playerInventory);
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull final Player player, final int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        final Slot actualSlot = this.slots.get(slotIndex);
        if (actualSlot != null && actualSlot.hasItem()) {
            ItemStack itemstack1 = actualSlot.getItem();
            itemstack = itemstack1.copy();
            if (slotIndex < 27) {
                if (!moveItemStackTo(itemstack1, 27, 36, false))
                    return ItemStack.EMPTY;
            } else {
                if (!moveItemStackTo(itemstack1, 0, 27, false))
                    return ItemStack.EMPTY;
            }
            if (itemstack1.getCount() == 0)
                actualSlot.set(ItemStack.EMPTY);
            actualSlot.setChanged();
        }
        return itemstack;
    }

    @Override
    public void defineShape(@Nonnull final ResourceLocation resourceLocation) {
        if (getTile().getLevel() != null) {
            var recipeMap = CompressorRecipeHelper.getRecipeMap(getTile().getLevel());
            var recipe = recipeMap.get(resourceLocation);
            getTile().compressorRecipeField.setCompressorRecipe(recipe);
        }
    }

    @Override
    public void clearShape() {
        getTile().compressorRecipeField.setCompressorRecipe(null);
    }
}