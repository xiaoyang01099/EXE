package net.xiaoyang010.ex_enigmaticlegacy.Client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import net.xiaoyang010.ex_enigmaticlegacy.Container.DimensionalMirrorContainer;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.TeleportPacket;

public class DimensionalMirrorScreen extends AbstractContainerScreen<DimensionalMirrorContainer> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ex_enigmaticlegacy", "textures/gui/dimensional_mirror.png");
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 246;

    private ResourceKey<Level> selectedDimension = null;

    public DimensionalMirrorScreen(DimensionalMirrorContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        this.blit(matrixStack, x, y, 0, 0, imageWidth, imageHeight);

        drawDimensionButtons(matrixStack, x, y, mouseX, mouseY);

        drawConfirmButton(matrixStack, x, y, mouseX, mouseY);
    }

    private void drawDimensionButtons(PoseStack matrixStack, int x, int y, int mouseX, int mouseY) {
        // 主世界按钮
        boolean isOverworldSelected = selectedDimension == Level.OVERWORLD;
        drawButton(matrixStack, x + 30, y + 20,
                new TranslatableComponent("gui.dimension.overworld").getString(),
                Items.GRASS_BLOCK, isOverworldSelected);

        // 地狱按钮
        boolean isNetherSelected = selectedDimension == Level.NETHER;
        drawButton(matrixStack, x + 30, y + 50,
                new TranslatableComponent("gui.dimension.nether").getString(),
                Items.NETHERRACK, isNetherSelected);

        // 末地按钮
        boolean isEndSelected = selectedDimension == Level.END;
        drawButton(matrixStack, x + 30, y + 80,
                new TranslatableComponent("gui.dimension.end").getString(),
                Items.END_STONE, isEndSelected);
    }

    private void drawButton(PoseStack matrixStack, int x, int y, String text, Item icon, boolean selected) {
        fill(matrixStack, x, y, x + 116, y + 20, selected ? 0xFF666666 : 0xFF555555);
        itemRenderer.renderAndDecorateItem(new ItemStack(icon), x + 2, y + 2);
        font.draw(matrixStack, text, x + 24, y + 6, selected ? 0xFFFFAA : 0xFFFFFF);

        if (selected) {
            fill(matrixStack, x - 2, y, x, y + 20, 0xFF00FF00);
        }
    }

    private void drawConfirmButton(PoseStack matrixStack, int x, int y, int mouseX, int mouseY) {
        boolean canTeleport = selectedDimension != null && menu.hasRequiredItems(selectedDimension);
        int buttonColor = canTeleport ? 0xFF375537 : 0xFF553737;

        fill(matrixStack, x + 30, y + 110, x + 146, y + 130, buttonColor);
        font.draw(matrixStack,
                new TranslatableComponent("gui.dimension.teleport.confirm").getString(),
                x + 70, y + 116,
                canTeleport ? 0xFFFFFF : 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        if (isInButton(mouseX, mouseY, x + 30, y + 20)) {
            selectedDimension = Level.OVERWORLD;
            return true;
        } else if (isInButton(mouseX, mouseY, x + 30, y + 50)) {
            selectedDimension = Level.NETHER;
            return true;
        } else if (isInButton(mouseX, mouseY, x + 30, y + 80)) {
            selectedDimension = Level.END;
            return true;
        }

        if (isInButton(mouseX, mouseY, x + 30, y + 110)) {
            if (selectedDimension != null && menu.hasRequiredItems(selectedDimension)) {
                NetworkHandler.CHANNEL.sendToServer(new TeleportPacket(selectedDimension));
                selectedDimension = null;
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isInButton(double mouseX, double mouseY, int buttonX, int buttonY) {
        return mouseX >= buttonX && mouseX <= buttonX + 116 &&
                mouseY >= buttonY && mouseY <= buttonY + 20;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);

        if (selectedDimension != null) {
            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;
            if (mouseY >= y + 110 && mouseY <= y + 130 &&
                    mouseX >= x + 30 && mouseX <= x + 146) {
                if (!menu.hasRequiredItems(selectedDimension)) {
                    renderTooltip(matrixStack,
                            new TranslatableComponent("gui.dimension.teleport.missing_items"),
                            mouseX, mouseY);
                }
            }
        }
    }
}