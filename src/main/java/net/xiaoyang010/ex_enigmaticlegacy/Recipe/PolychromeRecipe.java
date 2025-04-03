package net.xiaoyang010.ex_enigmaticlegacy.Recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PolychromeRecipe implements IPolychromeRecipe {
    private final ResourceLocation id;
    private final ItemStack output;
    private final List<IngredientWithCount> inputs;
    private final int mana;
    private final int processTime;
    public static final ResourceLocation TYPE_ID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "polychrome");

    // 内部类，用于存储材料及其数量
    public static class IngredientWithCount {
        private final Ingredient ingredient;
        private final int count;

        public IngredientWithCount(Ingredient ingredient, int count) {
            this.ingredient = ingredient;
            this.count = count;
        }

        public Ingredient getIngredient() {
            return ingredient;
        }

        public int getCount() {
            return count;
        }

        public boolean test(ItemStack stack) {
            return this.ingredient.test(stack) && stack.getCount() >= this.count;
        }
    }

    public PolychromeRecipe(ResourceLocation id, ItemStack output, List<IngredientWithCount> inputs, int mana, int processTime) {
        this.id = id;
        this.output = output;
        this.inputs = inputs;
        this.mana = mana;
        this.processTime = processTime;
    }

    @Override
    public boolean matches(Container inv, Level worldIn) {
        List<IngredientWithCount> ingredientsMissing = new ArrayList<>(inputs);

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            int stackIndex = -1;
            for (int j = 0; j < ingredientsMissing.size(); j++) {
                IngredientWithCount ingr = ingredientsMissing.get(j);
                if (ingr.getIngredient().test(stack) && stack.getCount() >= ingr.getCount()) {
                    stackIndex = j;
                    break;
                }
            }

            if (stackIndex == -1) {
                return false; // 有额外不需要的物品或数量不足
            }

            ingredientsMissing.remove(stackIndex);
        }

        return ingredientsMissing.isEmpty(); // 如果所有需要的材料都找到了
    }

    @Override
    public ItemStack assemble(Container inv) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return output;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.POLYCHROME_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.POLYCHROME_TYPE;
    }

    @Override
    public int getMana() {
        return mana;
    }

    public static class Type implements RecipeType<PolychromeRecipe> {
        public static final PolychromeRecipe.Type INSTANCE = new PolychromeRecipe.Type();
        public static final String ID = "polychrome";
        public Type() {}

        @Override
        public String toString() {
            return PolychromeRecipe.TYPE_ID.toString();
        }
    }

    @Override
    public int getProcessTime() {
        return processTime;
    }

    public List<IngredientWithCount> getInputs() {
        return inputs;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<PolychromeRecipe> {
        @Override
        public PolychromeRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ItemStack output = ItemStack.EMPTY;
            if (json.has("output")) {
                JsonObject outputObj = json.getAsJsonObject("output");
                ResourceLocation itemId = new ResourceLocation(outputObj.get("item").getAsString());
                int count = outputObj.has("count") ? outputObj.get("count").getAsInt() : 1;

                output = new ItemStack(net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(itemId), count);

                if (outputObj.has("nbt")) {
                    try {
                        output.setTag(net.minecraft.nbt.TagParser.parseTag(outputObj.get("nbt").getAsString()));
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid NBT string for output", e);
                    }
                }
            }

            JsonArray ingredients = json.getAsJsonArray("ingredients");
            List<IngredientWithCount> inputs = new ArrayList<>();

            for (JsonElement element : ingredients) {
                if (element.isJsonObject()) {
                    JsonObject ingredientObj = element.getAsJsonObject();
                    Ingredient ingredient;
                    int count = 1;

                    // 如果是复杂格式带数量
                    if (ingredientObj.has("ingredient") && ingredientObj.has("count")) {
                        ingredient = Ingredient.fromJson(ingredientObj.get("ingredient"));
                        count = ingredientObj.get("count").getAsInt();
                    }
                    // 标准Ingredient格式
                    else {
                        ingredient = Ingredient.fromJson(element);
                    }

                    inputs.add(new IngredientWithCount(ingredient, count));
                } else {
                    // 简单格式，数量为1
                    Ingredient ingredient = Ingredient.fromJson(element);
                    inputs.add(new IngredientWithCount(ingredient, 1));
                }
            }

            int mana = json.get("mana").getAsInt();
            int processTime = json.has("processTime") ? json.get("processTime").getAsInt() : 200;

            return new PolychromeRecipe(recipeId, output, inputs, mana, processTime);
        }

        @Nullable
        @Override
        public PolychromeRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int inputSize = buffer.readVarInt();
            List<IngredientWithCount> inputs = new ArrayList<>();
            for (int i = 0; i < inputSize; i++) {
                Ingredient ingredient = Ingredient.fromNetwork(buffer);
                int count = buffer.readVarInt();
                inputs.add(new IngredientWithCount(ingredient, count));
            }

            ItemStack output = buffer.readItem();
            int mana = buffer.readVarInt();
            int processTime = buffer.readVarInt();

            return new PolychromeRecipe(recipeId, output, inputs, mana, processTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, PolychromeRecipe recipe) {
            buffer.writeVarInt(recipe.inputs.size());
            for (IngredientWithCount input : recipe.inputs) {
                input.getIngredient().toNetwork(buffer);
                buffer.writeVarInt(input.getCount());
            }

            buffer.writeItem(recipe.output);
            buffer.writeVarInt(recipe.mana);
            buffer.writeVarInt(recipe.processTime);
        }
    }
}