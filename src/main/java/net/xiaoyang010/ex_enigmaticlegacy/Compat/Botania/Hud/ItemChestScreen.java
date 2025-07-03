package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ItemChestScreen extends AbstractContainerScreen<ItemChestContainer> {

    private static final ResourceLocation CHEST_GUI_TEXTURE =
            new ResourceLocation("textures/gui/container/generic_54.png");

    private final int inventoryRows;

    public ItemChestScreen(ItemChestContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);

        InventoryItemChest itemChestInv = container.itemChestInv;
        this.inventoryRows = itemChestInv.getContainerSize() / 9;
        this.imageHeight = 114 + this.inventoryRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(poseStack, this.title, 8.0F, 6.0F, 4210752);
        this.font.draw(poseStack, this.playerInventoryTitle, 8.0F, (float)(this.imageHeight - 96 + 2), 4210752);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CHEST_GUI_TEXTURE);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.blit(poseStack, x, y, 0, 0, this.imageWidth, this.inventoryRows * 18 + 17);
        this.blit(poseStack, x, y + this.inventoryRows * 18 + 17, 0, 126, this.imageWidth, 96);
    }
}