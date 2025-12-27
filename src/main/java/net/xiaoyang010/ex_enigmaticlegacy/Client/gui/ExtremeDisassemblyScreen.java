package net.xiaoyang010.ex_enigmaticlegacy.Client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ExtremeDisassemblyMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.EXPacketIndex;
import net.xiaoyang010.ex_enigmaticlegacy.Util.ExtremeCraftingDeconstructionManager;
import java.util.List;

public class ExtremeDisassemblyScreen extends AbstractContainerScreen<ExtremeDisassemblyMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            "ex_enigmaticlegacy", "textures/gui/container/extreme_disassembly.png");

    private DeconButton nextButton;
    private DeconButton backButton;
    private int recipeIndex;

    public ExtremeDisassemblyScreen(ExtremeDisassemblyMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 256;
        this.recipeIndex = 0;
    }

    @Override
    protected void init() {
        super.init();

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.addRenderableWidget(this.nextButton = new DeconButton(
                x + 230, y + 170, true, btn -> handleButtonClick(btn)));
        this.addRenderableWidget(this.backButton = new DeconButton(
                x + 30, y + 170, false, btn -> handleButtonClick(btn)));

        this.nextButton.active = false;
        this.backButton.active = false;
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.blit(poseStack, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        updateButtons();
    }

    private void updateButtons() {
        List<?> recipes = ExtremeCraftingDeconstructionManager.instance
                .getExtremeCraftingRecipes(this.menu.slots.get(0).getItem());

        if (recipes.isEmpty()) {
            this.recipeIndex = 0;
            this.nextButton.active = false;
            this.backButton.active = false;
        } else {
            this.nextButton.active = this.recipeIndex < recipes.size() - 1;
            this.backButton.active = this.recipeIndex > 0;
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    private void handleButtonClick(Button button) {
        if (button == this.nextButton) {
            this.recipeIndex++;
        } else if (button == this.backButton) {
            this.recipeIndex--;
        } else {
            return;
        }

        NetworkHandler.CHANNEL.sendToServer(new EXPacketIndex(this.recipeIndex));
        this.menu.setRecipeIndex(this.recipeIndex);
    }

    public static class DeconButton extends Button {
        private final boolean isNext;

        public DeconButton(int x, int y, boolean isNext, OnPress onPress) {
            super(x, y, 12, 19, TextComponent.EMPTY, onPress);
            this.isNext = isNext;
        }

        @Override
        public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            boolean hover = mouseX >= this.x && mouseY >= this.y &&
                    mouseX < this.x + this.width && mouseY < this.y + this.height;

            int u = 256;
            int v = isNext ? 0 : 19;

            if (!this.active) u += 24;
            else if (hover) u += 12;

            blit(poseStack, this.x, this.y, u, v, this.width, this.height);
        }
    }
}