package net.xiaoyang010.ex_enigmaticlegacy.Recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import org.jetbrains.annotations.Nullable;

public class AncientAlphirineRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack output;
    private final int chance;
    public static final ResourceLocation TYPE_ID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "ancient_alphirine");


    public AncientAlphirineRecipe(ResourceLocation id, Ingredient input, ItemStack output, int chance) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.chance = Math.max(1, Math.min(100, chance));
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (container.isEmpty()) return false;
        return this.input.test(container.getItem(0));
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
        return this.output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ANCIENT_ALPHIRINE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ANCIENT_ALPHIRINE_TYPE;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(this.input);
        return ingredients;
    }

    public boolean hasChance() {
        return this.chance < 100;
    }

    public String getChanceString() {
        return this.chance + "%";
    }

    public Ingredient getInput() {
        return this.input;
    }

    public int getChance() {
        return this.chance;
    }

    public static class Type implements RecipeType<AncientAlphirineRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "ancient_alphirine";

        public Type() {
        }

        @Override
        public String toString() {
            return AncientAlphirineRecipe.TYPE_ID.toString();
        }
    }

    public static class Serializer implements RecipeSerializer<AncientAlphirineRecipe> {
        private static final ResourceLocation NAME = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "ancient_alphirine");

        @Override
        public AncientAlphirineRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            int chance = GsonHelper.getAsInt(json, "chance", 100);

            return new AncientAlphirineRecipe(recipeId, input, output, chance);
        }

        @Override
        public AncientAlphirineRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            ItemStack output = buffer.readItem();
            int chance = buffer.readInt();

            return new AncientAlphirineRecipe(recipeId, input, output, chance);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, AncientAlphirineRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeItem(recipe.output);
            buffer.writeInt(recipe.chance);
        }


        @Override
        public RecipeSerializer<?> setRegistryName(ResourceLocation name) {
            return this;
        }

        @Override
        public @Nullable ResourceLocation getRegistryName() {
            return NAME;
        }

        @Override
        public Class<RecipeSerializer<?>> getRegistryType() {
            return (Class<RecipeSerializer<?>>) (Class<?>) RecipeSerializer.class;
        }
    }
}