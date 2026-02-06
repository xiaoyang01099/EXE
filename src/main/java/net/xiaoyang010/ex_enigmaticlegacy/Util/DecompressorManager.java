package net.xiaoyang010.ex_enigmaticlegacy.Util;

import morph.avaritia.api.CompressorRecipe;
import morph.avaritia.init.AvaritiaModContent;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class DecompressorManager {
    public static DecompressorManager instance = new DecompressorManager();
    private static Map<String, DecompressRecipeData> recipeMap = new HashMap<>();
    private static boolean isInitialized = false;

    public void loadRecipes() {
        RecipeManager recipeManager = getRecipeManager();
        if (recipeManager == null) return;

        recipeMap.clear();

        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            if (recipe.getType() == AvaritiaModContent.COMPRESSOR_RECIPE_TYPE.get()) {
                if (recipe instanceof CompressorRecipe compressorRecipe) {
                    addCompressorRecipe(compressorRecipe);
                }
            }
        }

        isInitialized = true;
        ExEnigmaticlegacyMod.LOGGER.info("Loaded {} decompressor recipes", recipeMap.size());
    }

    private void addCompressorRecipe(CompressorRecipe recipe) {
        ItemStack result = recipe.getResultItem();

        if (result.isEmpty() || recipe.getIngredients().isEmpty()) {
            return;
        }

        ItemStack[] ingredients = recipe.getIngredients().get(0).getItems();
        if (ingredients.length == 0) {
            return;
        }

        ItemStack ingredient = ingredients[0];
        int cost = recipe.getCost();

        String key = getKey(result);
        recipeMap.put(key, new DecompressRecipeData(ingredient.copy(), cost));
    }

    public DecompressRecipeData getRecipe(ItemStack compressed) {
        if (!isInitialized) {
            loadRecipes();
        }

        if (compressed.isEmpty()) {
            return null;
        }

        return recipeMap.get(getKey(compressed));
    }

    public boolean canDecompress(ItemStack stack) {
        return getRecipe(stack) != null;
    }

    private String getKey(ItemStack stack) {
        return stack.getItem().getRegistryName().toString();
    }

    private RecipeManager getRecipeManager() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.getRecipeManager();
        }

        if (Minecraft.getInstance() != null &&
                Minecraft.getInstance().level != null) {
            return Minecraft.getInstance().level.getRecipeManager();
        }

        return null;
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        instance.loadRecipes();
    }

    @SubscribeEvent
    public static void onResourceReload(AddReloadListenerEvent event) {
        instance.loadRecipes();
    }

    public static class DecompressRecipeData {
        private final ItemStack ingredient;
        private final int count;

        public DecompressRecipeData(ItemStack ingredient, int count) {
            this.ingredient = ingredient;
            this.count = count;
        }

        public ItemStack getIngredient() {
            return ingredient.copy();
        }

        public int getCount() {
            return count;
        }

        public ItemStack getResult(int multiplier) {
            ItemStack result = ingredient.copy();
            result.setCount(count * multiplier);
            return result;
        }
    }
}