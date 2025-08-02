package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IField;


import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public abstract class WField<F extends IField<F>> extends WElement<WField<F>> {
    public static final ResourceLocation GUI_TEXTURES = new ResourceLocation("ex_enigmaticlegacy:textures/gui/gui_textures.png");

    protected final F field;

    public WField(@Nonnull final F field, @Nonnull final WGuiContainer<?> wGuiContainer, final int x, final int y, final int width, final int height) {
        super(wGuiContainer, x, y, width, height);
        this.field = field;
    }

    public F getField() {
        return field;
    }
}