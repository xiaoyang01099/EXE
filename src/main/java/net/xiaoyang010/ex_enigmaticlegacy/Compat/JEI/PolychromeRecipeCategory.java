package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import morph.avaritia.init.AvaritiaModContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec2;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.PolychromeRecipe;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.common.block.ModBlocks;

import javax.annotation.Nonnull;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PolychromeRecipeCategory implements IRecipeCategory<PolychromeRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "polychrome");

    private final Component localizedName;
    private final IDrawable background;
    private final IDrawable overlay;
    private final IDrawable icon;
    private final IDrawable visual3D;

    private static final String[][] STRUCTURE_DATA = {
            {
                    "_______________",
                    "_______________",
                    "_______________",
                    "_______________",
                    "____X_____X____",
                    "_______Z_______",
                    "_______________",
                    "_____Z___Z_____",
                    "_______________",
                    "_______Z_______",
                    "____X_____X____",
                    "_______________",
                    "_______________",
                    "_______________",
                    "_______________"
            },
            {
                    "_______________",
                    "_______________",
                    "_______________",
                    "______J_J______",
                    "____H_____H____",
                    "_______K_______",
                    "___J_______J___",
                    "_____K_P_K_____",
                    "___J_______J___",
                    "_______K_______",
                    "____H_____H____",
                    "______J_J______",
                    "_______________",
                    "_______________",
                    "_______________"
            },
            {
                    "_______G_______",
                    "______OIO______",
                    "_____FIUIF_____",
                    "____FIYTYIF____",
                    "___FIYTETYIF___",
                    "__FIYEWQWEYIF__",
                    "_OIYTWRLRWTYIO_",
                    "GIUTEQL0LQETUIG",
                    "_OIYTWRLRWTYIO_",
                    "__FIYEWQWEYIF__",
                    "___FIYTETYIF___",
                    "____FIYTYIF____",
                    "_____FIUIF_____",
                    "______OIO______",
                    "_______G_______"
            }
    };

    public PolychromeRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(114, 131);

        ResourceLocation overlayPath = new ResourceLocation("botania", "textures/gui/terrasteel_jei_overlay.png");
        this.overlay = guiHelper.createDrawable(overlayPath, 42, 29, 64, 64);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlockss.POLYCHROME_COLLAPSE_PRISM.get()));
        this.localizedName = EComponent.translatable("jei.ex_enigmaticlegacy.polychrome");

        Map<Character, IDrawable> blockMap = new HashMap<>();

        registerBlock(guiHelper, blockMap, 'P', new ItemStack(ModBlockss.POLYCHROME_COLLAPSE_PRISM.get()));
        registerBlock(guiHelper, blockMap, 'R', new ItemStack(ModBlockss.MITHRILL_BLOCK.get()));
        registerBlock(guiHelper, blockMap, '0', new ItemStack(ModBlockss.BLOCKNATURE.get()));
        registerBlock(guiHelper, blockMap, 'L', new ItemStack(ModBlockss.GAIA_BLOCK.get()));
        registerBlock(guiHelper, blockMap, 'Q', new ItemStack(ModBlockss.ARCANE_ICE_CHUNK.get()));
        registerBlock(guiHelper, blockMap, 'W', new ItemStack(AvaritiaModContent.CRYSTAL_MATRIX_STORAGE_BLOCK.get()));
        registerBlock(guiHelper, blockMap, 'E', new ItemStack(ModBlockss.DRAGON_CRYSTALS_BLOCK.get()));
        registerBlock(guiHelper, blockMap, 'T', new ItemStack(AvaritiaModContent.NEUTRONIUM_STORAGE_BLOCK.get()));
        registerBlock(guiHelper, blockMap, 'Y', new ItemStack(ModBlocks.livingrock));
        registerBlock(guiHelper, blockMap, 'U', new ItemStack(Blocks.GLOWSTONE));
        registerBlock(guiHelper, blockMap, 'I', new ItemStack(ModBlockss.INFINITYGlASS.get()));
        registerBlock(guiHelper, blockMap, 'O', new ItemStack(ModBlocks.terrasteelBlock));
        registerBlock(guiHelper, blockMap, 'G', new ItemStack(ModBlocks.dragonstoneBlock));
        registerBlock(guiHelper, blockMap, 'F', new ItemStack(ModBlockss.DECAY_BLOCK.get()));
        registerBlock(guiHelper, blockMap, 'H', new ItemStack(ModBlockss.MANA_CONTAINER.get()));
        registerBlock(guiHelper, blockMap, 'J', new ItemStack(ModBlockss.INFINITY_POTATO.get()));
        registerBlock(guiHelper, blockMap, 'K', new ItemStack(ModBlocks.fabulousPool));
        registerBlock(guiHelper, blockMap, 'Z', new ItemStack(ModBlocks.manaPylon));
        registerBlock(guiHelper, blockMap, 'X', new ItemStack(ModBlocks.naturaPylon));

        this.visual3D = new PolychromeCollapsePrismDrawable(blockMap, STRUCTURE_DATA);
    }

    private void registerBlock(IGuiHelper guiHelper, Map<Character, IDrawable> map, char c, ItemStack stack) {
        map.put(c, guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, stack));
    }

    @Nonnull
    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Nonnull
    @Override
    public Class<? extends PolychromeRecipe> getRecipeClass() {
        return PolychromeRecipe.class;
    }

    @Nonnull
    @Override
    public Component getTitle() {
        return this.localizedName;
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Nonnull
    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(@Nonnull PolychromeRecipe recipe, @Nonnull IRecipeSlotsView view, @Nonnull PoseStack ms, double mouseX, double mouseY) {
        RenderSystem.enableBlend();

        this.overlay.draw(ms, 25, 14);
        this.visual3D.draw(ms, 35, 92);
        int mana = recipe.getManaUsage();
        int maxMana = Math.max(100000, mana * 2);
        HUDHandler.renderManaBar(ms, 6, 126, 0x0000FF, 0.75F, mana, maxMana);

        Font font = Minecraft.getInstance().font;
        String manaString = NumberFormat.getInstance(Locale.US).format(mana);
        int width = font.width(manaString);
        font.draw(ms, manaString, 114 / 2 - width / 2, 121, 0x0000FF);

        RenderSystem.disableBlend();
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull PolychromeRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 48, 37)
                .addItemStack(recipe.getResultItem());

        List<Ingredient> ingredients = recipe.getIngredients();
        int size = ingredients.size();

        if (size > 0) {
            double angleBetweenEach = 360.0 / size;
            Vec2 point = new Vec2(48.0F, 5.0F);
            Vec2 center = new Vec2(48.0F, 37.0F);

            for (Ingredient ingr : ingredients) {
                builder.addSlot(RecipeIngredientRole.INPUT, (int) point.x, (int) point.y)
                        .addIngredients(ingr);

                point = rotatePointAbout(point, center, angleBetweenEach);
            }
        }

        builder.addSlot(RecipeIngredientRole.CATALYST, 48, 92)
                .addItemStack(new ItemStack(ModBlockss.POLYCHROME_COLLAPSE_PRISM.get()));
    }

    private Vec2 rotatePointAbout(Vec2 in, Vec2 about, double degrees) {
        double rad = degrees * Math.PI / 180.0;
        double newX = Math.cos(rad) * (in.x - about.x) - Math.sin(rad) * (in.y - about.y) + about.x;
        double newY = Math.sin(rad) * (in.x - about.x) + Math.cos(rad) * (in.y - about.y) + about.y;
        return new Vec2((float) newX, (float) newY);
    }
}