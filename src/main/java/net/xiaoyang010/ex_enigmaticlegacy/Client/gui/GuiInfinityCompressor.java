package net.xiaoyang010.ex_enigmaticlegacy.Client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import morph.avaritia.client.gui.AnimScreenBase;
import morph.avaritia.client.gui.DrawableElement.AnimationDirection;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ContainerInfinityCompressor;


public class GuiInfinityCompressor extends AnimScreenBase<ContainerInfinityCompressor> {
    public static final ResourceLocation TEXTURE = new ResourceLocation("avaritia", "textures/gui/compressor.png");
    public static final TranslatableComponent TITLE = new TranslatableComponent("container.ex_enigmaticlegacy.infinity_compressor.name");

    public GuiInfinityCompressor(ContainerInfinityCompressor menu, Inventory playerInv, Component title) {
        super(menu, playerInv, TITLE);
        this.setBackgroundTexture(TEXTURE);
        this.addDrawable().location(62, 35).size(176, 0, 22, 16).animationDirection(AnimationDirection.LEFT_RIGHT).renderPredicate(() -> {
            return menu.machineTile.getConsumptionProgress() > 0;
        }).progressSupplier(() -> {
            return Math.min(1.0d, (double)(menu.machineTile).getConsumptionProgress() / (double)(menu.machineTile).getConsumptionTarget());
        }).add();
        this.addDrawable().location(90, 35).size(176, 16, 16, 16).animationDirection(AnimationDirection.BOTTOM_UP).renderPredicate(() -> {
            return (menu.machineTile).getCompressionProgress() > 0;
        }).progressSupplier(() -> {
            return Math.min(1.0d, (double)(menu.machineTile).getCompressionProgress() / (double)(menu.machineTile).getCompressionTarget());
        }).tooltipSupplier(() -> {
            return new TextComponent(String.format("%.2f%%", 100.0 * ((double)(menu.machineTile).getCompressionProgress() / (double)(menu.machineTile).getCompressionTarget())));
        }).add();
    }

    public void render(PoseStack pStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(pStack);
        super.render(pStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(pStack, mouseX, mouseY);
    }

    protected void renderLabels(PoseStack pStack, int mouseX, int mouseY) {
        this.titleLabelX = this.imageWidth / 2 - this.font.width(this.title) / 2;
        super.renderLabels(pStack, mouseX, mouseY);
        if (((this.menu).machineTile).getCompressionProgress() > 0) {
            int var10000 = ((this.menu).machineTile).getCompressionProgress();
            String s = "" + var10000 + " / " + ((this.menu).machineTile).getCompressionTarget();
            int x = this.imageWidth / 2 - this.font.width(s) / 2;
            this.font.draw(pStack, s, (float)x, 60.0F, 4210752);
        }

    }

    protected void renderBg(PoseStack pStack, float partialTicks, int mouseX, int mouseY) {
        this.drawBackground(pStack);
        super.renderBg(pStack, partialTicks, mouseX, mouseY);
    }
}