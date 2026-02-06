package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import morph.avaritia.api.ExtremeCraftingRecipe;
import morph.avaritia.init.AvaritiaModContent;
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
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Block.BlockExtremeAutoCrafter;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ContainerExtremeAutoCrafter;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TileEntityExtremeAutoCrafter extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> inputItems = NonNullList.withSize(164, ItemStack.EMPTY);
    private boolean isPowered;
    private ExtremeCraftingRecipe recipe;
    private ResourceLocation recipeId;

    public TileEntityExtremeAutoCrafter(@NotNull BlockEntityType<TileEntityExtremeAutoCrafter> tileEntityExtremeAutoCrafterBlockEntityType, BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXTREME_AUTO_CRAFTER_TILE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileEntityExtremeAutoCrafter tile) {
        if (level == null || level.isClientSide)
            return;

        tile.resolveRecipe();

        BlockState blockState = level.getBlockState(pos);
        tile.isPowered = blockState.getValue(BlockExtremeAutoCrafter.POWERED);
        if (!tile.isPowered) return;
        if (tile.recipe == null) return;

        if (!tile.validateRecipeSlots()) {
            return;
        }

        ItemStack recipeOut = tile.recipe.getResultItem().copy();
        ItemStack outItem = tile.getItem(163);
        if (recipeOut.isEmpty()) return;

        NonNullList<Ingredient> ingredients = tile.recipe.getIngredients();
        Map<CompoundTag, Integer> inputItems = getRecipes(ingredients);

        if (isInputItem(tile, inputItems)){
            if (!outItem.isEmpty() && !ItemStack.isSameItemSameTags(outItem, recipeOut)) return;

            if (outItem.isEmpty()) {
                tile.setItem(163, recipeOut);
            }else if (outItem.getCount() == 1 && outItem.getMaxStackSize() == 1){
                return;
            }else {
                int newCount = outItem.getCount() + recipeOut.getCount();
                if (newCount <= outItem.getMaxStackSize()) {
                    outItem.setCount(newCount);
                    tile.setItem(163, outItem);
                } else {
                    return;
                }
            }
            removeInputItem(tile, inputItems);
            tile.setChanged();
        }
    }


    private boolean validateRecipeSlots() {

        if (this.recipe == null) return false;

        NonNullList<Ingredient> ingredients = this.recipe.getIngredients();

        for (int i = 0; i < 81; i++) {
            ItemStack slotStack = this.getItem(81 + i);

            Ingredient ingredient = i < ingredients.size() ? ingredients.get(i) : Ingredient.EMPTY;

            if (ingredient.isEmpty()) {
                if (!slotStack.isEmpty()) {
                    return false;
                }
            } else {
                if (slotStack.isEmpty()) {
                    return false;
                }
                if (!ingredient.test(slotStack)) {
                    return false;
                }
            }
        }

        ItemStack previewStack = this.getItem(162);
        ItemStack recipeResult = this.recipe.getResultItem();
        if (!ItemStack.isSameItemSameTags(previewStack, recipeResult)) {
            return false;
        }

        return true;
    }

    private static void removeInputItem(TileEntityExtremeAutoCrafter tile, Map<CompoundTag, Integer> inputItems){
        for (int i = 0; i < 81; ++i){
            ItemStack stack = tile.getItem(i);
            if (!stack.isEmpty()){
                CompoundTag tag = createItemTag(stack);
                if (inputItems.containsKey(tag)){
                    Integer integer = inputItems.get(tag);
                    if (integer > stack.getCount()){
                        inputItems.put(tag, Math.max(integer - stack.getCount(), 0));
                        tile.removeItemNoUpdate(i);
                    }else {
                        tile.removeItem(i, integer);
                        inputItems.remove(tag);
                    }
                }
            }
        }
    }

    private static boolean isInputItem(TileEntityExtremeAutoCrafter tile, Map<CompoundTag, Integer> inputItems){
        Map<CompoundTag, Integer> maps = new HashMap<>();
        for (int i = 0; i < 81; ++i){
            ItemStack stack = tile.getItem(i);
            if (!stack.isEmpty()){
                CompoundTag tag = createItemTag(stack);
                if (maps.containsKey(tag)){
                    maps.put(tag, maps.get(tag) + stack.getCount());
                }else maps.put(tag, stack.getCount());
            }
        }

        int num = 0;
        for (Entry<CompoundTag, Integer> entry : inputItems.entrySet()) {
            if (maps.containsKey(entry.getKey())){
                if (maps.get(entry.getKey()) >= entry.getValue()){
                    num++;
                }
            }
        }

        return num == inputItems.size();
    }

    private static Map<CompoundTag, Integer> getRecipes(NonNullList<Ingredient> ingredients){
        Map<CompoundTag, Integer> inputItems = new HashMap<>();
        for (Ingredient ingredient : ingredients) {
            if (!ingredient.isEmpty()){
                ItemStack stack = ingredient.getItems()[0];
                CompoundTag tag  = createItemTag(stack);
                if (inputItems.containsKey(tag)) {
                    inputItems.put(tag, inputItems.get(tag) + 1);
                }else inputItems.put(tag, 1);
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
        if (this.level == null || this.recipeId == null || this.recipe != null) {
            return;
        }

        RecipeManager recipeManager = this.level.getRecipeManager();
        recipeManager.byKey(this.recipeId).ifPresentOrElse(
                recipe -> {
                    if (recipe instanceof ExtremeCraftingRecipe extremeRecipe) {
                        this.recipe = extremeRecipe;
                    } else {
                        this.recipeId = null;
                    }
                },
                () -> {
                    List<ExtremeCraftingRecipe> recipes = recipeManager
                            .getAllRecipesFor(AvaritiaModContent.EXTREME_CRAFTING_RECIPE_TYPE.get());

                    for (ExtremeCraftingRecipe recipe : recipes) {
                        if (recipe.getId().equals(this.recipeId)) {
                            this.recipe = recipe;
                            return;
                        }
                    }
                    this.recipeId = null;
                }
        );
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
        return inputItems.subList(0, 81).isEmpty();
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

    public boolean isPowered() {
        return isPowered;
    }

    public void setPowered(boolean powered) {
        isPowered = powered;
    }

    public ExtremeCraftingRecipe getRecipe() {
        return recipe;
    }

    public void setRecipe(ExtremeCraftingRecipe recipe) {
        this.recipe = recipe;
        this.recipeId = recipe != null ? recipe.getId() : null;
    }

    public ResourceLocation getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(ResourceLocation recipeId) {
        this.recipeId = recipeId;
        this.recipe = null;
    }

    public void dropContents() {
        BlockPos pos = this.getBlockPos();
        for(int i = 0; i < 81; ++i) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), this.getItem(i));
        }
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), this.getItem(163));
    }
}
