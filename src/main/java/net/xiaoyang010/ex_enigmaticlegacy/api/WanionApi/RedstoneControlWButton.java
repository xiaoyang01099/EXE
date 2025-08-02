package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public final class RedstoneControlWButton extends ControlWButton<RedstoneControl, RedstoneControl.RedstoneState> {
    private final boolean hoverBelow;

    public RedstoneControlWButton(@Nonnull final RedstoneControl redStoneControl, @Nonnull final WGuiContainer<?> wGuiContainer, final int x, final int y) {
        this(redStoneControl, wGuiContainer, x, y, false);
    }

    public RedstoneControlWButton(@Nonnull final RedstoneControl redStoneControl, @Nonnull final WGuiContainer<?> wGuiContainer, final int x, final int y, final boolean hoverBelow) {
        super(redStoneControl, wGuiContainer, x, y);
        this.hoverBelow = hoverBelow;
    }

    @Override
    public int getTooltipX(@Nonnull final WInteraction wInteraction) {
        return wInteraction.getMouseX() - (lineWidth / 2) - 12 - wGuiContainer.leftPos;
    }

    @Override
    public int getTooltipY(@Nonnull final WInteraction wInteraction) {
        return wInteraction.getMouseY() - (hoverBelow ? -30 : 20) - wGuiContainer.topPos;
    }
}