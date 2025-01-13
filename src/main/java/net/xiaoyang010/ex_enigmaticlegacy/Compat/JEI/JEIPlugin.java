package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.xiaoyang010.ex_enigmaticlegacy.Client.gui.CelestialHTScreen;
import net.xiaoyang010.ex_enigmaticlegacy.Container.CelestialHTMenu;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlocks;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.CelestialTransmuteRecipe;

import java.util.List;
import java.util.Objects;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static final ResourceLocation PLUGIN_ID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "jei_plugin");

    public static final RecipeType<CelestialTransmuteRecipe> CELESTIAL_TRANSMUTE =
            new RecipeType<>(new ResourceLocation(ExEnigmaticlegacyMod.MODID, "celestial_transmute"),
                    CelestialTransmuteRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CelestialTransmuteRecipeCategory(
                registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(CelestialHTScreen.class, 0, 0, 0, 0, CelestialTransmuteRecipe.TYPE_ID);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                CelestialHTMenu.class,
                CelestialTransmuteRecipe.TYPE_ID,
                1,
                4,
                5,
                36
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CELESTIAL_HOLINESS_TRANSMUTER.get()), CelestialTransmuteRecipe.TYPE_ID);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();
        List<CelestialTransmuteRecipe> recipes = recipeManager.getAllRecipesFor(ModRecipes.CHT_TYPE).stream().filter(Objects::nonNull).toList();
        registration.addRecipes(recipes, CelestialTransmuteRecipe.TYPE_ID);
    }
}