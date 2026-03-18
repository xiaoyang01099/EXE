package net.xiaoyang010.ex_enigmaticlegacy.Recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.xiaoyang010.ex_enigmaticlegacy.Container.slot.MagicTableRecipeInput;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;

public class MagicTableRecipe implements Recipe<MagicTableRecipeInput> {
    private final ResourceLocation id;
    private final Ingredient input;
    private final int inputCount;
    private final ItemStack output;
    private final long outputCount;
    private final BigInteger emcCost;
    private final int craftTicks;

    public MagicTableRecipe(ResourceLocation id, Ingredient input, int inputCount, ItemStack output, long outputCount, BigInteger emcCost, int craftTicks) {
        this.id = id;
        this.input = input;
        this.inputCount = inputCount;
        this.output = output;
        this.outputCount = outputCount;
        this.emcCost = emcCost;
        this.craftTicks = craftTicks;
    }

    @Override
    public boolean matches(@NotNull MagicTableRecipeInput container, @NotNull Level level) {
        ItemStack inputStack = container.getInput();
        return !inputStack.isEmpty()
                && input.test(inputStack)
                && inputStack.getCount() >= inputCount;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull MagicTableRecipeInput container) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem() {
        return output.copy();
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.MAGIC_TABLE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.MAGIC_TABLE_TYPE.get();
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(input);
        return list;
    }

    public Ingredient getInput() {
        return input;
    }

    public int getInputCount() {
        return inputCount;
    }

    public ItemStack getOutput() {
        return output.copy();
    }

    public long getOutputCount() {
        return outputCount;
    }

    public BigInteger getEmcCost() {
        return emcCost;
    }

    public int getCraftTicks() {
        return craftTicks;
    }

    public boolean matchesInput(ItemStack stack) {
        return !stack.isEmpty() && input.test(stack) && stack.getCount() >= inputCount;
    }

    @Override
    public String toString() {
        return "MagicTableRecipe{" + id + ": " + inputCount + "x input + " +
                emcCost + " EMC -> " + outputCount + "x " +
                output.getItem().getRegistryName() + "}";
    }

    public static class Type implements RecipeType<MagicTableRecipe> {
        public static final MagicTableRecipe.Type INSTANCE = new MagicTableRecipe.Type();
        public static final String ID = "magic_table";

        @Override
        public String toString() {
            return ID;
        }
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<MagicTableRecipe> {

        @Override
        public @NotNull MagicTableRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            Ingredient input;
            if (json.has("input")) {
                JsonElement inputElement = json.get("input");
                input = Ingredient.fromJson(inputElement);
            } else {
                throw new JsonSyntaxException("Missing 'input' field in magic_table recipe " + recipeId);
            }
            int inputCount = GsonHelper.getAsInt(json, "input_count", 1);
            if (inputCount <= 0) {
                throw new JsonSyntaxException("'input_count' must be > 0 in recipe " + recipeId);
            }
            ItemStack output;
            if (json.has("output")) {
                JsonObject outputObj = GsonHelper.getAsJsonObject(json, "output");
                output = ShapedRecipe.itemStackFromJson(outputObj);
            } else {
                throw new JsonSyntaxException("Missing 'output' field in magic_table recipe " + recipeId);
            }
            long outputCount = parseOutputCount(json, recipeId);
            BigInteger emcCost = parseEmcCost(json, recipeId);
            int craftTicks = GsonHelper.getAsInt(json, "craft_ticks", 200);
            if (craftTicks <= 0) {
                throw new JsonSyntaxException("'craft_ticks' must be > 0 in recipe " + recipeId);
            }
            return new MagicTableRecipe(recipeId, input, inputCount, output, outputCount, emcCost, craftTicks);
        }
        @Override
        public @Nullable MagicTableRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buf) {
            Ingredient input = Ingredient.fromNetwork(buf);
            int inputCount = buf.readVarInt();
            ItemStack output = buf.readItem();
            long outputCount = buf.readLong();
            String emcCostStr = buf.readUtf(256);
            BigInteger emcCost;
            try {
                emcCost = new BigInteger(emcCostStr);
            } catch (NumberFormatException e) {
                emcCost = BigInteger.ZERO;
            }
            int craftTicks = buf.readVarInt();
            return new MagicTableRecipe(recipeId, input, inputCount, output, outputCount, emcCost, craftTicks);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buf, @NotNull MagicTableRecipe recipe) {
            recipe.getInput().toNetwork(buf);
            buf.writeVarInt(recipe.getInputCount());
            buf.writeItem(recipe.getOutput());
            buf.writeLong(recipe.getOutputCount());
            buf.writeUtf(recipe.getEmcCost().toString(), 256);
            buf.writeVarInt(recipe.getCraftTicks());
        }

        private static long parseOutputCount(JsonObject json, ResourceLocation recipeId) {
            if (!json.has("output_count")) {
                return 1;
            }
            JsonElement element = json.get("output_count");
            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isNumber()) {
                    long value = element.getAsLong();
                    if (value <= 0) {
                        throw new JsonSyntaxException(
                                "'output_count' must be > 0 in recipe " + recipeId);
                    }
                    return value;
                } else if (element.getAsJsonPrimitive().isString()) {
                    try {
                        long value = Long.parseLong(element.getAsString());
                        if (value <= 0) {
                            throw new JsonSyntaxException(
                                    "'output_count' must be > 0 in recipe " + recipeId);
                        }
                        return value;
                    } catch (NumberFormatException e) {
                        throw new JsonSyntaxException(
                                "Invalid 'output_count' value '" + element.getAsString() +
                                        "' in recipe " + recipeId + ". Must be a valid long.");
                    }
                }
            }
            throw new JsonSyntaxException(
                    "'output_count' must be a number or numeric string in recipe " + recipeId);
        }

        private static BigInteger parseEmcCost(JsonObject json, ResourceLocation recipeId) {
            if (!json.has("emc_cost")) {
                throw new JsonSyntaxException("Missing 'emc_cost' field in magic_table recipe " + recipeId);
            }
            JsonElement element = json.get("emc_cost");
            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isNumber()) {
                    long value = element.getAsLong();
                    if (value < 0) {
                        throw new JsonSyntaxException(
                                "'emc_cost' must be >= 0 in recipe " + recipeId);
                    }
                    return BigInteger.valueOf(value);
                } else if (element.getAsJsonPrimitive().isString()) {
                    try {
                        BigInteger value = new BigInteger(element.getAsString());
                        if (value.compareTo(BigInteger.ZERO) < 0) {
                            throw new JsonSyntaxException(
                                    "'emc_cost' must be >= 0 in recipe " + recipeId);
                        }
                        return value;
                    } catch (NumberFormatException e) {
                        throw new JsonSyntaxException(
                                "Invalid 'emc_cost' value '" + element.getAsString() +
                                        "' in recipe " + recipeId + ". Must be a valid integer.");
                    }
                }
            }
            throw new JsonSyntaxException(
                    "'emc_cost' must be a number or numeric string in recipe " + recipeId);
        }
    }
}