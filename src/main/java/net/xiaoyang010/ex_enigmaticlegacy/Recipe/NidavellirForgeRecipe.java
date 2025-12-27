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
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NidavellirForgeRecipe implements Recipe<Container> {
    public static final ResourceLocation TYPE_ID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "nidavellir_forge");

    private final ResourceLocation id;
    private final ItemStack output;
    private final int color;
    private final List<ItemStack> inputs;
    private final int mana;

    public NidavellirForgeRecipe(ResourceLocation id, ItemStack output, int mana, int color, ItemStack... inputs) {
        this.id = id;
        this.output = output;
        this.mana = mana;
        this.color = color;
        this.inputs = new ArrayList<>(Arrays.asList(inputs));
    }

    public List<ItemStack> getInputs() {
        return new ArrayList<>(this.inputs);
    }

    public ItemStack getOutput() {
        return this.output.copy();
    }

    public int getManaUsage() {
        return this.mana;
    }

    public int getColor() {
        return this.color;
    }

    public boolean matches(Container inv) {
        List<ItemStack> inputsRequired = new ArrayList<>(this.inputs);

        for (ItemStack requiredInput : inputsRequired) {
            boolean found = false;

            for (int i = 1; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty() && simpleAreStacksEqual(requiredInput.copy(), stack)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }
        return true;
    }

    private boolean simpleAreStacksEqual(ItemStack input, ItemStack stack) {
        if (input.getDamageValue() == 32767) {
            input.setDamageValue(stack.getDamageValue());
        }
        return input.getItem() == stack.getItem() &&
                (input.getDamageValue() == stack.getDamageValue() || input.getDamageValue() == 32767) &&
                stack.getCount() >= input.getCount();
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (level.isClientSide()) {
            return false;
        }
        return matches(container);
    }

    @Override
    public ItemStack assemble(Container container) {
        return output.copy();
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
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (ItemStack input : inputs) {
            ingredients.add(Ingredient.of(input));
        }
        return ingredients;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<NidavellirForgeRecipe> getSerializer() {
        return ModRecipes.NIDAVELLIR_FORGE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.NIDAVELLIR_FORGE_TYPE;
    }

    public static class Type implements RecipeType<NidavellirForgeRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "nidavellir_forge";
    }

    public static class Serializer implements RecipeSerializer<NidavellirForgeRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        private static final ResourceLocation NAME = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "nidavellir_forge");

        @Override
        public NidavellirForgeRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            int mana = GsonHelper.getAsInt(json, "mana");
            int color = GsonHelper.getAsInt(json, "color");

            JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            ItemStack[] inputs = new ItemStack[ingredients.size()];

            for (int i = 0; i < ingredients.size(); i++) {
                inputs[i] = ShapedRecipe.itemStackFromJson(ingredients.get(i).getAsJsonObject());
            }

            return new NidavellirForgeRecipe(recipeId, output, mana, color, inputs);
        }

        @Override
        public NidavellirForgeRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            ItemStack output = buffer.readItem();
            int mana = buffer.readInt();
            int color = buffer.readInt();

            int inputCount = buffer.readInt();
            ItemStack[] inputs = new ItemStack[inputCount];
            for (int i = 0; i < inputCount; i++) {
                inputs[i] = buffer.readItem();
            }

            return new NidavellirForgeRecipe(recipeId, output, mana, color, inputs);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, NidavellirForgeRecipe recipe) {
            buffer.writeItem(recipe.output);
            buffer.writeInt(recipe.mana);
            buffer.writeInt(recipe.color);

            buffer.writeInt(recipe.inputs.size());
            for (ItemStack input : recipe.inputs) {
                buffer.writeItem(input);
            }
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