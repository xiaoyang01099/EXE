package net.xiaoyang010.ex_enigmaticlegacy.Container;

import morph.avaritia.api.ExtremeCraftingRecipe;
import morph.avaritia.container.slot.OutputSlot;
import morph.avaritia.init.AvaritiaModContent;
import morph.avaritia.recipe.ExtremeShapedRecipe;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.client.Minecraft;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileEntityExtremeAutoCrafter;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IGhostAcceptorContainer;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IResourceShapedContainer;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.Util;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.WContainer;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.slot.ShapeSlot;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.slot.SpecialSlot;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ContainerExtremeAutoCrafter extends WContainer<TileEntityExtremeAutoCrafter> implements IResourceShapedContainer, IGhostAcceptorContainer {

    private final TileEntityExtremeAutoCrafter tileEntityExtremeAutoCrafter;
    private final Level level;
    private final int playerInventoryEnds, playerInventoryStarts, inventoryFull, result;

    public ContainerExtremeAutoCrafter(int containerId, Inventory inventory, @Nonnull final TileEntityExtremeAutoCrafter tileEntityExtremeAutoCrafter) {
        super(ModMenus.EXTREME_AUTO_CRAFTER_MENU, containerId, tileEntityExtremeAutoCrafter);
        this.tileEntityExtremeAutoCrafter = tileEntityExtremeAutoCrafter;
        final List<Slot> slotList = new ArrayList<>();
        for (int y = 0; y < 9; y++)
            for (int x = 0; x < 9; x++)
                slotList.add((new Slot(tileEntityExtremeAutoCrafter, y * 9 + x, 8 + (18 * x), 18 + (18 * y))));
        for (int y = 0; y < 9; y++)
            for (int x = 0; x < 9; x++)
                slotList.add((new ShapeSlot(tileEntityExtremeAutoCrafter, 81 + (y * 9 + x), 175 + (18 * x), 18 + (18 * y))));
        slotList.add((new ShapeSlot(tileEntityExtremeAutoCrafter, 162, 247, 194))); //配方结果格
        slotList.add((new OutputSlot(tileEntityExtremeAutoCrafter, 163, 247, 222)));
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 9; x++)
                slotList.add((new Slot(inventory, 9 + y * 9 + x, 43 + (18 * x), 194 + (18 * y))));
        for (int i = 0; i < 9; i++)
            slotList.add((new Slot(inventory, i, 43 + (18 * i), 252)));
        slotList.forEach(this::addSlot);
        final int inventorySize = slots.size();
        playerInventoryEnds = inventorySize;
        playerInventoryStarts = inventorySize - 36;
        inventoryFull = (playerInventoryStarts - 1) / 2;//81
        result = inventoryFull * 2 + 1; //163 容器输出格
        this.level = tileEntityExtremeAutoCrafter.getLevel();
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull final Player player, final int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        final Slot actualSlot = this.slots.get(slotIndex);
        if (actualSlot.hasItem()) {
            ItemStack itemstack1 = actualSlot.getItem();
            itemstack = itemstack1.copy();
            if (slotIndex >= playerInventoryStarts) {
                if (!moveItemStackTo(itemstack1, 0, inventoryFull, false))
                    return ItemStack.EMPTY;
            } else if (slotIndex <= inventoryFull || slotIndex == result) {
                if (!moveItemStackTo(itemstack1, playerInventoryStarts, playerInventoryEnds, true))
                    return ItemStack.EMPTY;
            }
            if (itemstack1.getCount() == 0)
                actualSlot.set(ItemStack.EMPTY);
            actualSlot.setChanged();
        }
        return itemstack;
    }

    @Override
    public void clicked(final int slotId, final int mouseButton, @Nonnull final ClickType clickType, @Nonnull final Player player) {
        if (slotId >= inventoryFull && slotId < 162) {
            final Slot actualSlot = slots.get(slotId);
            if (clickType == ClickType.QUICK_MOVE) {
                actualSlot.set(ItemStack.EMPTY);
            } else if (clickType == ClickType.PICKUP) {
                final ItemStack playerStack = getCarried();
                final boolean slotHasStack = actualSlot.hasItem();
                if (!playerStack.isEmpty() && !slotHasStack) {
                    final ItemStack newSlotStack = playerStack.copy();
                    newSlotStack.setCount(1);
                    actualSlot.set(newSlotStack);
                } else if (playerStack.isEmpty() && slotHasStack || !playerStack.isEmpty() && ItemStack.isSameItemSameTags(playerStack, actualSlot.getItem()))
                    actualSlot.set(ItemStack.EMPTY);
            }
            tileEntityExtremeAutoCrafter.recipeShapeChanged();
        } else if (slotId == 162){
            if (clickType == ClickType.PICKUP) {
                slots.get(slotId).set(ItemStack.EMPTY);
                for (int i = inventoryFull; i <= 162; i++){
                    slots.get(i).set(ItemStack.EMPTY);
                }
                tileEntityExtremeAutoCrafter.recipeShapeChanged();
            }
        }else super.clicked(slotId, mouseButton, clickType, player);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        //实时设置输出
//        CraftingContainer craftingContainer = new CraftingContainer(this, 81, 161);
//        Optional<ExtremeCraftingRecipe> recipeFor = this.level.getRecipeManager().getRecipeFor(AvaritiaModContent.EXTREME_CRAFTING_RECIPE_TYPE.get(), craftingContainer, level);
//        if (recipeFor.isPresent()) {
//            this.getSlot(162).set(recipeFor.get().getResultItem().copy());
//        }else this.getSlot(162).set(ItemStack.EMPTY);
    }

    @Override
    public void defineShape(@Nonnull final ResourceLocation resourceLocation) {
        Level level = tileEntityExtremeAutoCrafter.getLevel();
        if (level == null) {
            level = Minecraft.getInstance().level;
        }

        if (level == null) return;

        Optional<? extends ExtremeCraftingRecipe> recipeOpt = level.getRecipeManager()
                .byKey(resourceLocation)
                .filter(recipe -> recipe instanceof ExtremeCraftingRecipe)
                .map(recipe -> (ExtremeCraftingRecipe) recipe);

        if (!recipeOpt.isPresent()) return;

        ExtremeCraftingRecipe extremeRecipe = recipeOpt.get();
        final int startsIn = inventoryFull, endsIn = result;
        final int root = (int) Math.sqrt(endsIn - startsIn);
        clearShape(startsIn, endsIn);
        final List<Ingredient> inputs = extremeRecipe.getIngredients();

        if (extremeRecipe instanceof ExtremeShapedRecipe shapedRecipe) {
            int recipeWidth = shapedRecipe.getWidth();
            int recipeHeight = shapedRecipe.getHeight();

            int i = 0;
            for (int y = 0; y < root; y++) {
                for (int x = 0; x < root; x++) {
                    if (i >= inputs.size() || x >= recipeWidth || y >= recipeHeight)
                        continue;
                    final Slot slot = slots.get(startsIn + (x + (root * y)));
                    final ItemStack stackInput = Util.getStackFromIngredient(inputs.get(i++));
                    if (!stackInput.isEmpty())
                        slot.set(stackInput);
                }
            }
        } else {
            for (int i = 0; i < inputs.size() && i < root * root; i++) {
                final Slot slot = slots.get(startsIn + i);
                final ItemStack stackInput = Util.getStackFromIngredient(inputs.get(i));
                if (!stackInput.isEmpty())
                    slot.set(stackInput);
            }
        }
        tileEntityExtremeAutoCrafter.recipeShapeChanged();
        broadcastChanges();
    }

    @Override
    public void clearShape() {
        final int slotCount = slots.size();
        final int startsIn = ((slotCount - 36) / 2) - 1, endsIn = slotCount - 37;
        clearShape(startsIn, endsIn);
        tileEntityExtremeAutoCrafter.recipeShapeChanged();
    }

    private void clearShape(final int startsIn, final int endsIn) {
        for (int i = startsIn; i < endsIn; i++)
            slots.get(i).set(ItemStack.EMPTY);
    }

    @Override
    public void acceptGhostStack(final int slotIndex, @Nonnull ItemStack itemStack) {
        if (slotIndex >= inventoryFull && slotIndex < result) {
            final Slot actualSlot = slots.get(slotIndex);
            if (ItemStack.isSameItemSameTags(itemStack, actualSlot.getItem()))
                actualSlot.set(ItemStack.EMPTY);
            else
                actualSlot.set(itemStack);
            tileEntityExtremeAutoCrafter.recipeShapeChanged();
            broadcastChanges();
        }
    }
}