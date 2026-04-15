package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Hud.TChest;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiItemChest extends AbstractContainerScreen<ContainerItemChest> {
    private static final ResourceLocation CHEST_GUI_TEXTURE =
            new ResourceLocation("textures/gui/container/generic_54.png");
    private final int inventoryRows;

    public GuiItemChest(ContainerItemChest container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        int baseHeight = 222;
        int adjustment = baseHeight - 108;
        this.inventoryRows = container.itemChestInv.getContainerSize() / 9;
        this.imageHeight = adjustment + this.inventoryRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode >= 49 && keyCode <= 57) return false;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(CHEST_GUI_TEXTURE, x, y, 0, 0, this.imageWidth, this.inventoryRows * 18 + 17);
        guiGraphics.blit(CHEST_GUI_TEXTURE, x, y + this.inventoryRows * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}