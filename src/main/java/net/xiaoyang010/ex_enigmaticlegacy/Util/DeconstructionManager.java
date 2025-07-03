package net.xiaoyang010.ex_enigmaticlegacy.Util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
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

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class DeconstructionManager {
    public static DeconstructionManager instance = new DeconstructionManager();
    public static Multimap<String, DeconRecipe> recipeMap = HashMultimap.create();
    private static boolean isInitialized = false;

    public DeconstructionManager() {
    }

    public void loadRecipes() {
        RecipeManager recipeManager = getRecipeManager();
        if (recipeManager == null) return;

        recipeMap.clear();

        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            if (recipe.getType() == RecipeType.CRAFTING) {
                addRecipe(recipe);
            }
        }

        isInitialized = true;
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

    public void addRecipe(Recipe<?> recipe) {
        this.addRecipe(new DeconRecipe(recipe));
    }

    public boolean addRecipe(ItemStack result, ItemStack[] ingredients, int width, int height, boolean shapeless) {
        return this.addRecipe(new DeconRecipe(result, ingredients, width, height, shapeless));
    }

    public boolean addRecipe(DeconRecipe recipe) {
        if (recipe == null || recipe.getResult().isEmpty() || recipe.getIngredients() == null) {
            return false;
        }
        return recipeMap.put(recipe.getResult().getHoverName().getString(), recipe);
    }

    public List<DeconRecipe> getRecipes(ItemStack stack) {
        if (!isInitialized) {
            loadRecipes();
        }

        List<DeconRecipe> filter = new ArrayList<>();
        if (stack != null && !stack.isEmpty()) {
            for(DeconRecipe recipe : new ArrayList<>(recipeMap.get(stack.getHoverName().getString()))) {
                if (recipe.getResult().getCount() <= stack.getCount() &&
                        (!recipe.getResult().isDamageableItem() || recipe.getResult().getDamageValue() == stack.getDamageValue()) &&
                        recipe != null && recipe.getIngredients() != null &&
                        !Arrays.asList(recipe.getIngredients()).contains(recipe.getResult())) {
                    filter.add(recipe);
                }
            }
        }

        filter.sort((o1, o2) -> {
            String s1 = Arrays.stream(o1.getIngredients())
                    .filter(item -> item != null && !item.isEmpty())
                    .map(item -> item.getHoverName().getString())
                    .collect(Collectors.toList()).toString();
            String s2 = Arrays.stream(o2.getIngredients())
                    .filter(item -> item != null && !item.isEmpty())
                    .map(item -> item.getHoverName().getString())
                    .collect(Collectors.toList()).toString();
            return s1.compareTo(s2);
        });

        return filter;
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        instance.loadRecipes();
    }

    @SubscribeEvent
    public static void onResourceReload(AddReloadListenerEvent event) {
        instance.loadRecipes();
    }
}