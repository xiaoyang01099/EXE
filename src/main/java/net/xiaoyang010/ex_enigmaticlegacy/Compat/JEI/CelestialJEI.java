/*package net.xiaoyang010.ex_enigmaticlegacy.compat.JEI;


import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.recipe.CelestialTransmuteRecipe;


public class CelestialJEI implements IRecipeCategory<CelestialTransmuteRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "celestial_transmute");
    private final Component title;
    private final ResourceLocation background;

    public CelestialJEI(IGuiHelper guiHelper) {
        this.title = new TranslatableComponent("gui.ex_enigmaticlegacy.celestial_transmute");
        this.background = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/gui/celestial_transmute_jei.png");
    }

    @Override
    public RecipeType<CelestialTransmuteRecipe> getRecipeType() {
        return RecipeType.create(ExEnigmaticlegacyMod.MODID, "celestial_transmute", CelestialTransmuteRecipe.class);
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public IDrawable getBackground() {
        return null;
    }

    @Override
    public IDrawable getIcon() {
        return (IDrawable) ModItems.CELESTIAL_HOLINESS_TRANSMUTER.get();
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends CelestialTransmuteRecipe> getRecipeClass() {
        return null;
    }

    @Override
    public void draw(CelestialTransmuteRecipe recipe, PoseStack poseStack, double mouseX, double mouseY) {
        RenderSystem.setShaderTexture(0, background);
        blit(poseStack, 0, 0, 0, 0, 160, 60);
    }

    private void blit(PoseStack poseStack, int i, int i1, int i2, int i3, int i4, int i5) {
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CelestialTransmuteRecipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, 20 + i * 20, 20);
            slot.addIngredients(recipe.getIngredients().get(i));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 140, 20).addItemStack(recipe.getResultItem());
    }
}*/