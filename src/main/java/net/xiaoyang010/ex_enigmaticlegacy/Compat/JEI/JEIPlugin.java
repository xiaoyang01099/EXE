package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import mezz.jei.common.transfer.BasicRecipeTransferInfo;
import mezz.jei.common.transfer.RecipeTransferHandlerHelper;
import morph.avaritia.api.ExtremeCraftingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import net.xiaoyang010.ex_enigmaticlegacy.Client.gui.CelestialHTScreen;
import net.xiaoyang010.ex_enigmaticlegacy.Client.gui.RainbowTableScreen;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI.AvaritiaJei.AvaTransferHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Container.CelestialHTMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Container.RainbowTableContainer;
import net.xiaoyang010.ex_enigmaticlegacy.Container.StarlitSanctumMenu;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static final ResourceLocation PLUGIN_ID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "jei_plugin");
    public static final RecipeType<ExtremeCraftingRecipe> EXTREME_CRAFTING_TYPE = RecipeType.create("avaritia", "extreme_crafting", ExtremeCraftingRecipe.class);
    public static final RecipeType<CraftingRecipe> DOUBLE_CRAFTING_RECIPE_TYPE = RecipeType.create("ex_enigmaticlegacy", "double_crafting", CraftingRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    //注册jei配方显示
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new PolychromeRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new CelestialTransmuteRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new RainbowTableCategory(registration.getJeiHelpers().getGuiHelper()),
                new NidavellirCategory(registration.getJeiHelpers().getGuiHelper()),
                new AncientAlphirineCategory(registration.getJeiHelpers().getGuiHelper()),
                new DoubleCraftingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new StarlitSanctumCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    //配方显示位置，大小
    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(CelestialHTScreen.class, 0, 0, 0, 0, CelestialTransmuteRecipe.TYPE_ID);
        registration.addRecipeClickArea(RainbowTableScreen.class, 99, 51, 35, 5, RainbowTableRecipe.TYPE_ID);
    }

    //+号
    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(CelestialHTMenu.class, CelestialTransmuteRecipe.TYPE_ID, 1, 4, 5, 36);
        registration.addRecipeTransferHandler(RainbowTableContainer.class, RainbowTableRecipe.TYPE_ID, 0, 4, 5, 36);
//        registration.addRecipeTransferHandler(StarlitSanctumMenu.class, StarlitSanctumCategory.UID, 0, 488, 489, 36);
        registration.addUniversalRecipeTransferHandler(new AvaTransferHandler());
        BasicRecipeTransferInfo<StarlitSanctumMenu, StarlitSanctumRecipe> transferInfo = new BasicRecipeTransferInfo(StarlitSanctumMenu.class, new RecipeType(StarlitSanctumCategory.UID, StarlitSanctumRecipe.class), 0, 488, 489, 36);
        registration.addRecipeTransferHandler(new StarlitSanctumTransferHandler(registration.getTransferHelper()), StarlitSanctumCategory.UID);
//        registration.addRecipeTransferHandler(new IRecipeTransferInfo<StarlitSanctumMenu, StarlitSanctumRecipe>() {
//            @Override
//            public Class<StarlitSanctumMenu> getContainerClass() {
//                return StarlitSanctumMenu.class;
//            }
//            @Override
//            public Class<StarlitSanctumRecipe> getRecipeClass() {
//                return StarlitSanctumRecipe.class;
//            }
//            @Override
//            public ResourceLocation getRecipeCategoryUid() {
//                return StarlitSanctumCategory.UID;
//            }
//            @Override
//            public boolean canHandle(StarlitSanctumMenu container, StarlitSanctumRecipe recipe) {
//                return true;
//            }
//            @Override
//            public List<Slot> getRecipeSlots(StarlitSanctumMenu container, StarlitSanctumRecipe recipe) {
//                List<Slot> slots = new ArrayList<>();
//                slots.add(container.getSlot(486));
//                slots.add(container.getSlot(488));
//                for (int i = 0; i < 486; i++) {
//                    slots.add(container.getSlot(i));
//                }
//                return slots;
//            }
//            @Override
//            public List<Slot> getInventorySlots(StarlitSanctumMenu container, StarlitSanctumRecipe recipe) {
//                List<Slot> slots = new ArrayList<>();
//                for (int i = 489; i < container.slots.size(); i++) {
//                    slots.add(container.getSlot(i));
//                }
//
//                return slots;
//            }
//        });
    }

    //用合成方块查找配方
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlockss.CELESTIAL_HOLINESS_TRANSMUTER.get()),
                CelestialTransmuteRecipe.TYPE_ID);
        registration.addRecipeCatalyst(new ItemStack(ModBlockss.RAINBOW_TABLE.get()),
                RainbowTableRecipe.TYPE_ID);
        registration.addRecipeCatalyst(new ItemStack(ModBlockss.POLYCHROME_COLLAPSE_PRISM.get()), PolychromeRecipeCategory.UID);
        registration.addRecipeCatalyst(new ItemStack(ModBlockss.NIDAVELLIR_FORGE.get()), NidavellirCategory.UID);
        registration.addRecipeCatalyst(new ItemStack(ModBlockss.EXTREME_AUTO_CRAFTER.get()), EXTREME_CRAFTING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlockss.STARLIT_SANCTUM.get()), StarlitSanctumCategory.UID);
    }

    //配方查看
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();

        List<NidavellirForgeRecipe> nidavellirForgeRecipes = recipeManager.getAllRecipesFor(ModRecipes.NIDAVELLIR_FORGE_TYPE);
        registration.addRecipes(nidavellirForgeRecipes, NidavellirCategory.UID);

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
        registration.addRecipes(polychromeRecipes, PolychromeRecipeCategory.UID);

        List<StarlitSanctumRecipe> starlitRecipes = new ArrayList<>(
                recipeManager.getAllRecipesFor(ModRecipes.STARLIT_TYPE)
                        .stream().filter(Objects::nonNull).toList()
        );
        registration.addRecipes(starlitRecipes, StarlitSanctumCategory.UID);

        ClientLevel lvl = Minecraft.getInstance().level;
        if (lvl != null) {
            RecipeManager rm = lvl.getRecipeManager();
            List<CraftingRecipe> recipes = new ArrayList<>();
            for (CraftingRecipe recipe : rm.getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING)) {
                if (recipe instanceof IShapedRecipe && !(recipe instanceof ExtremeCraftingRecipe)) {
                    IShapedRecipe<?> shapedRecipe = (IShapedRecipe) recipe;
                    if (shapedRecipe.getRecipeWidth() <= 3 && shapedRecipe.getRecipeHeight() > 3 && shapedRecipe.getRecipeHeight() <= 6 ||
                            Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(shapedRecipe.getResultItem().getItem())).getNamespace().equals("double_crafting")) {
                        recipes.add(recipe);
                    }
                }
            }
            registration.addRecipes(DOUBLE_CRAFTING_RECIPE_TYPE, recipes);
        }
    }
}