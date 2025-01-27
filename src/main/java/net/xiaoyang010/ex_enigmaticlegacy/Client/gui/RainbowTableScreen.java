package net.xiaoyang010.ex_enigmaticlegacy.Client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.xiaoyang010.ex_enigmaticlegacy.Container.RainbowTableContainer;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

public class RainbowTableScreen extends AbstractContainerScreen<RainbowTableContainer> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/gui/container/rainbow_nature.png");

    public RainbowTableScreen(RainbowTableContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(PoseStack ms, float partialTicks, int gx, int gy) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(ms, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int time = this.menu.getTime();
        if (time > 0) {
            this.blit(ms, this.leftPos + 99, this.topPos + 51, 176, 0, time, 5);
        }

        int mana = this.menu.getMana();
        if (mana > 0) {
            this.blit(ms, this.leftPos + 40, this.topPos + 74, 0, 183, mana, 5);
        }

        RenderSystem.disableBlend();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}