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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Container.PagedChestContainer;



@OnlyIn(Dist.CLIENT)
public class PagedChestScreen extends AbstractContainerScreen<PagedChestContainer> {
    private static final ResourceLocation CHEST_GUI = new ResourceLocation("ex_enigmaticlegacy", "textures/gui/container/multipage_chest_gui.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 256;

    private Button nextButton;
    private Button prevButton;

    public PagedChestScreen(PagedChestContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.imageHeight = GUI_HEIGHT;
        this.imageWidth = GUI_WIDTH;
        this.inventoryLabelY = this.imageHeight - 84;
    }

    @Override
    protected void init() {
        super.init();

        // 添加导航按钮
        int buttonWidth = 20;
        int buttonHeight = 20;
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.prevButton = this.addRenderableWidget(new Button(
                x - buttonWidth - 4, y + 20,
                buttonWidth, buttonHeight,
                new TextComponent("<"),
                button -> this.menu.previousPage()
        ));

        this.nextButton = this.addRenderableWidget(new Button(
                x + this.imageWidth + 4, y + 20,
                buttonWidth, buttonHeight,
                new TextComponent(">"),
                button -> this.menu.nextPage()
        ));

        updateButtonStates();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);

        String pageText = String.format("Page %d/%d",
                this.menu.getCurrentPage() + 1,
                this.menu.getTotalPages()
        );

        this.font.draw(
                poseStack,
                pageText,
                (this.width - this.font.width(pageText)) / 2f,
                this.topPos - 10,
                0x404040
        );

        updateButtonStates();
    }

    private void updateButtonStates() {
        this.prevButton.active = this.menu.getCurrentPage() > 0;
        this.nextButton.active = this.menu.getCurrentPage() < this.menu.getTotalPages() - 1;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CHEST_GUI);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.blit(poseStack, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }
}