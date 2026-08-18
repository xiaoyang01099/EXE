package org.xiaoyang.ex_enigmaticlegacy.Recipe.Manager;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.yuo.endless.Recipe.ExtremeCraftRecipe;
import com.yuo.endless.Recipe.ExtremeCraftShapeRecipe;
import com.yuo.endless.Recipe.IExtremeCraftRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Recipe.ExtremeDeconRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Exe.MODID)
public class ExtremeCraftingDeconstructionManager {
    public static final ExtremeCraftingDeconstructionManager instance = new ExtremeCraftingDeconstructionManager();
    private static final Multimap<ResourceLocation, ExtremeDeconRecipe> extremeRecipeMap = HashMultimap.create();
    private static boolean isInitialized = false;

    public void loadExtremeCraftingRecipes() {
        RecipeManager recipeManager = getRecipeManager();
        if (recipeManager == null) {
            Exe.LOGGER.warn("RecipeManager is null, cannot load extreme recipes");
            return;
        }

        extremeRecipeMap.clear();

        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            if (recipe instanceof IExtremeCraftRecipe extremeRecipe) {
                if (!(extremeRecipe instanceof ExtremeCraftRecipe)
                        && !(extremeRecipe instanceof ExtremeCraftShapeRecipe)) {
                    Exe.LOGGER.warn("Unknown IExtremeCraftRecipe type: {}, skipping",
                            recipe.getClass().getName());
                    continue;
                }

                ExtremeDeconRecipe deconRecipe = ExtremeDeconRecipe.fromEndlessRecipe(extremeRecipe);

                if (deconRecipe != null && deconRecipe.isValid()) {
                    addExtremeDeconRecipe(deconRecipe);

                    if (Exe.LOGGER.isDebugEnabled()) {
                        String type = deconRecipe.isShapeless() ? "Shapeless" : "Shaped";
                        Exe.LOGGER.debug(
                                "Loaded endless {} recipe: {} -> {} ({}x{}, {} ingredients)",
                                type,
                                deconRecipe.getRecipeId(),
                                deconRecipe.getResult().getHoverName().getString(),
                                deconRecipe.getWidth(),
                                deconRecipe.getHeight(),
                                deconRecipe.getNonEmptyIngredientCount()
                        );
                    }
                }
            }
        }

        isInitialized = true;
        Exe.LOGGER.info("Loaded {} endless extreme crafting recipes",
                extremeRecipeMap.size());
    }

    private RecipeManager getRecipeManager() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.getRecipeManager();
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.level != null) {
            return mc.level.getRecipeManager();
        }

        return null;
    }

    private void addExtremeDeconRecipe(ExtremeDeconRecipe recipe) {
        ItemStack result = recipe.getResult();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(result.getItem());
        if (itemId != null && !itemId.equals(BuiltInRegistries.ITEM.getDefaultKey())) {
            extremeRecipeMap.put(itemId, recipe);
        } else {
            Exe.LOGGER.warn("Recipe {} has invalid result item", recipe.getRecipeId());
        }
    }

    public boolean hasExtremeCraftingRecipe(ItemStack stack) {
        if (!isInitialized) loadExtremeCraftingRecipes();
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null) return false;
        return new ArrayList<>(extremeRecipeMap.get(itemId))
                .stream()
                .anyMatch(recipe -> recipe.matchesResult(stack));
    }

    public List<ExtremeDeconRecipe> getExtremeCraftingRecipes(ItemStack stack) {
        if (!isInitialized) loadExtremeCraftingRecipes();
        if (stack == null || stack.isEmpty()) return new ArrayList<>();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null) return new ArrayList<>();
        return new ArrayList<>(extremeRecipeMap.get(itemId))
                .stream()
                .filter(recipe -> recipe.matchesResult(stack))
                .sorted((r1, r2) -> Integer.compare(
                        r2.getNonEmptyIngredientCount(),
                        r1.getNonEmptyIngredientCount()))
                .collect(Collectors.toList());
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