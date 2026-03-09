package net.xiaoyang010.ex_enigmaticlegacy.Util;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.api.IWaveName;

public class WaveNameData implements TooltipComponent {
    private final ItemStack           stack;
    private final IWaveName.WaveStyle style;
    private final String              rawText;

    public WaveNameData(ItemStack stack, IWaveName.WaveStyle style, String rawText) {
        this.stack   = stack;
        this.style   = style;
        this.rawText = rawText;
    }

    public ItemStack getStack()   {
        return stack;
    }

    public IWaveName.WaveStyle getStyle()   {
        return style;
    }

    public String getRawText() {
        return rawText;
    }
}