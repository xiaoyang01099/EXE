package net.xiaoyang010.ex_enigmaticlegacy.Client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ContainerExtremeAutoCrafter;
import org.jetbrains.annotations.NotNull;

import static net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod.MODID;

public class GuiExtremeAutoCrafter extends AbstractContainerScreen<ContainerExtremeAutoCrafter> {
    private static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(MODID, "textures/gui/extreme_auto_crafter.png");

    public GuiExtremeAutoCrafter(ContainerExtremeAutoCrafter container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.imageWidth = 343;
        this.imageHeight = 276;
        this.titleLabelY = 4;
        this.inventoryLabelY = 194;
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, RESOURCE_LOCATION);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        GuiComponent.blit(poseStack, i, j, this.blitOffset, 0, 0, this.imageWidth, this.imageHeight, 343, 343);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

}