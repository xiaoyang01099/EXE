package net.xiaoyang010.ex_enigmaticlegacy.Util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.DeconRecipe;
import morph.avaritia.api.ExtremeCraftingRecipe;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class ExtremeCraftingDeconstructionManager {
    public static ExtremeCraftingDeconstructionManager instance = new ExtremeCraftingDeconstructionManager();
    public static Multimap<String, DeconRecipe> extremeRecipeMap = HashMultimap.create();
    private static boolean isInitialized = false;

    public ExtremeCraftingDeconstructionManager() {
    }

    public void loadExtremeCraftingRecipes() {
        RecipeManager recipeManager = getRecipeManager();
        if (recipeManager == null) return;

        extremeRecipeMap.clear();
            for (Recipe<?> recipe : recipeManager.getRecipes()) {
                if (recipe instanceof ExtremeCraftingRecipe) {
                    ExtremeCraftingRecipe extremeRecipe = (ExtremeCraftingRecipe) recipe;
                    addExtremeCraftingRecipe(extremeRecipe);
                }
            }

        isInitialized = true;
        ExEnigmaticlegacyMod.LOGGER.info("Loaded {} extreme crafting recipes for disassembly", extremeRecipeMap.size());
    }

    public RecipeManager getRecipeManager() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.getRecipeManager();
        }

        if (Minecraft.getInstance() != null && Minecraft.getInstance().level != null) {
            return Minecraft.getInstance().level.getRecipeManager();
        }

        return null;
    }

    public void addExtremeCraftingRecipe(ExtremeCraftingRecipe recipe) {
            DeconRecipe deconRecipe = createExtremeDeconRecipe(recipe);
            if (deconRecipe != null) {
                extremeRecipeMap.put(deconRecipe.getResult().getHoverName().getString(), deconRecipe);
            }
    }

    private DeconRecipe createExtremeDeconRecipe(ExtremeCraftingRecipe recipe) {
        try {
            ItemStack result = recipe.getResultItem();
            if (result.isEmpty()) return null;

            var ingredients = recipe.getIngredients();
            if (ingredients.isEmpty()) return null;

            ItemStack[] extremeIngredients = new ItemStack[81]; // 9x9 = 81

            for (int i = 0; i < Math.min(ingredients.size(), 81); i++) {
                var ingredient = ingredients.get(i);
                if (!ingredient.isEmpty()) {
                    ItemStack[] items = ingredient.getItems();
                    if (items.length > 0) {
                        extremeIngredients[i] = items[0].copy();
                        extremeIngredients[i].setCount(1);
                    } else {
                        extremeIngredients[i] = ItemStack.EMPTY;
                    }
                } else {
                    extremeIngredients[i] = ItemStack.EMPTY;
                }
            }

            for (int i = ingredients.size(); i < 81; i++) {
                extremeIngredients[i] = ItemStack.EMPTY;
            }

            return new DeconRecipe(result, extremeIngredients, 9, 9, false);

        } catch (Exception e) {
            ExEnigmaticlegacyMod.LOGGER.error("Error creating extreme decon recipe", e);
            return null;
        }
    }

    public boolean hasExtremeCraftingRecipe(ItemStack stack) {
        if (!isInitialized) {
            loadExtremeCraftingRecipes();
        }

        if (stack.isEmpty()) return false;

        String itemName = stack.getHoverName().getString();
        return extremeRecipeMap.containsKey(itemName);
    }

    public List<DeconRecipe> getExtremeCraftingRecipes(ItemStack stack) {
        if (!isInitialized) {
            loadExtremeCraftingRecipes();
        }

        List<DeconRecipe> filter = new ArrayList<>();
        if (stack != null && !stack.isEmpty()) {
            String itemName = stack.getHoverName().getString();
            for(DeconRecipe recipe : new ArrayList<>(extremeRecipeMap.get(itemName))) {
                if (recipe.getResult().getCount() <= stack.getCount() &&
                        (!recipe.getResult().isDamageableItem() || recipe.getResult().getDamageValue() == stack.getDamageValue()) &&
                        recipe != null && recipe.getIngredients() != null &&
                        !Arrays.asList(recipe.getIngredients()).contains(recipe.getResult()) &&
                        recipe.width == 9 && recipe.height == 9) { // 确保是9x9配方
                    filter.add(recipe);
                }
            }
        }

        filter.sort((o1, o2) -> {
            long nonEmptyCount1 = Arrays.stream(o1.getIngredients())
                    .filter(item -> item != null && !item.isEmpty())
                    .count();
            long nonEmptyCount2 = Arrays.stream(o2.getIngredients())
                    .filter(item -> item != null && !item.isEmpty())
                    .count();
            return Long.compare(nonEmptyCount2, nonEmptyCount1);
        });

        return filter;
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        instance.loadExtremeCraftingRecipes();
    }

    @SubscribeEvent
    public static void onResourceReload(AddReloadListenerEvent event) {
        instance.loadExtremeCraftingRecipes();
    }
}