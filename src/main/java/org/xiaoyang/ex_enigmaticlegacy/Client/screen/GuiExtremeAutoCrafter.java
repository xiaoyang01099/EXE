package org.xiaoyang.ex_enigmaticlegacy.Client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.xiaoyang.ex_enigmaticlegacy.Container.ContainerExtremeAutoCrafter;
import org.xiaoyang.ex_enigmaticlegacy.Exe;


public class GuiExtremeAutoCrafter extends AbstractContainerScreen<ContainerExtremeAutoCrafter> {
    private static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(Exe.MODID, "textures/gui/extreme_auto_crafter.png");

    public GuiExtremeAutoCrafter(ContainerExtremeAutoCrafter container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.imageWidth = 343;
        this.imageHeight = 276;
        this.titleLabelY = 4;
        this.inventoryLabelY = 194;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, RESOURCE_LOCATION);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RESOURCE_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight, 343, 343);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}