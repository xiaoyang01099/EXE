package net.xiaoyang010.ex_enigmaticlegacy.Recipe;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StarlitSanctumRecipe implements Recipe<Container> {
    public static final String RECIPE_ID = "starlit_crafting";
    private static final int INPUT_LEFT_SLOT = 486;
    private static final int INPUT_RIGHT_SLOT = 487;
    private static final int GRID_ROWS = 18;
    private static final int GRID_COLS_PER_BLOCK = 9;
    private static final int TOTAL_ROW_WIDTH = 27;
    private final ResourceLocation id;
    private final long manaCost;
    private final ItemStack result;
    private final Ingredient leftInput;
    private final int leftInputCount;
    private final Ingredient rightInput;
    private final int rightInputCount;
    private final List<NonNullList<Ingredient>> patternGroups;

    public StarlitSanctumRecipe(ResourceLocation id, long manaCost, Ingredient leftInput, int leftInputCount, Ingredient rightInput, int rightInputCount, List<NonNullList<Ingredient>> patternGroups, ItemStack result) {
        this.id = id;
        this.manaCost = manaCost;
        this.leftInput = leftInput;
        this.leftInputCount = leftInputCount;
        this.rightInput = rightInput;
        this.rightInputCount = rightInputCount;
        this.patternGroups = patternGroups;
        this.result = result;
    }

    public long getManaCost() {
        return manaCost;
    }

    public Ingredient getLeftInput() {
        return leftInput;
    }

    public int getLeftInputCount() {
        return leftInputCount;
    }

    public Ingredient getRightInput() {
        return rightInput;
    }

    public int getRightInputCount() {
        return rightInputCount;
    }

    public List<NonNullList<Ingredient>> getPatternGroups() {
        return patternGroups;
    }

    @Override
    public boolean matches(Container pContainer, Level pLevel) {
        ItemStack leftStack = pContainer.getItem(INPUT_LEFT_SLOT);
        if (!leftInput.test(leftStack) || leftStack.getCount() < leftInputCount) {
            return false;
        }

        ItemStack rightStack = pContainer.getItem(INPUT_RIGHT_SLOT);
        if (!rightInput.test(rightStack) || rightStack.getCount() < rightInputCount) {
            return false;
        }

        List<List<ItemStack>> containerBlocks = new ArrayList<>();
        containerBlocks.add(extractBlock(pContainer, 0));
        containerBlocks.add(extractBlock(pContainer, 1));
        containerBlocks.add(extractBlock(pContainer, 2));

        List<NonNullList<Ingredient>> remainingPatterns = new ArrayList<>(this.patternGroups);

        for (List<ItemStack> blockItems : containerBlocks) {
            NonNullList<Ingredient> matchedPattern = null;

            for (NonNullList<Ingredient> pattern : remainingPatterns) {
                if (checkPatternMatch(blockItems, pattern)) {
                    matchedPattern = pattern;
                    break;
                }
            }

            if (matchedPattern == null) {
                return false;
            }

            remainingPatterns.remove(matchedPattern);
        }

        return remainingPatterns.isEmpty();
    }

    private List<ItemStack> extractBlock(Container container, int blockIndex) {
        List<ItemStack> items = new ArrayList<>();
        int startCol = blockIndex * GRID_COLS_PER_BLOCK;
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS_PER_BLOCK; col++) {
                int realIndex = row * TOTAL_ROW_WIDTH + (startCol + col);
                items.add(container.getItem(realIndex));
            }
        }
        return items;
    }

    private boolean checkPatternMatch(List<ItemStack> items, NonNullList<Ingredient> ingredients) {
        if (items.size() != ingredients.size()) return false;

        for (int i = 0; i < items.size(); i++) {
            Ingredient required = ingredients.get(i);
            ItemStack actual = items.get(i);

            if (!required.test(actual)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(Container pContainer) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return result;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.STARLIT_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.STARLIT_TYPE;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<StarlitSanctumRecipe> {

        private static Map<String, Ingredient> customKeyFromJson(JsonObject json) {
            Map<String, Ingredient> map = Maps.newHashMap();

            for (Map.Entry<String, com.google.gson.JsonElement> entry : json.entrySet()) {
                if (entry.getKey().length() != 1) {
                    throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol.");
                }

                if (" ".equals(entry.getKey())) {
                    throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
                }

                map.put(entry.getKey(), Ingredient.fromJson(entry.getValue()));
            }

            map.put(" ", Ingredient.EMPTY);
            return map;
        }

        @Override
        public StarlitSanctumRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
            long mana;
            if (pJson.has("mana")) {
                if (pJson.get("mana").isJsonPrimitive() && pJson.get("mana").getAsJsonPrimitive().isNumber()) {
                    mana = pJson.get("mana").getAsLong();
                } else {
                    throw new JsonParseException("'mana' must be a number");
                }
            } else {
                throw new JsonParseException("Missing 'mana' field");
            }

            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));

            JsonObject leftJson = GsonHelper.getAsJsonObject(pJson, "left_input");
            Ingredient leftIng = Ingredient.fromJson(leftJson);
            int leftCount = GsonHelper.getAsInt(leftJson, "count", 1);

            JsonObject rightJson = GsonHelper.getAsJsonObject(pJson, "right_input");
            Ingredient rightIng = Ingredient.fromJson(rightJson);
            int rightCount = GsonHelper.getAsInt(rightJson, "count", 1);

            List<NonNullList<Ingredient>> patterns = new ArrayList<>();
            JsonArray patternArray = GsonHelper.getAsJsonArray(pJson, "patterns");

            if (patternArray.size() != 3) {
                throw new JsonParseException("Recipe must contain exactly 3 pattern blocks");
            }

            for (int i = 0; i < 3; i++) {
                JsonObject patternObj = patternArray.get(i).getAsJsonObject();
                patterns.add(parsePatternBlock(patternObj));
            }

            return new StarlitSanctumRecipe(pRecipeId, mana, leftIng, leftCount, rightIng, rightCount, patterns, result);
        }

        private NonNullList<Ingredient> parsePatternBlock(JsonObject json) {
            Map<String, Ingredient> key = customKeyFromJson(GsonHelper.getAsJsonObject(json, "key"));
            JsonArray patternArray = GsonHelper.getAsJsonArray(json, "pattern");

            if (patternArray.size() > GRID_ROWS) {
                throw new JsonParseException("Pattern height cannot exceed " + GRID_ROWS);
            }

            NonNullList<Ingredient> ingredients = NonNullList.withSize(GRID_ROWS * GRID_COLS_PER_BLOCK, Ingredient.EMPTY);

            for (int row = 0; row < patternArray.size(); row++) {
                String rowStr = patternArray.get(row).getAsString();
                if (rowStr.length() > GRID_COLS_PER_BLOCK) {
                    throw new JsonParseException("Pattern width cannot exceed " + GRID_COLS_PER_BLOCK);
                }

                for (int col = 0; col < rowStr.length(); col++) {
                    String charKey = String.valueOf(rowStr.charAt(col));
                    int index = row * GRID_COLS_PER_BLOCK + col;
                    Ingredient ingredient = key.getOrDefault(charKey, Ingredient.EMPTY);
                    ingredients.set(index, ingredient);
                }
            }
            return ingredients;
        }

        @Nullable
        @Override
        public StarlitSanctumRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            long mana = pBuffer.readLong();

            Ingredient leftIng = Ingredient.fromNetwork(pBuffer);
            int leftCount = pBuffer.readInt();

            Ingredient rightIng = Ingredient.fromNetwork(pBuffer);
            int rightCount = pBuffer.readInt();

            ItemStack result = pBuffer.readItem();

            List<NonNullList<Ingredient>> patterns = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                int size = pBuffer.readVarInt();
                NonNullList<Ingredient> list = NonNullList.withSize(size, Ingredient.EMPTY);
                for (int j = 0; j < size; j++) {
                    list.set(j, Ingredient.fromNetwork(pBuffer));
                }
                patterns.add(list);
            }

            return new StarlitSanctumRecipe(pRecipeId, mana, leftIng, leftCount, rightIng, rightCount, patterns, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, StarlitSanctumRecipe pRecipe) {
            pBuffer.writeLong(pRecipe.manaCost);

            pRecipe.leftInput.toNetwork(pBuffer);
            pBuffer.writeInt(pRecipe.leftInputCount);

            pRecipe.rightInput.toNetwork(pBuffer);
            pBuffer.writeInt(pRecipe.rightInputCount);

            pBuffer.writeItem(pRecipe.result);

            for (NonNullList<Ingredient> pattern : pRecipe.patternGroups) {
                pBuffer.writeVarInt(pattern.size());
                for (Ingredient ing : pattern) {
                    ing.toNetwork(pBuffer);
                }
            }
        }
    }

    public static class Type implements RecipeType<StarlitSanctumRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "starlit_crafting";

        @Override
        public String toString() {
            return ID;
        }
    }
}