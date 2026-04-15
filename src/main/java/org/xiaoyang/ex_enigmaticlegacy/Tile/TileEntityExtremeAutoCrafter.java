package org.xiaoyang.ex_enigmaticlegacy.Tile;

import com.yuo.endless.Recipe.EndlessRecipes;
import com.yuo.endless.Recipe.ExtremeCraftRecipe;
import com.yuo.endless.Recipe.ExtremeCraftShapeRecipe;
import com.yuo.endless.Recipe.IExtremeCraftRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.xiaoyang.ex_enigmaticlegacy.Block.BlockExtremeAutoCrafter;
import org.xiaoyang.ex_enigmaticlegacy.Container.ContainerExtremeAutoCrafter;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModBlockEntities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TileEntityExtremeAutoCrafter extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> inputItems = NonNullList.withSize(164, ItemStack.EMPTY);
    private boolean isPowered;
    private IExtremeCraftRecipe recipe;
    private ResourceLocation recipeId;

    public TileEntityExtremeAutoCrafter(@NotNull BlockEntityType<TileEntityExtremeAutoCrafter> tileEntityExtremeAutoCrafterBlockEntityType, BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXTREME_AUTO_CRAFTER_TILE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileEntityExtremeAutoCrafter tile) {
        if (level == null || level.isClientSide) return;

        tile.resolveRecipe();

        BlockState blockState = level.getBlockState(pos);
        tile.isPowered = blockState.getValue(BlockExtremeAutoCrafter.POWERED);
        if (!tile.isPowered) return;
        if (tile.recipe == null) return;
        if (!tile.validateRecipeSlots()) return;

        ItemStack recipeOut = getResultItem(tile.recipe);
        if (recipeOut.isEmpty()) return;

        ItemStack outItem = tile.getItem(163);
        NonNullList<Ingredient> ingredients = tile.recipe.getIngredients();
        Map<CompoundTag, Integer> inputItemMap = getRecipes(ingredients);

        if (isInputItem(tile, inputItemMap)) {
            if (!outItem.isEmpty() && !ItemStack.isSameItemSameTags(outItem, recipeOut)) return;

            if (outItem.isEmpty()) {
                tile.setItem(163, recipeOut);
            } else if (outItem.getCount() == 1 && outItem.getMaxStackSize() == 1) {
                return;
            } else {
                int newCount = outItem.getCount() + recipeOut.getCount();
                if (newCount <= outItem.getMaxStackSize()) {
                    outItem.setCount(newCount);
                    tile.setItem(163, outItem);
                } else {
                    return;
                }
            }
            removeInputItem(tile, inputItemMap);
            tile.setChanged();
        }
    }

    private static ItemStack getResultItem(IExtremeCraftRecipe recipe) {
        if (recipe instanceof ExtremeCraftRecipe shaped) {
            return shaped.getResultItem().copy();
        } else if (recipe instanceof ExtremeCraftShapeRecipe shapeless) {
            return shapeless.getResultItem().copy();
        }
        return ItemStack.EMPTY;
    }

    private boolean validateRecipeSlots() {
        if (this.recipe == null) return false;

        NonNullList<Ingredient> ingredients = this.recipe.getIngredients();

        for (int i = 0; i < 81; i++) {
            ItemStack slotStack = this.getItem(81 + i);
            Ingredient ingredient = i < ingredients.size() ? ingredients.get(i) : Ingredient.EMPTY;

            if (ingredient.isEmpty()) {
                if (!slotStack.isEmpty()) return false;
            } else {
                if (slotStack.isEmpty()) return false;
                if (!ingredient.test(slotStack)) return false;
            }
        }

        ItemStack previewStack = this.getItem(162);
        ItemStack recipeResult = getResultItem(this.recipe);
        if (!ItemStack.isSameItemSameTags(previewStack, recipeResult)) return false;

        return true;
    }

    private static void removeInputItem(TileEntityExtremeAutoCrafter tile, Map<CompoundTag, Integer> inputItems) {
        for (int i = 0; i < 81; ++i) {
            ItemStack stack = tile.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag tag = createItemTag(stack);
                if (inputItems.containsKey(tag)) {
                    Integer count = inputItems.get(tag);
                    if (count > stack.getCount()) {
                        inputItems.put(tag, Math.max(count - stack.getCount(), 0));
                        tile.removeItemNoUpdate(i);
                    } else {
                        tile.removeItem(i, count);
                        inputItems.remove(tag);
                    }
                }
            }
        }
    }

    private static boolean isInputItem(TileEntityExtremeAutoCrafter tile, Map<CompoundTag, Integer> inputItems) {
        Map<CompoundTag, Integer> maps = new HashMap<>();
        for (int i = 0; i < 81; ++i) {
            ItemStack stack = tile.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag tag = createItemTag(stack);
                maps.merge(tag, stack.getCount(), Integer::sum);
            }
        }

        int matched = 0;
        for (Entry<CompoundTag, Integer> entry : inputItems.entrySet()) {
            if (maps.containsKey(entry.getKey())
                    && maps.get(entry.getKey()) >= entry.getValue()) {
                matched++;
            }
        }
        return matched == inputItems.size();
    }

    private static Map<CompoundTag, Integer> getRecipes(NonNullList<Ingredient> ingredients) {
        Map<CompoundTag, Integer> inputItems = new HashMap<>();
        for (Ingredient ingredient : ingredients) {
            if (!ingredient.isEmpty()) {
                ItemStack stack = ingredient.getItems()[0];
                CompoundTag tag = createItemTag(stack);
                inputItems.merge(tag, 1, Integer::sum);
            }
        }
        return inputItems;
    }

    private static CompoundTag createItemTag(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        CompoundTag tag = new CompoundTag();
        copy.save(tag);
        return tag;
    }

    private void resolveRecipe() {
        if (this.level == null || this.recipeId == null || this.recipe != null) return;

        RecipeManager recipeManager = this.level.getRecipeManager();

        recipeManager.byKey(this.recipeId).ifPresent(recipe -> {
            if (recipe instanceof IExtremeCraftRecipe extremeRecipe) {
                this.recipe = extremeRecipe;
            }
        });
        if (this.recipe != null) return;

        List<ExtremeCraftRecipe> shapedList = recipeManager
                .getAllRecipesFor(EndlessRecipes.EXTREME_CRAFT_RECIPE.get());
        for (ExtremeCraftRecipe r : shapedList) {
            if (r.getId().equals(this.recipeId)) {
                this.recipe = r;
                return;
            }
        }

        List<ExtremeCraftShapeRecipe> shapelessList = recipeManager
                .getAllRecipesFor(EndlessRecipes.EXTREME_CRAFT_SHAPE_RECIPE.get());
        for (ExtremeCraftShapeRecipe r : shapelessList) {
            if (r.getId().equals(this.recipeId)) {
                this.recipe = r;
                return;
            }
        }

        this.recipeId = null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        ContainerHelper.saveAllItems(tag, inputItems);
        tag.putBoolean("isPowered", isPowered);

        if (recipe != null && recipe.getId() != null) {
            tag.putString("RecipeId", recipe.getId().toString());
        } else if (recipeId != null) {
            tag.putString("RecipeId", recipeId.toString());
        }

        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.inputItems = NonNullList.withSize(164, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, inputItems);
        isPowered = tag.getBoolean("isPowered");

        if (tag.contains("RecipeId", Tag.TAG_STRING)) {
            String recipeIdStr = tag.getString("RecipeId");
            this.recipeId = new ResourceLocation(recipeIdStr);
            this.recipe = null;
        }

        if (this.level != null) {
            resolveRecipe();
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) {
            load(pkt.getTag());
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
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
        return inputItems.subList(0, 81).stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int i) {
        return inputItems.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int i1) {
        return ContainerHelper.removeItem(inputItems, i, i1);
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ContainerHelper.takeItem(inputItems, i);
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        inputItems.set(i, itemStack);
        if (i >= 81 && i <= 162) {
            setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        BlockPos pos = this.getBlockPos();
        return player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= 64;
    }

    @Override
    public void clearContent() {
        inputItems.clear();
    }

    public boolean isPowered() { return isPowered; }
    public void setPowered(boolean powered) { isPowered = powered; }

    public IExtremeCraftRecipe getRecipe() { return recipe; }

    public void setRecipe(IExtremeCraftRecipe recipe) {
        this.recipe = recipe;
        this.recipeId = recipe != null ? recipe.getId() : null;
    }

    public ResourceLocation getRecipeId() { return recipeId; }

    public void setRecipeId(ResourceLocation recipeId) {
        this.recipeId = recipeId;
        this.recipe = null;
    }

    public void dropContents() {
        BlockPos pos = this.getBlockPos();
        for (int i = 0; i < 81; ++i) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), this.getItem(i));
        }
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), this.getItem(163));
    }
}