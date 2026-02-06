package net.xiaoyang010.ex_enigmaticlegacy.Client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.xiaoyang010.ex_enigmaticlegacy.Container.NeutroniumDecompressorMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.NeutroniumDecompressorTile;
import net.xiaoyang010.ex_enigmaticlegacy.Util.DecompressorManager;

public class NeutroniumDecompressorScreen extends AbstractContainerScreen<NeutroniumDecompressorMenu> {

    public static final ResourceLocation BACKGROUND = new ResourceLocation(
            "ex_enigmaticlegacy", "textures/gui/container/neutronium_decompressor.png");

    public static final TranslatableComponent TITLE = new TranslatableComponent(
            "container.ex_enigmaticlegacy.neutronium_decompressor");

    public NeutroniumDecompressorScreen(NeutroniumDecompressorMenu menu,
                                        Inventory playerInv, Component title) {
        super(menu, playerInv, TITLE);
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(PoseStack pStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(pStack);
        super.render(pStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(pStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack pStack, int mouseX, int mouseY) {
        this.titleLabelX = this.imageWidth / 2 - this.font.width(this.title) / 2;
        super.renderLabels(pStack, mouseX, mouseY);

        NeutroniumDecompressorTile tile = this.menu.getTile();
        int progress = tile.getProgress();
        int maxProgress = tile.getMaxProgress();

        if (maxProgress > 0) {
            String progressText = String.format("%.2f%%",
                    100.0F * ((float) progress / (float) maxProgress));

            int textX = this.imageWidth / 2 - this.font.width(progressText) / 2;
            this.font.draw(pStack, progressText, (float) textX, 60.0F, 0x404040);
        }

        renderDecompressInfo(pStack);
    }

    private void renderDecompressInfo(PoseStack pStack) {
        DecompressorManager.DecompressRecipeData recipe = this.menu.getTile().getCurrentRecipe();

        if (recipe != null) {
            String info = "1x → " + recipe.getCount() + "x";
            int infoX = this.imageWidth / 2 - this.font.width(info) / 2;
            this.font.draw(pStack, info, (float) infoX, 51.0F, 0x808080);
        }
    }

    @Override
    protected void renderBg(PoseStack pStack, float partialTicks, int mouseX, int mouseY) {
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, BACKGROUND);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.blit(pStack, x, y, 0, 0, this.imageWidth, this.imageHeight);

        renderProgressArrow(pStack, x, y);
    }

    private void renderProgressArrow(PoseStack pStack, int x, int y) {
        int progress = menu.getProgress();
        int maxProgress = menu.getMaxProgress();

        if (maxProgress > 0 && progress > 0) {
            int arrowWidth = (int) Math.floor(progress * 16.0f / maxProgress);
            int w = (int) Math.floor(progress * 22.0f / maxProgress);
            this.blit(pStack, x + 62, y + 35, 176, 0, w, 16);
            this.blit(pStack, x + 90, y + 35 + 16 - arrowWidth, 176, 16 + 16 - arrowWidth, 16, arrowWidth);
        }
    }

    @Override
    protected void renderTooltip(PoseStack pStack, int mouseX, int mouseY) {
        super.renderTooltip(pStack, mouseX, mouseY);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        if (isHovering(79, 35, 24, 17, mouseX, mouseY)) {
            int progress = menu.getProgress();
            int maxProgress = menu.getMaxProgress();

            if (maxProgress > 0) {
                String tooltip = String.format("Progress: %d / %d ticks", progress, maxProgress);
                this.renderTooltip(pStack, Component.nullToEmpty(tooltip), mouseX, mouseY);
            }
        }

        if (isHovering(56, 35, 16, 16, mouseX, mouseY)) {
            DecompressorManager.DecompressRecipeData recipe = this.menu.getTile().getCurrentRecipe();
            if (recipe != null) {
                String tooltip = String.format("Decompresses into %dx %s",
                        recipe.getCount(),
                        recipe.getIngredient().getHoverName().getString());
                this.renderTooltip(pStack, Component.nullToEmpty(tooltip), mouseX, mouseY);
            }
        }
    }
}