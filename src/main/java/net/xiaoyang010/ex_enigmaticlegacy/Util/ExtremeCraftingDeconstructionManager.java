package net.xiaoyang010.ex_enigmaticlegacy.Util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.ExtremeDeconRecipe;
import morph.avaritia.api.ExtremeCraftingRecipe;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class ExtremeCraftingDeconstructionManager {
    public static final ExtremeCraftingDeconstructionManager instance = new ExtremeCraftingDeconstructionManager();
    private static final Multimap<ResourceLocation, ExtremeDeconRecipe> extremeRecipeMap = HashMultimap.create();
    private static boolean isInitialized = false;

    public void loadExtremeCraftingRecipes() {
        RecipeManager recipeManager = getRecipeManager();
        if (recipeManager == null) {
            ExEnigmaticlegacyMod.LOGGER.warn("RecipeManager is null, cannot load extreme recipes");
            return;
        }

        extremeRecipeMap.clear();

        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            if (recipe instanceof ExtremeCraftingRecipe) {
                ExtremeCraftingRecipe extremeRecipe = (ExtremeCraftingRecipe) recipe;
                ExtremeDeconRecipe deconRecipe = ExtremeDeconRecipe.fromExtremeRecipe(extremeRecipe);

                if (deconRecipe != null && deconRecipe.isValid()) {
                    addExtremeDeconRecipe(deconRecipe);

                    if (deconRecipe.isShapeless()) {
                    } else {
                    }

                    if (ExEnigmaticlegacyMod.LOGGER.isDebugEnabled()) {
                        String type = deconRecipe.isShapeless() ? "Shapeless" : "Shaped";
                        ExEnigmaticlegacyMod.LOGGER.debug("Loaded extreme {} recipe: {} -> {} ({}x{}, {} ingredients)",
                                type,
                                deconRecipe.getRecipeId(),
                                deconRecipe.getResult().getHoverName().getString(),
                                deconRecipe.getWidth(),
                                deconRecipe.getHeight(),
                                deconRecipe.getNonEmptyIngredientCount());
                    }
                }
            }
        }
        isInitialized = true;
    }

    private RecipeManager getRecipeManager() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.getRecipeManager();
        }

        if (Minecraft.getInstance() != null && Minecraft.getInstance().level != null) {
            return Minecraft.getInstance().level.getRecipeManager();
        }

        return null;
    }

    private void addExtremeDeconRecipe(ExtremeDeconRecipe recipe) {
        ItemStack result = recipe.getResult();
        ResourceLocation itemId = result.getItem().getRegistryName();

        if (itemId != null) {
            extremeRecipeMap.put(itemId, recipe);
        } else {
            ExEnigmaticlegacyMod.LOGGER.warn("Recipe {} has invalid result item", recipe.getRecipeId());
        }
    }

    public boolean hasExtremeCraftingRecipe(ItemStack stack) {
        if (!isInitialized) {
            loadExtremeCraftingRecipes();
        }

        if (stack == null || stack.isEmpty()) {
            return false;
        }

        ResourceLocation itemId = stack.getItem().getRegistryName();
        if (itemId == null) {
            return false;
        }

        List<ExtremeDeconRecipe> recipes = new ArrayList<>(extremeRecipeMap.get(itemId));
        return recipes.stream().anyMatch(recipe -> recipe.matchesResult(stack));
    }

    public List<ExtremeDeconRecipe> getExtremeCraftingRecipes(ItemStack stack) {
        if (!isInitialized) {
            loadExtremeCraftingRecipes();
        }

        if (stack == null || stack.isEmpty()) {
            return new ArrayList<>();
        }

        ResourceLocation itemId = stack.getItem().getRegistryName();
        if (itemId == null) {
            return new ArrayList<>();
        }

        List<ExtremeDeconRecipe> recipes = new ArrayList<>(extremeRecipeMap.get(itemId));

        List<ExtremeDeconRecipe> matchedRecipes = recipes.stream()
                .filter(recipe -> recipe.matchesResult(stack))
                .collect(Collectors.toList());

        matchedRecipes.sort((r1, r2) ->
                Integer.compare(r2.getNonEmptyIngredientCount(), r1.getNonEmptyIngredientCount()));

        return matchedRecipes;
    }

    public void clearRecipes() {
        extremeRecipeMap.clear();
        isInitialized = false;
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        instance.loadExtremeCraftingRecipes();
    }

    @SubscribeEvent
    public static void onResourceReload(AddReloadListenerEvent event) {
        instance.clearRecipes();
        instance.loadExtremeCraftingRecipes();
    }
}