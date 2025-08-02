package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public final class WKeyInteraction extends WInteraction {
    private final char key;
    private final int keyCode;

    public WKeyInteraction(@Nonnull final WGuiContainer<?> wGuiContainer, final int mouseX, int mouseY, final char key, final int keyCode) {
        super(wGuiContainer, mouseX, mouseY);
        this.key = key;
        this.keyCode = keyCode;
    }

    public WKeyInteraction(@Nonnull final WGuiContainer<?> wGuiContainer, final char key, final int keyCode) {
        super(wGuiContainer);
        this.key = key;
        this.keyCode = keyCode;
    }

    public WKeyInteraction(@Nonnull final WGuiContainer<?> wGuiContainer) {
        super(wGuiContainer);
        this.key = '\0';
        this.keyCode = -1;
    }

    public WKeyInteraction(@Nonnull final WGuiContainer<?> wGuiContainer, final int keyCode) {
        super(wGuiContainer);
        this.key = '\0';
        this.keyCode = keyCode;
    }

    public char getKey()
    {
        return key;
    }

    public int getKeyCode()
    {
        return keyCode;
    }

    @FunctionalInterface
    public interface IWKeyInteraction {
        void interact(@Nonnull final WKeyInteraction wKeyInteraction);
    }
}