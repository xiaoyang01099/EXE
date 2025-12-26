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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec2;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.PolychromeRecipe;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.client.integration.jei.PetalApothecaryRecipeCategory;
import vazkii.botania.client.integration.jei.TerraPlateDrawable;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.lib.ResourceLocationHelper;

import javax.annotation.Nonnull;

import java.util.Iterator;


public class PolychromeCollapsePrismRecipeCategory implements IRecipeCategory<PolychromeRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "polychrome");

    private final Component localizedName;
    private final IDrawable background;
    private final IDrawable overlay;
    private final IDrawable icon;
    private final IDrawable terraPlate;

    public PolychromeCollapsePrismRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = ResourceLocationHelper.prefix("textures/gui/terrasteel_jei_overlay.png");
        this.background = guiHelper.createBlankDrawable(114, 131);
        this.overlay = guiHelper.createDrawable(location, 42, 29, 64, 64);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlockss.POLYCHROME_COLLAPSE_PRISM.get()));
        this.localizedName = new TranslatableComponent("botania.nei.terraPlate");
        IDrawable livingrock = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlockss.PLATINUM_ORE.get()));
        this.terraPlate = new TerraPlateDrawable(livingrock, livingrock, guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlockss.AMETHYST_ORE.get())));
    }

    @Nonnull
    public ResourceLocation getUid() {
        return UID;
    }

    @Nonnull
    public Class<? extends PolychromeRecipe> getRecipeClass() {
        return PolychromeRecipe.class;
    }

    @Nonnull
    public Component getTitle() {
        return this.localizedName;
    }

    @Nonnull
    public IDrawable getBackground() {
        return this.background;
    }

    @Nonnull
    public IDrawable getIcon() {
        return this.icon;
    }

    public void draw(@Nonnull PolychromeRecipe recipe, @Nonnull IRecipeSlotsView view, @Nonnull PoseStack ms, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        this.overlay.draw(ms, 25, 14);
        HUDHandler.renderManaBar(ms, 6, 126, 255, 0.75F, recipe.getManaUsage(), 100000);
        this.terraPlate.draw(ms, 35, 92);
        RenderSystem.disableBlend();
    }

    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull PolychromeRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 48, 37).addItemStack(recipe.getResultItem());
        double angleBetweenEach = 360.0 / (double)recipe.getIngredients().size();
        Vec2 point = new Vec2(48.0F, 5.0F);
        Vec2 center = new Vec2(48.0F, 37.0F);

        for(Iterator<Ingredient> var8 = recipe.getIngredients().iterator(); var8.hasNext(); point = PetalApothecaryRecipeCategory.rotatePointAbout(point, center, angleBetweenEach)) {
            Ingredient ingr = var8.next();
            builder.addSlot(RecipeIngredientRole.INPUT, (int)point.x, (int)point.y).addIngredients(ingr);
        }

        builder.addSlot(RecipeIngredientRole.CATALYST, 48, 92).addItemStack(new ItemStack(ModBlockss.POLYCHROME_COLLAPSE_PRISM.get()));
    }
}