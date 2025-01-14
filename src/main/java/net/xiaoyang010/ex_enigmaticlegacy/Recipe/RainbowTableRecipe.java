package net.xiaoyang010.ex_enigmaticlegacy.Recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.RainbowTableTile;

import javax.annotation.Nullable;

public class RainbowTableRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<Ingredient> recipeItems;
    private final NonNullList<Integer> inputCounts;

    public static final ResourceLocation TYPE_ID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "rainbow_table");

    public RainbowTableRecipe(ResourceLocation id, ItemStack output, NonNullList<Ingredient> recipeItems, NonNullList<Integer> inputCounts) {
        this.id = id;
        this.output = output;
        this.recipeItems = recipeItems;
        this.inputCounts = inputCounts;
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (!(container instanceof RainbowTableTile)) {
            return false;
        }

        for (int i = 0; i < recipeItems.size(); i++) {
            ItemStack stack = container.getItem(i);
            if (!recipeItems.get(i).test(stack) || stack.getCount() < inputCounts.get(i)) {
                return false;
            }
        }
        return true;
    }

    public NonNullList<Integer> getInputCounts() {
        return inputCounts;
    }

    @Override
    public ItemStack assemble(Container container) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.RAINBOW_TABLE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.RAINBOW_TABLE_TYPE;
    }

    public NonNullList<Ingredient> getRecipeItems() {
        return recipeItems;
    }

    public static class Type implements RecipeType<RainbowTableRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "rainbow_table";
        public Type() {}

        @Override
        public String toString() {
            return RainbowTableRecipe.TYPE_ID.toString();
        }
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<RainbowTableRecipe> {

        @Override
        public RainbowTableRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));

            JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(4, Ingredient.EMPTY);
            NonNullList<Integer> counts = NonNullList.withSize(4, 1);

            for (int i = 0; i < inputs.size(); i++) {
                JsonObject inputObj = ingredients.get(i).getAsJsonObject();
                inputs.set(i, Ingredient.fromJson(inputObj));
                if (inputObj.has("count")) {
                    counts.set(i, inputObj.get("count").getAsInt());
                }
            }

            return new RainbowTableRecipe(recipeId, output, inputs, counts);
        }

        @Override
        public @Nullable RainbowTableRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(4, Ingredient.EMPTY);
            NonNullList<Integer> counts = NonNullList.withSize(4, 1);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(buffer));
                counts.set(i, buffer.readVarInt());
            }

            ItemStack output = buffer.readItem();
            return new RainbowTableRecipe(recipeId, output, inputs, counts);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RainbowTableRecipe recipe) {
            for (int i = 0; i < recipe.getRecipeItems().size(); i++) {
                recipe.getRecipeItems().get(i).toNetwork(buffer);
                buffer.writeVarInt(recipe.getInputCounts().get(i));
            }
            buffer.writeItemStack(recipe.getResultItem(), false);
        }

    }
}