package net.xiaoyang010.ex_enigmaticlegacy.Recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import net.xiaoyang010.ex_enigmaticlegacy.api.IPolychromeRecipe;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.crafting.RecipeSerializerBase;
import vazkii.botania.common.crafting.recipe.RecipeUtils;

import javax.annotation.Nonnull;

public class PolychromeRecipe implements IPolychromeRecipe {
    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<Ingredient> inputs;
    private final int mana;
    public static final ResourceLocation TYPE_ID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "polychrome");

    public PolychromeRecipe(ResourceLocation id, int mana, NonNullList<Ingredient> inputs, ItemStack output) {
        this.id = id;
        this.mana = mana;
        this.inputs = inputs;
        this.output = output;
    }

    public int getManaUsage() {
        return this.mana;
    }

    @Override
    public boolean matches(Container inv, @Nonnull Level world) {
        int nonEmptySlots = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (!inv.getItem(i).isEmpty()) {
                if (inv.getItem(i).getCount() > 1) {
                    return false;
                }
                nonEmptySlots++;
            }
        }

        IntOpenHashSet usedSlots = new IntOpenHashSet(inv.getContainerSize());
        return RecipeUtils.matches(inputs, inv, usedSlots) && usedSlots.size() == nonEmptySlots;
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull Container inv) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.POLYCHROME_TYPE;
    }

    @Nonnull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return inputs;
    }

    @Nonnull
    @Override
    public ItemStack getResultItem() {
        return output;
    }


    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<PolychromeRecipe> getSerializer() {
        return ModRecipes.POLYCHROME_SERIALIZER.get();
    }

    public static class Type implements RecipeType<PolychromeRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "polychrome";

        public Type() {
        }

        @Override
        public String toString() {
            return PolychromeRecipe.TYPE_ID.toString();
        }
    }

    public static class Serializer extends RecipeSerializerBase<PolychromeRecipe> {
        @Nonnull
        @Override
        public PolychromeRecipe fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
            int mana = GsonHelper.getAsInt(json, "mana");
            JsonArray ingrs = GsonHelper.getAsJsonArray(json, "ingredients");
            Ingredient[] ingredients = new Ingredient[ingrs.size()];
            for (int i = 0; i < ingrs.size(); i++) {
                ingredients[i] = Ingredient.fromJson(ingrs.get(i));
            }
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            return new PolychromeRecipe(recipeId, mana, NonNullList.of(Ingredient.EMPTY, ingredients), output);
        }

        @Override
        public PolychromeRecipe fromNetwork(@Nonnull ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int mana = buffer.readVarInt();
            Ingredient[] ingredients = new Ingredient[buffer.readVarInt()];
            for (int i = 0; i < ingredients.length; i++) {
                ingredients[i] = Ingredient.fromNetwork(buffer);
            }
            ItemStack output = buffer.readItem();
            return new PolychromeRecipe(recipeId, mana, NonNullList.of(Ingredient.EMPTY, ingredients), output);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, PolychromeRecipe recipe) {
            buffer.writeVarInt(recipe.mana);
            buffer.writeVarInt(recipe.getIngredients().size());
            for (Ingredient ingr : recipe.getIngredients()) {
                ingr.toNetwork(buffer);
            }
            buffer.writeItem(recipe.output);
        }
    }
}