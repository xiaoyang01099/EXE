package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class WMouseInteraction extends WInteraction
{
    private final int mouseButton;

    public WMouseInteraction(@Nonnull final WGuiContainer<?> wGuiContainer, final int mouseX, int mouseY, int mouseButton)
    {
        super(wGuiContainer, mouseX, mouseY);
        this.mouseButton = mouseButton;
    }

    public WMouseInteraction(@Nonnull final WGuiContainer<?> wGuiContainer)
    {
        super(wGuiContainer);
        this.mouseButton = -1;
    }

    public WMouseInteraction(@Nonnull final WGuiContainer<?> wGuiContainer, int mouseButton)
    {
        super(wGuiContainer);
        this.mouseButton = mouseButton;
    }

    public int getMouseButton()
    {
        return mouseButton;
    }

    @FunctionalInterface
    public interface IWMouseInteraction
    {
        void interact(@Nonnull final WMouseInteraction wMouseInteraction);
    }
}