package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.xiaoyang010.ex_enigmaticlegacy.Client.gui.CelestialHTScreen;
import net.xiaoyang010.ex_enigmaticlegacy.Client.gui.RainbowTableScreen;
import net.xiaoyang010.ex_enigmaticlegacy.Container.CelestialHTMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Container.RainbowTableContainer;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlocks;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.CelestialTransmuteRecipe;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.RainbowTableRecipe;

import java.util.List;
import java.util.Objects;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static final ResourceLocation PLUGIN_ID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "jei_plugin");

    public static final RecipeType<CelestialTransmuteRecipe> CELESTIAL_TRANSMUTE =
            new RecipeType<>(new ResourceLocation(ExEnigmaticlegacyMod.MODID, "celestial_transmute"),
                    CelestialTransmuteRecipe.class);

    public static final RecipeType<RainbowTableRecipe> RAINBOW_TABLE =
            new RecipeType<>(RainbowTableRecipe.TYPE_ID, RainbowTableRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new CelestialTransmuteRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new RainbowTableCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(CelestialHTScreen.class, 0, 0, 0, 0, CelestialTransmuteRecipe.TYPE_ID);
        registration.addRecipeClickArea(RainbowTableScreen.class, 99, 51, 35, 5, RainbowTableRecipe.TYPE_ID);
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
        registration.addRecipeTransferHandler(
                RainbowTableContainer.class,
                RainbowTableRecipe.TYPE_ID,
                0,
                4,
                5,
                36
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CELESTIAL_HOLINESS_TRANSMUTER.get()),
                CelestialTransmuteRecipe.TYPE_ID);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.RAINBOW_TABLE.get()),
                RainbowTableRecipe.TYPE_ID);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();

        List<CelestialTransmuteRecipe> celestialRecipes = recipeManager.getAllRecipesFor(ModRecipes.CHT_TYPE)
                .stream().filter(Objects::nonNull).toList();
        registration.addRecipes(celestialRecipes, CelestialTransmuteRecipe.TYPE_ID);

        List<RainbowTableRecipe> rainbowRecipes = recipeManager.getAllRecipesFor(ModRecipes.RAINBOW_TABLE_TYPE)
                .stream().filter(Objects::nonNull).toList();
        registration.addRecipes(rainbowRecipes, RainbowTableRecipe.TYPE_ID);

        registration.addIngredientInfo(
                new ItemStack(ModItems.ASTRAL_PILE.get()),
                VanillaTypes.ITEM_STACK,
                new TranslatableComponent("item.ex_enigmaticlegacy.astral_pile.desc")
        );
        registration.addIngredientInfo(
                new ItemStack(ModItems.DEAD_SUBSTANCE.get()),
                VanillaTypes.ITEM_STACK,
                new TranslatableComponent("item.ex_enigmaticlegacy.dead_substance.desc")
        );
    }
}