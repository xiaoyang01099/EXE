package org.xiaoyang.ex_enigmaticlegacy.Recipe.Manager;

import com.yuo.endless.Recipe.EndlessRecipes;
import com.yuo.endless.Recipe.NeutroniumRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Exe.MODID)
public class DecompressorManager {
    public static DecompressorManager instance = new DecompressorManager();
    private static Map<String, DecompressRecipeData> recipeMap = new HashMap<>();
    private static boolean isInitialized = false;

    public void loadRecipes() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            Exe.LOGGER.warn("DecompressorManager: Server is null, cannot load recipes");
            return;
        }

        RecipeManager recipeManager = server.getRecipeManager();
        RegistryAccess registryAccess = server.registryAccess();

        recipeMap.clear();

        List<NeutroniumRecipe> recipes = recipeManager.getAllRecipesFor(
                EndlessRecipes.NEUTRONIUM_RECIPE.get()
        );

        Exe.LOGGER.info("Found {} neutronium recipes to process", recipes.size());

        for (NeutroniumRecipe recipe : recipes) {
            addCompressorRecipe(recipe, registryAccess);
        }

        isInitialized = true;
        Exe.LOGGER.info("Loaded {} decompressor recipes", recipeMap.size());
    }

    private void addCompressorRecipe(NeutroniumRecipe recipe, RegistryAccess registryAccess) {
        ItemStack singularity = recipe.getResultItem(registryAccess);

        if (singularity.isEmpty()) {
            Exe.LOGGER.warn("Skipping recipe with empty output: {}", recipe.getId());
            return;
        }

        Ingredient input = recipe.getInput();
        ItemStack[] ingredientItems = input.getItems();

        if (ingredientItems.length == 0) {
            Exe.LOGGER.warn("Skipping recipe with empty ingredient array: {}", recipe.getId());
            return;
        }

        ItemStack rawMaterial = ingredientItems[0];
        int count = recipe.getRecipeCount();

        String key = getKey(singularity);
        if (key.isEmpty()) return;

        recipeMap.put(key, new DecompressRecipeData(rawMaterial.copy(), count));

        Exe.LOGGER.debug("Loaded: {} -> {} x{}",
                singularity.getDisplayName().getString(),
                rawMaterial.getDisplayName().getString(),
                count);
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
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (registryName == null) return "";

        String baseKey = registryName.toString();

        if (stack.hasTag() && stack.getTag() != null) {
            return baseKey + "@" + stack.getTag().toString();
        }

        return baseKey;
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        instance.loadRecipes();
    }

    @SubscribeEvent
    public static void onResourceReload(AddReloadListenerEvent event) {
        isInitialized = false;
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