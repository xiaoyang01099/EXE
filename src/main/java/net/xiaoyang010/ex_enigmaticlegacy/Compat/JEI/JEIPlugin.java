package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import morph.avaritia.api.ExtremeCraftingRecipe;
import morph.avaritia.init.AvaritiaModContent;
import morph.avaritia.recipe.ExtremeShapedRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.xiaoyang010.ex_enigmaticlegacy.Client.gui.CelestialHTScreen;
import net.xiaoyang010.ex_enigmaticlegacy.Client.gui.RainbowTableScreen;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI.AvaritiaJei.AvaTransferHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI.AvaritiaJei.ExtremeAutoTransferInfo;
import net.xiaoyang010.ex_enigmaticlegacy.Container.CelestialHTMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Container.RainbowTableContainer;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.*;

import java.util.List;
import java.util.Objects;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static final ResourceLocation PLUGIN_ID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "jei_plugin");
    public static final RecipeType<ExtremeCraftingRecipe> EXTREME_CRAFTING_TYPE = RecipeType.create("avaritia", "extreme_crafting", ExtremeCraftingRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    //注册jei配方显示
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new PolychromeCollapsePrismRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new CelestialTransmuteRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new RainbowTableCategory(registration.getJeiHelpers().getGuiHelper()),
                new NidavellirCategory(registration.getJeiHelpers().getGuiHelper()),
//                new AvaCategory(registration.getJeiHelpers().getGuiHelper()),
                new AncientAlphirineCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    //配方显示位置，大小
    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(CelestialHTScreen.class, 0, 0, 0, 0, CelestialTransmuteRecipe.TYPE_ID);
        registration.addRecipeClickArea(RainbowTableScreen.class, 99, 51, 35, 5, RainbowTableRecipe.TYPE_ID);
//        registration.addRecipeClickArea(GuiExtremeAutoCrafter.class, 175, 79, 28, 26, AvaritiaJEIPlugin.EXTREME_CRAFTING_TYPE.getUid());
    }

    //+号
    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(CelestialHTMenu.class, CelestialTransmuteRecipe.TYPE_ID, 1, 4, 5, 36);
        registration.addRecipeTransferHandler(RainbowTableContainer.class, RainbowTableRecipe.TYPE_ID, 0, 4, 5, 36);
        registration.addUniversalRecipeTransferHandler(new AvaTransferHandler());
//        registration.addRecipeTransferHandler(ContainerExtremeAutoCrafter.class, new RecipeType<>(AvaritiaModContent.EXTREME_CRAFTING_RECIPE_TYPE.getId(), ExtremeShapedRecipe.class), 81, 81, 163, 36);
    }

    //用合成方块查找配方
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlockss.CELESTIAL_HOLINESS_TRANSMUTER.get()),
                CelestialTransmuteRecipe.TYPE_ID);
        registration.addRecipeCatalyst(new ItemStack(ModBlockss.RAINBOW_TABLE.get()),
                RainbowTableRecipe.TYPE_ID);
        registration.addRecipeCatalyst(new ItemStack(ModBlockss.POLYCHROME_COLLAPSE_PRISM.get()), PolychromeCollapsePrismRecipeCategory.UID);
        registration.addRecipeCatalyst(new ItemStack(ModBlockss.NIDAVELLIR_FORGE.get()), NidavellirCategory.UID);
        registration.addRecipeCatalyst(new ItemStack(ModBlockss.EXTREME_AUTO_CRAFTER.get()), EXTREME_CRAFTING_TYPE);
    }

    //配方查看
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();

        List<NidavellirForgeRecipe> nidavellirForgeRecipes = recipeManager.getAllRecipesFor(ModRecipes.NIDAVELLIR_FORGE_TYPE);
        registration.addRecipes(nidavellirForgeRecipes, NidavellirCategory.UID);

//        List<ExtremeCraftingRecipe> aaa = recipeManager.getAllRecipesFor(AvaritiaModContent.EXTREME_CRAFTING_RECIPE_TYPE.get());
//        registration.addRecipes(aaa, EXTREME_CRAFTING_TYPE);

        List<CelestialTransmuteRecipe> celestialRecipes = recipeManager.getAllRecipesFor(ModRecipes.CHT_TYPE)
                .stream().filter(Objects::nonNull).toList();
        registration.addRecipes(celestialRecipes, CelestialTransmuteRecipe.TYPE_ID);

        List<RainbowTableRecipe> rainbowRecipes = recipeManager.getAllRecipesFor(ModRecipes.RAINBOW_TABLE_TYPE)
                .stream().filter(Objects::nonNull).toList();
        registration.addRecipes(rainbowRecipes, RainbowTableRecipe.TYPE_ID);

        List<AncientAlphirineRecipe> ancientAlphirineRecipes = recipeManager.getAllRecipesFor(ModRecipes.ANCIENT_ALPHIRINE_TYPE)
                .stream().filter(Objects::nonNull).toList();
        registration.addRecipes(ancientAlphirineRecipes, AncientAlphirineRecipe.TYPE_ID);

        List<PolychromeRecipe> polychromeRecipes = recipeManager.getAllRecipesFor(ModRecipes.POLYCHROME_TYPE)
                .stream().filter(Objects::nonNull).toList();
        registration.addRecipes(polychromeRecipes, PolychromeCollapsePrismRecipeCategory.UID);

    }
}