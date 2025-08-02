package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class LetterBoxElement extends WElement<LetterBoxElement>
{
    protected static final ResourceLocation DEFAULT_RESOURCE_LOCATION = new ResourceLocation("ex_enigmaticlegacy", "textures/gui/gui_textures.png");

    private final String letter;
    private final Font fontRenderer;
    private final Supplier<Integer> letterX, letterY;

    public LetterBoxElement(final char letter, @Nonnull final WGuiContainer<?> wGuiContainer, int x, int y) {
        super(wGuiContainer, x, y, 18, 18);
        this.letter = ChatFormatting.BOLD + Character.toString(letter);
        this.fontRenderer = getFontRenderer();
        this.letterX = () -> getUsableX() + 9 - (fontRenderer.width(Character.toString(letter)) / 2);
        this.letterY = () -> getUsableY() + 10 - (fontRenderer.lineHeight / 2);
    }

    @Override
    public void draw(@Nonnull final WInteraction wInteraction) {
        bindTexture(DEFAULT_RESOURCE_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        PoseStack poseStack = new PoseStack();

        GuiComponent.blit(poseStack, getUsableX(), getUsableY(),
                !wInteraction.isHovering(this) ? 36 : 54, 72,
                width, height, 128, 128);

        fontRenderer.drawShadow(poseStack, letter, letterX.get(), letterY.get(), 0xFFFFFF);
    }
}