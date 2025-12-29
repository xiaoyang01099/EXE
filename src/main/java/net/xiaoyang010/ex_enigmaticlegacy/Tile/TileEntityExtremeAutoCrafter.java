package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import morph.avaritia.api.ExtremeCraftingRecipe;
import morph.avaritia.init.AvaritiaModContent;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Block.BlockExtremeAutoCrafter;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ContainerExtremeAutoCrafter;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

public final class TileEntityExtremeAutoCrafter extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(164, ItemStack.EMPTY);
    private boolean isPowered;
    private String recipe;

    public TileEntityExtremeAutoCrafter(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXTREME_AUTO_CRAFTER_TILE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileEntityExtremeAutoCrafter tile) {
        if (level == null || level.isClientSide)
            return;
        BlockState blockState = level.getBlockState(pos);
        tile.isPowered = blockState.getValue(BlockExtremeAutoCrafter.POWERED);
        if (!tile.isPowered) return;
        if (tile.recipe == null || tile.recipe.isEmpty()) return;

        Stream<ExtremeCraftingRecipe> recipeStream = level.getRecipeManager().getAllRecipesFor(AvaritiaModContent.EXTREME_CRAFTING_RECIPE_TYPE.get()).stream().filter(e -> e.getId().compareTo(new ResourceLocation(tile.getRecipe())) == 0);
        if (recipeStream.findFirst().isPresent()) {
            ExtremeCraftingRecipe recipe = recipeStream.findFirst().get();
            ItemStack recipeOut = recipe.getResultItem();
            ItemStack outItem = tile.getItem(163);
            if (recipeOut.isEmpty()) return;

            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            Map<ItemStack, Integer> inputItems = getRecipes(ingredients);

            if (isInputItem(tile, inputItems)){
                if (!outItem.isEmpty() && outItem.getItem() != recipeOut.getItem()) return;

                removeInputItem(tile, inputItems);
                if (outItem.isEmpty()) tile.setItem(163, recipeOut);
                else {
                    outItem.setCount(outItem.getCount() + recipeOut.getCount());
                    tile.setItem(163, outItem);
                }
                setChanged(level, pos, state);
            }

        }
    }

    //判断输入是否匹配
    private static void removeInputItem(TileEntityExtremeAutoCrafter tile, Map<ItemStack, Integer> inputItems){
        for (int i = 0; i < 81; ++i){
            ItemStack stack = tile.getItem(i);
            if (!stack.isEmpty()){
                if (inputItems.containsKey(stack)){
                    Integer integer = inputItems.get(stack);
                    if (integer > stack.getCount()){ //输入数量不够
                        inputItems.put(stack, Math.max(integer - stack.getCount(), 0));
                        tile.removeItemNoUpdate(i);
                    }else {
                        tile.removeItem(i, integer);
                        inputItems.remove(stack);
                    }
                }
            }
        }
    }

    //判断输入是否匹配
    private static boolean isInputItem(TileEntityExtremeAutoCrafter tile, Map<ItemStack, Integer> inputItems){
        Map<ItemStack, Integer> maps = new HashMap<>();
        for (int i = 0; i < 81; ++i){
            ItemStack stack = tile.getItem(i);
            if (!stack.isEmpty()){
                if (maps.containsKey(stack)){
                    maps.put(stack, maps.get(stack) + stack.getCount());
                }else maps.put(stack, stack.getCount());
            }
        }

        int num = 0;
        for (Entry<ItemStack, Integer> entry : inputItems.entrySet()) {
            if (maps.containsKey(entry.getKey())){
                if (maps.get(entry.getKey()) >= entry.getValue()){
                    num++;
                }
            }
        }

        return num == inputItems.size();
    }

    /**
     * 将配方输入转为类型map
     */
    private static Map<ItemStack, Integer> getRecipes(NonNullList<Ingredient> ingredients){
        Map<ItemStack, Integer> inputItems = new HashMap<>();
        for (Ingredient ingredient : ingredients) {
            if (!ingredient.isEmpty()){
                ItemStack stack = ingredient.getItems()[0];
                if (inputItems.containsKey(stack)) {
                    inputItems.put(stack, inputItems.get(stack) + 1);
                }else inputItems.put(stack, 1);
            }
        }

        return inputItems;
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