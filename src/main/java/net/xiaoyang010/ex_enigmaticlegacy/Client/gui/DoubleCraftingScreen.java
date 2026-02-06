package net.xiaoyang010.ex_enigmaticlegacy.Client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Container.DoubleCraftingMenu;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class DoubleCraftingScreen extends AbstractContainerScreen<DoubleCraftingMenu> implements RecipeUpdateListener {
    public static final ResourceLocation CRAFTING_TABLE_LOCATION = new ResourceLocation("ex_enigmaticlegacy", "textures/gui/container/double_crafting_table.png");
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
    private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
    private boolean widthTooNarrow;
    protected int imageWidth = 176;
    protected int imageHeight = 220;

    public DoubleCraftingScreen(DoubleCraftingMenu doubleCraftingMenu, Inventory inventory, Component component) {
        super(doubleCraftingMenu, inventory, component);
    }

    protected void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;

        assert this.minecraft != null;

        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        this.addRenderableWidget(new ImageButton(this.leftPos + 5, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (cop) -> {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            ((ImageButton)cop).setPosition(this.leftPos + 5, this.height / 2 - 49);
        }));
        this.addWidget(this.recipeBookComponent);
        this.setInitialFocus(this.recipeBookComponent);
        this.titleLabelX = 8;
        this.titleLabelY = -21;
        this.inventoryLabelY = this.imageHeight - 121;
    }

    public void containerTick() {
        super.containerTick();
        this.recipeBookComponent.tick();
    }

    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
        this.renderBackground(poseStack);
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(poseStack, delta, mouseX, mouseY);
            this.recipeBookComponent.render(poseStack, mouseX, mouseY, delta);
        } else {
            this.recipeBookComponent.render(poseStack, mouseX, mouseY, delta);
            super.render(poseStack, mouseX, mouseY, delta);
            this.recipeBookComponent.renderGhostRecipe(poseStack, this.leftPos, this.topPos, true, delta);
        }

        this.renderTooltip(poseStack, mouseX, mouseY);
        this.recipeBookComponent.renderTooltip(poseStack, this.leftPos, this.topPos, mouseX, mouseY);
    }

    protected void renderBg(@NotNull PoseStack poseStack, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, CRAFTING_TABLE_LOCATION);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(poseStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected boolean isHovering(int p, int k, int l, int o, double i, double u) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(p, k, l, o, i, u);
    }

    public boolean mouseClicked(double a, double b, int c) {
        if (this.recipeBookComponent.mouseClicked(a, b, c)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        } else {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() || super.mouseClicked(a, b, c);
        }
    }

    protected boolean hasClickedOutside(double q, double w, int e, int r, int t) {
        boolean flag = q < (double)e || w < (double)r || q >= (double)(e + this.imageWidth) || w >= (double)(r + this.imageHeight);
        return this.recipeBookComponent.hasClickedOutside(q, w, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, t) && flag;
    }

    protected void slotClicked(@NotNull Slot slot, int s, int d, @NotNull ClickType f) {
        super.slotClicked(slot, s, d, f);
        this.recipeBookComponent.slotClicked(slot);
    }

    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    public @NotNull RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }
}
