package org.xiaoyang.ex_enigmaticlegacy.Client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.xiaoyang.ex_enigmaticlegacy.Container.NeutroniumDecompressorMenu;
import org.xiaoyang.ex_enigmaticlegacy.Recipe.Manager.DecompressorManager;
import org.xiaoyang.ex_enigmaticlegacy.Tile.NeutroniumDecompressorTile;

public class NeutroniumDecompressorScreen extends AbstractContainerScreen<NeutroniumDecompressorMenu> {
    public static final ResourceLocation BACKGROUND = new ResourceLocation("ex_enigmaticlegacy", "textures/gui/container/neutronium_decompressor.png");
    public static final Component TITLE = Component.translatable("container.ex_enigmaticlegacy.neutronium_decompressor");

    public NeutroniumDecompressorScreen(NeutroniumDecompressorMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, TITLE);
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        this.titleLabelX = this.imageWidth / 2 - this.font.width(this.title) / 2;
        super.renderLabels(guiGraphics, mouseX, mouseY);

        NeutroniumDecompressorTile tile = this.menu.getTile();
        int progress = tile.getProgress();
        int maxProgress = tile.getMaxProgress();

        if (maxProgress > 0) {
            String progressText = String.format("%.2f%%",
                    100.0F * ((float) progress / (float) maxProgress));

            int textX = this.imageWidth / 2 - this.font.width(progressText) / 2;
            guiGraphics.drawString(this.font, progressText, textX, 60, 0x404040, false);
        }

        renderDecompressInfo(guiGraphics);
    }

    private void renderDecompressInfo(GuiGraphics guiGraphics) {
        DecompressorManager.DecompressRecipeData recipe = this.menu.getTile().getCurrentRecipe();

        if (recipe != null) {
            String info = "1x → " + recipe.getCount() + "x";
            int infoX = this.imageWidth / 2 - this.font.width(info) / 2;
            guiGraphics.drawString(this.font, info, infoX, 51, 0x808080, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(BACKGROUND, x, y, 0, 0, this.imageWidth, this.imageHeight);

        renderProgressArrow(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        int progress = menu.getProgress();
        int maxProgress = menu.getMaxProgress();

        if (maxProgress > 0 && progress > 0) {
            int arrowWidth = (int) Math.floor(progress * 16.0f / maxProgress);
            int w = (int) Math.floor(progress * 22.0f / maxProgress);
            guiGraphics.blit(BACKGROUND, x + 62, y + 35, 176, 0, w, 16);
            guiGraphics.blit(BACKGROUND, x + 90, y + 35 + 16 - arrowWidth, 176, 16 + 16 - arrowWidth, 16, arrowWidth);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        if (isHovering(79, 35, 24, 17, mouseX, mouseY)) {
            int progress = menu.getProgress();
            int maxProgress = menu.getMaxProgress();

            if (maxProgress > 0) {
                String tooltip = String.format("Progress: %d / %d ticks", progress, maxProgress);
                guiGraphics.renderTooltip(this.font, Component.nullToEmpty(tooltip), mouseX, mouseY);
            }
        }

        if (isHovering(56, 35, 16, 16, mouseX, mouseY)) {
            DecompressorManager.DecompressRecipeData recipe = this.menu.getTile().getCurrentRecipe();
            if (recipe != null) {
                String tooltip = String.format("Decompresses into %dx %s",
                        recipe.getCount(),
                        recipe.getIngredient().getHoverName().getString());
                guiGraphics.renderTooltip(this.font, Component.nullToEmpty(tooltip), mouseX, mouseY);
            }
        }
    }
}