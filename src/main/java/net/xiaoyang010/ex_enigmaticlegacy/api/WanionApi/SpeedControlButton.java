package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SpeedControlButton extends Button
{
    private final AbstractContainerScreen<?> containerScreen;
    private final ResourceLocation resourceLocation;
    public static final ResourceLocation GUI_TEXTURES = new ResourceLocation("ex_enigmaticlegacy:textures/gui/gui_textures.png");


    public SpeedControlButton(@Nonnull final AbstractContainerScreen<?> containerScreen, final int x, final int y)
    {
        this(containerScreen, x, y, GUI_TEXTURES);
    }

    public SpeedControlButton(@Nonnull final AbstractContainerScreen<?> containerScreen, final int x, final int y, @Nonnull ResourceLocation resourceLocation)
    {
        super(x, y, 18, 18, TextComponent.EMPTY, button -> {
        });
        this.containerScreen = containerScreen;
        this.resourceLocation = resourceLocation;
    }

    @Override
    public void renderButton(@Nonnull PoseStack poseStack, final int mouseX, final int mouseY, final float partialTicks)
    {
        if (!this.visible)
            return;

        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, resourceLocation);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

        blit(poseStack, x, y, 36, !isHovered ? 36 : 54, 18, 18, 128, 128);
    }

    public void renderButtonForegroundLayer(@Nonnull PoseStack poseStack, final int mouseX, final int mouseY)
    {
        if (!this.isHovered)
            return;

        int width = 0;
        final Font font = Minecraft.getInstance().font;
        final List<Component> description = Lists.newArrayList(
                new TextComponent(ChatFormatting.RED + "Speed Control: ")
        );

        for (final Component line : description) {
            final int lineWidth = font.width(line);
            if (lineWidth > width)
                width = lineWidth;
        }

        containerScreen.renderComponentTooltip(poseStack, description,
                mouseX - (width / 2) - 12 - containerScreen.getGuiLeft(),
                mouseY - 20 - containerScreen.getGuiTop());
    }

    public void renderTooltip(@Nonnull PoseStack poseStack, final int mouseX, final int mouseY)
    {
        if (!this.isHovered)
            return;

        final List<Component> tooltip = Lists.newArrayList(
                new TextComponent(ChatFormatting.RED + "Speed Control: ")
        );

        containerScreen.renderTooltip(poseStack, (Component) tooltip, mouseX, mouseY);
    }
}