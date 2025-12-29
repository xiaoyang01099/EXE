package net.xiaoyang010.ex_enigmaticlegacy.Client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ContainerInfinityCompressor;
import org.jetbrains.annotations.NotNull;

import static net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod.MODID;

public class GuiInfinityCompressor extends AbstractContainerScreen<ContainerInfinityCompressor> {
    private static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(MODID, "textures/gui/infinity_compressor.png");
    private static final boolean JEI_PRESENT = ModList.get().isLoaded("jei");

    public GuiInfinityCompressor(ContainerInfinityCompressor container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.imageHeight = 176;
        this.imageWidth = 166;
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, RESOURCE_LOCATION);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        RenderSystem.disableBlend();
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }
}