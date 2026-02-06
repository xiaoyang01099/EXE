package net.xiaoyang010.ex_enigmaticlegacy.Recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
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
import net.xiaoyang010.ex_enigmaticlegacy.Tile.CelestialHTTile;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class CelestialTransmuteRecipe implements Recipe<Container> {
    public static final ResourceLocation TYPE_ID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "celestial_transmute");
    private final ResourceLocation id;
    private final ItemStack result;
    private final NonNullList<Ingredient> recipeItems;
    private final NonNullList<Integer> inputCounts;

    public CelestialTransmuteRecipe(ResourceLocation id, ItemStack output, NonNullList<Ingredient> recipeItems, NonNullList<Integer> inputCounts) {
        this.id = id;
        this.result = output;
        this.recipeItems = recipeItems;
        this.inputCounts = inputCounts;
    }

     public static List<CelestialTransmuteRecipe> getAllRecipes() {
       return Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);
   }

    public NonNullList<Ingredient> getRecipeItems() {
        return this.recipeItems;
    }

    @Override
    public boolean matches(Container pContainer, Level pLevel) {
        if (pLevel.isClientSide()) {
            return false;
        }

        if (pContainer instanceof CelestialHTTile){
            CelestialHTTile tile = (CelestialHTTile) pContainer;
            for (int i = 0; i < recipeItems.size(); i++) {
                Ingredient ingredient = recipeItems.get(i);
                ItemStack stack = tile.getItem(i + 1);

                if (!ingredient.test(stack) || stack.getCount() < inputCounts.get(i))
                    return false;
            }
            return true;
        }
        return false;
    }

    public NonNullList<Integer> getInputCounts() {
        return inputCounts;
    }

    @Override
    public ItemStack assemble(Container container) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CELESTIAL_TRANSMUTE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.CHT_TYPE;
    }

    public static class Type implements RecipeType<CelestialTransmuteRecipe> {
       public Type(){ }
       public static final Type INSTANCE = new Type();

        @Override
        public String toString() {
            return CelestialTransmuteRecipe.TYPE_ID.toString();
        }
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<CelestialTransmuteRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =new ResourceLocation(ExEnigmaticlegacyMod.MODID,"celestial_transmute");

        @Override
        public CelestialTransmuteRecipe fromJson(ResourceLocation id, JsonObject json) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

            JsonArray ingredients = GsonHelper.getAsJsonArray(json, "inputs");
            NonNullList<Ingredient> inputs = NonNullList.withSize(4, Ingredient.EMPTY);
            NonNullList<Integer> counts = NonNullList.withSize(4, 1);

            for (int i = 0; i < inputs.size(); i++) {
                JsonObject inputObj = ingredients.get(i).getAsJsonObject();
                inputs.set(i, Ingredient.fromJson(inputObj));
                if (inputObj.has("count")) {
                    counts.set(i, inputObj.get("count").getAsInt());
                }
            }
            return new CelestialTransmuteRecipe(id, output, inputs, counts);
        }


        @Nullable
        @Override
        public CelestialTransmuteRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(4, Ingredient.EMPTY);
            NonNullList<Integer> counts = NonNullList.withSize(4, 1);

            for (int i = 0; i < 4; i++) {
                inputs.set(i, Ingredient.fromNetwork(buf));
                counts.set(i, buf.readVarInt());
            }

            ItemStack output = buf.readItem();
            return new CelestialTransmuteRecipe(id, output, inputs, counts);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CelestialTransmuteRecipe recipe) {
            for (int i = 0; i < recipe.recipeItems.size(); i++) {
                recipe.recipeItems.get(i).toNetwork(buf);
                buf.writeVarInt(recipe.inputCounts.get(i));
            }
            buf.writeItemStack(recipe.getResultItem(), false);
        }
    }
}
