package org.xiaoyang.ex_enigmaticlegacy.Client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.xiaoyang.ex_enigmaticlegacy.Container.DimensionalMirrorContainer;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.TeleportPacket;

import java.util.List;

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
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        drawDimensionButtons(guiGraphics, x, y, mouseX, mouseY);
        drawConfirmButton(guiGraphics, x, y, mouseX, mouseY);
    }

    private void drawDimensionButtons(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        boolean isOverworldSelected = selectedDimension == Level.OVERWORLD;
        drawButton(guiGraphics, x + 30, y + 20,
                Component.translatable("gui.dimension.overworld").getString(),
                Items.GRASS_BLOCK, isOverworldSelected);

        boolean isNetherSelected = selectedDimension == Level.NETHER;
        drawButton(guiGraphics, x + 30, y + 50,
                Component.translatable("gui.dimension.nether").getString(),
                Items.NETHERRACK, isNetherSelected);

        boolean isEndSelected = selectedDimension == Level.END;
        drawButton(guiGraphics, x + 30, y + 80,
                Component.translatable("gui.dimension.end").getString(),
                Items.END_STONE, isEndSelected);
    }

    private void drawButton(GuiGraphics guiGraphics, int x, int y, String text, Item icon, boolean selected) {
        guiGraphics.fill(x, y, x + 116, y + 20, selected ? 0xFF666666 : 0xFF555555);
        guiGraphics.renderItem(new ItemStack(icon), x + 2, y + 2);
        guiGraphics.drawString(font, text, x + 24, y + 6, selected ? 0xFFFFAA : 0xFFFFFF);

        if (selected) {
            guiGraphics.fill(x - 2, y, x, y + 20, 0xFF00FF00);
        }
    }

    private void drawConfirmButton(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        boolean canTeleport = selectedDimension != null && menu.hasRequiredItems(selectedDimension);
        int buttonColor = canTeleport ? 0xFF375537 : 0xFF553737;

        guiGraphics.fill(x + 30, y + 110, x + 146, y + 130, buttonColor);
        guiGraphics.drawString(font,
                Component.translatable("gui.dimension.teleport.confirm").getString(),
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        if (selectedDimension != null) {
            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;
            if (mouseY >= y + 110 && mouseY <= y + 130 &&
                    mouseX >= x + 30 && mouseX <= x + 146) {
                if (!menu.hasRequiredItems(selectedDimension)) {
                    guiGraphics.renderComponentTooltip(this.font,
                            List.of(Component.translatable("gui.dimension.teleport.missing_items")),
                            mouseX, mouseY);
                }
            }
        }
    }
}