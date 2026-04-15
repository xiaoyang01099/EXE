package org.xiaoyang.ex_enigmaticlegacy.Compat.JEI;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.*;
import mezz.jei.library.transfer.BasicRecipeTransferInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fml.ModList;
import org.xiaoyang.ex_enigmaticlegacy.Client.screen.CelestialHTScreen;
import org.xiaoyang.ex_enigmaticlegacy.Client.screen.RainbowTableScreen;
import org.xiaoyang.ex_enigmaticlegacy.Container.CelestialHTMenu;
import org.xiaoyang.ex_enigmaticlegacy.Container.RainbowTableContainer;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModBlocks;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModMenus;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModRecipes;
import org.xiaoyang.ex_enigmaticlegacy.Recipe.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static final ResourceLocation PLUGIN_ID = new ResourceLocation(Exe.MODID, "jei_plugin");

//    public static final mezz.jei.api.recipe.RecipeType<ExtremeCraftingRecipe>
//            EXTREME_CRAFTING_TYPE = new mezz.jei.api.recipe.RecipeType<>(
//            new ResourceLocation("avaritia", "extreme_crafting"),
//            ExtremeCraftingRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new CelestialTransmuteRecipeCategory(
                        registration.getJeiHelpers().getGuiHelper()),
                new RainbowTableCategory(
                        registration.getJeiHelpers().getGuiHelper()),
                new NidavellirCategory(
                        registration.getJeiHelpers().getGuiHelper()),
                new AncientAlphirineCategory(
                        registration.getJeiHelpers().getGuiHelper()),
                new StarlitSanctumCategory(
                        registration.getJeiHelpers().getGuiHelper())
        );

        if (ModList.get().isLoaded("projecte")) {
            registration.addRecipeCategories(
                    new MagicTableRecipeCategory(
                            registration.getJeiHelpers().getGuiHelper())
            );
        }
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(
                CelestialHTScreen.class, 0, 0, 0, 0,
                CelestialTransmuteRecipeCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(
                RainbowTableScreen.class, 99, 51, 35, 5,
                RainbowTableCategory.RECIPE_TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                new BasicRecipeTransferInfo<>(
                        CelestialHTMenu.class,
                        ModMenus.CELESTIAL_HOLINESS_TRANSMUTE.get(),
                        CelestialTransmuteRecipeCategory.RECIPE_TYPE,
                        1, 4,
                        5, 36
                )
        );

        registration.addRecipeTransferHandler(
                new BasicRecipeTransferInfo<>(
                        RainbowTableContainer.class,
                        ModMenus.RAINBOW_TABLE_CONTAINER.get(),
                        RainbowTableCategory.RECIPE_TYPE,
                        0, 4,
                        5, 36
                )
        );
//        registration.addUniversalRecipeTransferHandler(new AvaTransferHandler());
        registration.addRecipeTransferHandler(new StarlitSanctumTransferHandler(registration.getTransferHelper()),
                StarlitSanctumCategory.RECIPE_TYPE);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.CELESTIAL_HOLINESS_TRANSMUTER.get()),
                CelestialTransmuteRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.RAINBOW_TABLE.get()),
                RainbowTableCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.NIDAVELLIR_FORGE.get()),
                NidavellirCategory.RECIPE_TYPE);
//        registration.addRecipeCatalyst(
//                new ItemStack(ModBlocks.EXTREME_AUTO_CRAFTER.get()),
//                EXTREME_CRAFTING_TYPE);
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.STARLIT_SANCTUM.get()),
                StarlitSanctumCategory.RECIPE_TYPE);

        if (ModList.get().isLoaded("projecte") && ModBlocks.MAGIC_TABLE != null) {
            registration.addRecipeCatalyst(
                    new ItemStack(ModBlocks.MAGIC_TABLE.get()),
                    MagicTableRecipeCategory.RECIPE_TYPE);
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Objects.requireNonNull(
                Minecraft.getInstance().level).getRecipeManager();

        List<NidavellirForgeRecipe> nidavellirRecipes =
                recipeManager.getAllRecipesFor(ModRecipes.NIDAVELLIR_FORGE_TYPE.get());
        registration.addRecipes(NidavellirCategory.RECIPE_TYPE, nidavellirRecipes);

        List<CelestialTransmuteRecipe> celestialRecipes =
                recipeManager.getAllRecipesFor(ModRecipes.CHT_TYPE.get())
                        .stream().filter(Objects::nonNull).toList();
        registration.addRecipes(
                CelestialTransmuteRecipeCategory.RECIPE_TYPE, celestialRecipes);

        List<RainbowTableRecipe> rainbowRecipes =
                recipeManager.getAllRecipesFor(ModRecipes.RAINBOW_TABLE_TYPE.get())
                        .stream().filter(Objects::nonNull).toList();
        registration.addRecipes(RainbowTableCategory.RECIPE_TYPE, rainbowRecipes);

        List<AncientAlphirineRecipe> ancientRecipes =
                recipeManager.getAllRecipesFor(ModRecipes.ANCIENT_ALPHIRINE_TYPE.get())
                        .stream().filter(Objects::nonNull).toList();
        registration.addRecipes(
                AncientAlphirineCategory.RECIPE_TYPE, ancientRecipes);

        List<StarlitSanctumRecipe> starlitRecipes = new ArrayList<>(
                recipeManager.getAllRecipesFor(ModRecipes.STARLIT_TYPE.get())
                        .stream().filter(Objects::nonNull).toList());
        registration.addRecipes(StarlitSanctumCategory.RECIPE_TYPE, starlitRecipes);

        if (ModList.get().isLoaded("projecte") && ModRecipes.MAGIC_TABLE_TYPE != null) {
            List<MagicTableRecipe> magicTableRecipes =
                    recipeManager.getAllRecipesFor(ModRecipes.MAGIC_TABLE_TYPE.get())
                            .stream().filter(Objects::nonNull).toList();
            registration.addRecipes(MagicTableRecipeCategory.RECIPE_TYPE, magicTableRecipes);
        }
    }
}