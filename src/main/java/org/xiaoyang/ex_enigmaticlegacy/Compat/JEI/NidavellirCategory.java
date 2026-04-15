package org.xiaoyang.ex_enigmaticlegacy.Compat.JEI;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec2;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModBlocks;
import org.xiaoyang.ex_enigmaticlegacy.Recipe.NidavellirForgeRecipe;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.client.integration.jei.PetalApothecaryRecipeCategory;

import javax.annotation.Nonnull;
import java.util.Iterator;

public class NidavellirCategory implements IRecipeCategory<NidavellirForgeRecipe> {
    public static final ResourceLocation UID =
            new ResourceLocation(Exe.MODID, "nidavellir_forge");
    public static final RecipeType<NidavellirForgeRecipe> RECIPE_TYPE =
            new RecipeType<>(UID, NidavellirForgeRecipe.class);

    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable overlay;

    public NidavellirCategory(IGuiHelper helper) {
        this.title = Component.translatable(
                "gui." + Exe.MODID + ".category.nidavellir_forge");
        this.background = helper.createBlankDrawable(114, 104);
        this.overlay = helper.createDrawable(
                new ResourceLocation(Exe.MODID, "textures/gui/petal_overlay.png"),
                17, 11, 114, 82);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.NIDAVELLIR_FORGE.get()));
    }

    @Override
    public RecipeType<NidavellirForgeRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(NidavellirForgeRecipe recipe, @Nonnull IRecipeSlotsView slotsView, @Nonnull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        this.overlay.draw(guiGraphics, 0, 4);
        HUDHandler.renderManaBar(guiGraphics, 6, 98, 255, 0.75F,
                recipe.getManaUsage(), 100000);
        RenderSystem.disableBlend();
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull NidavellirForgeRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 48, 45)
                .addItemStack(new ItemStack(ModBlocks.NIDAVELLIR_FORGE.get()));

        double angleBetweenEach = 360.0 / recipe.getIngredients().size();
        Vec2 point = new Vec2(48.0F, 13.0F);
        Vec2 center = new Vec2(48.0F, 45.0F);

        for (Iterator<Ingredient> iter = recipe.getIngredients().iterator();
             iter.hasNext();
             point = PetalApothecaryRecipeCategory.rotatePointAbout(
                     point, center, angleBetweenEach)) {
            Ingredient ingr = iter.next();
            builder.addSlot(RecipeIngredientRole.INPUT,
                    (int) point.x, (int) point.y).addIngredients(ingr);
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 86, 10)
                .addItemStack(recipe.getResultItem(
                        net.minecraft.core.RegistryAccess.EMPTY));
    }
}