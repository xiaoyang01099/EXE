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
import net.xiaoyang010.ex_enigmaticlegacy.Tile.CelestialHTTile;
import org.jetbrains.annotations.Nullable;


public class CelestialTransmuteRecipe implements Recipe<Container> {
    public static final ResourceLocation TYPE_ID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "celestial_transmute");
    private final ResourceLocation id;
    private final ItemStack result;
    private final NonNullList<Ingredient> recipeItems;

    public CelestialTransmuteRecipe(ResourceLocation id, ItemStack output, NonNullList<Ingredient> recipeItems) {
        this.id = id;
        this.result = output;
        this.recipeItems = recipeItems;
    }

//    public static List<CelestialTransmuteRecipe> getAllRecipes() {
//        return Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(CelestialTransmuteRecipe.Type.INSTANCE);
//    }


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
                if (!ingredient.test(stack)) return false;
            }
            return true;
        }
        return false;
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
//        public Type(){ }
//        public static final Type INSTANCE = new Type();
//        public static final String ID = "celestial_transmute";

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

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
            }

            return new CelestialTransmuteRecipe(id, output, inputs);
        }


        @Nullable
        @Override
        public CelestialTransmuteRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(4, Ingredient.EMPTY);

            inputs.replaceAll(ignored -> Ingredient.fromNetwork(buf));

            ItemStack output = buf.readItem();
            return new CelestialTransmuteRecipe(id, output, inputs);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CelestialTransmuteRecipe recipe) {
            for (Ingredient recipeItem : recipe.recipeItems) {
                recipeItem.toNetwork(buf);
            }
            buf.writeItemStack(recipe.getResultItem(), false);
        }


    }
}
