package net.xiaoyang010.ex_enigmaticlegacy.Recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import morph.avaritia.api.ExtremeCraftingRecipe;
import morph.avaritia.recipe.ExtremeShapedRecipe;
import morph.avaritia.recipe.ExtremeShapelessRecipe;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

import java.util.Arrays;

/**
 * 极限合成拆解配方 - 专门用于 9x9 极限合成台的配方反向解析
 * 支持有序配方（ExtremeShapedRecipe）和无序配方（ExtremeShapelessRecipe）
 */
public class ExtremeDeconRecipe {

    /** 配方ID */
    private final ResourceLocation recipeId;

    /** 配方结果物品（即要拆解的物品） */
    private final ItemStack result;

    /** 配方原材料数组 (81格) */
    private final ItemStack[] ingredients;

    /** 配方宽度 (1-9) */
    private final int width;

    /** 配方高度 (1-9) */
    private final int height;

    /** 配方分组 */
    private final String group;

    /** 是否为无序配方 */
    private final boolean shapeless;

    public ExtremeDeconRecipe(ResourceLocation recipeId, String group, ItemStack result,
                              ItemStack[] ingredients, int width, int height, boolean shapeless) {
        this.recipeId = recipeId;
        this.group = group;
        this.result = result;
        this.ingredients = ingredients;
        this.width = width;
        this.height = height;
        this.shapeless = shapeless;

        if (ingredients.length != 81) {
            throw new IllegalArgumentException("Extreme decon recipe must have exactly 81 ingredient slots!");
        }
    }

    /**
     * 从极限合成配方创建拆解配方（支持有序和无序）
     */
    public static ExtremeDeconRecipe fromExtremeRecipe(ExtremeCraftingRecipe recipe) {
        try {
            ResourceLocation recipeId = recipe.getId();
            String group = recipe.getGroup();
            ItemStack result = recipe.getResultItem().copy();

            ItemStack[] ingredients = new ItemStack[81]; //拆解配方
            int width = 0;
            int height = 0;
            boolean shapeless = false;

            if (recipe instanceof ExtremeShapelessRecipe shapelessRecipe){
                NonNullList<Ingredient> list = shapelessRecipe.getIngredients();
                shapeless = true;
                width = 9;
                height = (int) Math.ceil(list.size() / 9.0f);
                setIngredients(ingredients, list);
            }else if (recipe instanceof ExtremeShapedRecipe shapedRecipe){
                width = shapedRecipe.getWidth();
                height = shapedRecipe.getHeight();
                NonNullList<Ingredient> list = shapedRecipe.getIngredients();
                if (width < 9){
                    NonNullList<Ingredient> nullList = NonNullList.withSize(81, Ingredient.EMPTY);
                    int size = list.size();
                    for (int h = 0; h < 9; h++){
                        for (int w = 0; w < 9; w++){
                            int slot = h * 9 + w;//完整配方9*9格序数
                            int recipeSlot = h * width + w;
                            if (w >= width){
                                nullList.set(slot, Ingredient.EMPTY);
                            }else {
                                if (recipeSlot > size - 1) nullList.set(slot, Ingredient.EMPTY);
                                else nullList.set(slot, list.get(recipeSlot));
                            }
                        }
                    }

                    setIngredients(ingredients, nullList);
                }else {
                    setIngredients(ingredients, list);
                }
            }


            return new ExtremeDeconRecipe(recipeId, group, result, ingredients, width, height, shapeless);

        } catch (Exception e) {
            ExEnigmaticlegacyMod.LOGGER.error("Failed to convert extreme recipe to decon recipe: {}",
                    recipe.getId(), e);
            return null;
        }
    }

    //配方设置
    private static void setIngredients(ItemStack[] ingredients, NonNullList<Ingredient> list){
        int slot = 0;
        for (Ingredient ingredient : list) {
            if (!ingredient.isEmpty() && slot < list.size()) {
                ingredients[slot] = ingredient.getItems()[0];
            }
            slot++;
        }
    }

    /**
     * 处理单个材料并放入数组
     */
    private static void processIngredient(Ingredient ingredient, ItemStack[] ingredients, int index) {
        if (ingredient == null || ingredient.isEmpty() || index >= ingredients.length) {
            return;
        }

        ItemStack[] possibleItems = ingredient.getItems();
        if (possibleItems.length > 0) {
            ItemStack item = possibleItems[0].copy();
            item.setCount(1); // 标准化数量

            // 跳过有容器的物品（如桶、玻璃瓶等）
            if (item.getItem().getCraftingRemainingItem() == null) {
                ingredients[index] = normalizeItemStack(item);
            } else {
                ingredients[index] = ItemStack.EMPTY;
            }
        }
    }

    /**
     * 标准化物品堆栈
     */
    private static ItemStack normalizeItemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack normalized = new ItemStack(stack.getItem(), 1);

        // 处理耐久度
        if (stack.isDamageableItem()) {
            int damage = stack.getDamageValue();
            normalized.setDamageValue(damage == 32767 ? 0 : damage);
        }

        // 复制 NBT 数据（如果需要）
        if (stack.hasTag()) {
            normalized.setTag(stack.getTag().copy());
        }

        return normalized;
    }

    // ==================== Getter 方法 ====================

    public ResourceLocation getRecipeId() {
        return recipeId;
    }

    public String getGroup() {
        return group;
    }

    /**
     * 获取结果物品（拆解目标）
     * @return 物品副本
     */
    public ItemStack getResult() {
        return result.copy();
    }

    /**
     * 获取原材料数组（拆解产物）
     * @return 81格材料数组的深拷贝
     */
    public ItemStack[] getIngredients() {
        ItemStack[] copy = new ItemStack[81];
        for (int i = 0; i < 81; i++) {
            copy[i] = ingredients[i] != null ? ingredients[i].copy() : ItemStack.EMPTY;
        }
        return copy;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * 是否为无序配方
     */
    public boolean isShapeless() {
        return shapeless;
    }

    /**
     * 获取指定位置的材料
     * @param x 横坐标 (0-8)
     * @param y 纵坐标 (0-8)
     */
    public ItemStack getIngredientAt(int x, int y) {
        if (x < 0 || x >= 9 || y < 0 || y >= 9) {
            return ItemStack.EMPTY;
        }
        int index = y * 9 + x;
        return ingredients[index] != null ? ingredients[index].copy() : ItemStack.EMPTY;
    }

    /**
     * 获取所有实际使用的材料（非空），主要用于无序配方
     */
    public NonNullList<ItemStack> getActualIngredients() {
        NonNullList<ItemStack> actualIngredients = NonNullList.create();
        for (ItemStack stack : ingredients) {
            if (stack != null && !stack.isEmpty()) {
                actualIngredients.add(stack.copy());
            }
        }
        return actualIngredients;
    }

    /**
     * 获取非空材料数量
     */
    public int getNonEmptyIngredientCount() {
        return (int) Arrays.stream(ingredients)
                .filter(stack -> stack != null && !stack.isEmpty())
                .count();
    }

    /**
     * 验证配方是否有效
     */
    public boolean isValid() {
        // 结果物品不能为空
        if (result.isEmpty()) {
            return false;
        }

        // 至少要有一个材料
        if (getNonEmptyIngredientCount() == 0) {
            return false;
        }

        // 宽高必须在合理范围内
        if (width < 1 || width > 9 || height < 1 || height > 9) {
            return false;
        }

        // 配方不能自循环（材料中不能包含结果物品）
        for (ItemStack ingredient : ingredients) {
            if (ingredient != null && !ingredient.isEmpty()) {
                if (ItemStack.isSameItemSameTags(ingredient, result)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 检查物品是否匹配此配方的结果
     */
    public boolean matchesResult(ItemStack stack) {
        if (stack.isEmpty() || result.isEmpty()) {
            return false;
        }

        // 检查物品类型
        if (!stack.is(result.getItem())) {
            return false;
        }

        // 检查数量
        if (stack.getCount() < result.getCount()) {
            return false;
        }

        // 检查耐久度（如果是可损坏物品）
        if (result.isDamageableItem() && stack.getDamageValue() != result.getDamageValue()) {
            return false;
        }

        // 检查 NBT（如果需要精确匹配）
        // 这里可以根据需求调整
        return true;
    }

    @Override
    public String toString() {
        return String.format("ExtremeDeconRecipe{id=%s, result=%s, ingredients=%d/%d, size=%dx%d, shapeless=%s}",
                recipeId,
                result.getHoverName().getString(),
                getNonEmptyIngredientCount(),
                81,
                width,
                height,
                shapeless);
    }
}