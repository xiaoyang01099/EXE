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
import morph.avaritia.recipe.ExtremeShapedRecipe;
import morph.avaritia.recipe.ExtremeShapelessRecipe;

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
        int loadedCount = 0;
        int shapelessCount = 0;
        int shapedCount = 0;

        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            // 检查是否为极限合成配方
            if (recipe instanceof ExtremeCraftingRecipe extremeRecipe) {
                ExtremeDeconRecipe deconRecipe = ExtremeDeconRecipe.fromExtremeRecipe(extremeRecipe);

                if (deconRecipe != null && deconRecipe.isValid()) {
                    addExtremeDeconRecipe(deconRecipe);
                    loadedCount++;

                    // 统计配方类型
                    if (deconRecipe.isShapeless()) {
                        shapelessCount++;
                    } else {
                        shapedCount++;
                    }

                    // 调试日志（可选，可以注释掉以减少日志输出）
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
        ExEnigmaticlegacyMod.LOGGER.info("Successfully loaded {} extreme crafting recipes for disassembly ({} shaped, {} shapeless)",
                loadedCount, shapedCount, shapelessCount);
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

        // 不仅检查是否存在，还要检查是否有匹配的配方
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

        // 按非空材料数量降序排序，优先显示复杂配方
        matchedRecipes.sort((r1, r2) ->
                Integer.compare(r2.getNonEmptyIngredientCount(), r1.getNonEmptyIngredientCount()));

        return matchedRecipes;
    }

    /**
     * 获取所有已加载的拆解配方
     */
    public List<ExtremeDeconRecipe> getAllRecipes() {
        if (!isInitialized) {
            loadExtremeCraftingRecipes();
        }

        return new ArrayList<>(extremeRecipeMap.values());
    }

    /**
     * 获取指定类型的配方
     * @param shapeless true为无序配方，false为有序配方
     */
    public List<ExtremeDeconRecipe> getRecipesByType(boolean shapeless) {
        if (!isInitialized) {
            loadExtremeCraftingRecipes();
        }

        return extremeRecipeMap.values().stream()
                .filter(recipe -> recipe.isShapeless() == shapeless)
                .collect(Collectors.toList());
    }

    public int getLoadedRecipeCount() {
        return extremeRecipeMap.size();
    }

    /**
     * 获取已加载的有序配方数量
     */
    public int getShapedRecipeCount() {
        if (!isInitialized) {
            loadExtremeCraftingRecipes();
        }

        return (int) extremeRecipeMap.values().stream()
                .filter(recipe -> !recipe.isShapeless())
                .count();
    }

    /**
     * 获取已加载的无序配方数量
     */
    public int getShapelessRecipeCount() {
        if (!isInitialized) {
            loadExtremeCraftingRecipes();
        }

        return (int) extremeRecipeMap.values().stream()
                .filter(ExtremeDeconRecipe::isShapeless)
                .count();
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