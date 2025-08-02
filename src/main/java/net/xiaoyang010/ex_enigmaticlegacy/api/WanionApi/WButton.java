package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public abstract class WButton<B extends WButton<B>> extends WElement<B> {
    public WButton(@NotNull final WGuiContainer<?> wGuiContainer, final int x, final int y, final int width, final int height)
    {
        this(wGuiContainer, x, y, width, height, true);
    }

    public WButton(@NotNull final WGuiContainer<?> wGuiContainer, final int x, final int y, final int width, final int height, final boolean enabled)
    {
        super(wGuiContainer, x, y, width, height);
        this.enabled = enabled;
    }
}