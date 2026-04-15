package org.xiaoyang.ex_enigmaticlegacy.Client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.xiaoyang.ex_enigmaticlegacy.Container.RainbowTableContainer;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

public class RainbowTableScreen extends AbstractContainerScreen<RainbowTableContainer> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Exe.MODID, "textures/gui/container/rainbow_nature.png");

    public RainbowTableScreen(RainbowTableContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
        guiGraphics.setColor(1f, 1f, 1f, 1f);
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int time = this.menu.getTime();
        if (time > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + 99, this.topPos + 51, 176, 0, time, 5);
        }

        int mana = this.menu.getMana();
        if (mana > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + 40, this.topPos + 74, 0, 183, mana, 5);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}