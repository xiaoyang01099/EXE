package org.xiaoyang.ex_enigmaticlegacy.Client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xiaoyang.ex_enigmaticlegacy.Container.SpectriteChestContainer;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.ScrollPagePacket;
import org.xiaoyang.ex_enigmaticlegacy.Tile.SpectriteChestTile;

@OnlyIn(Dist.CLIENT)
public class SpectriteChestScreen extends AbstractContainerScreen<SpectriteChestContainer> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private final SpectriteChestTile blockEntity;
    private final BlockPos blockPos;
    private int displayPage = 0;

    public SpectriteChestScreen(SpectriteChestContainer menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.blockEntity = menu.getBlockEntity();
        this.blockPos = blockEntity.getBlockPos();
        this.imageHeight = 114 + 3 * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.displayPage = blockEntity.getCurrentPage();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        renderPageInfo(graphics);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, 3 * 18 + 17);
        graphics.blit(TEXTURE, x, y + 3 * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    private void renderPageInfo(GuiGraphics graphics) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        String pageText = "Page: " + (displayPage + 1);
        graphics.drawString(this.font, pageText,
                x + this.imageWidth - this.font.width(pageText) - 8,
                y + 6, 0x404040, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isMouseOverChestArea(mouseX, mouseY)) {
            int scrollDelta = delta < 0 ? 1 : -1;
            int newPage = Math.max(0,
                    Math.min(displayPage + scrollDelta, SpectriteChestTile.MAX_PAGE));

            if (newPage != displayPage) {
                displayPage = newPage;
                NetworkHandler.CHANNEL.sendToServer(
                        new ScrollPagePacket(blockPos, scrollDelta));
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    public void syncPage(int serverPage) {
        this.displayPage = serverPage;
        blockEntity.setCurrentPageClient(serverPage);
    }

    private boolean isMouseOverChestArea(double mouseX, double mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        return mouseX >= x && mouseX <= x + this.imageWidth
                && mouseY >= y && mouseY <= y + 3 * 18 + 17;
    }

    @Override
    public void containerTick() {
        super.containerTick();
    }
}