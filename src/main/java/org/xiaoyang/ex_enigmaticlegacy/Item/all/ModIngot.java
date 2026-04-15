package org.xiaoyang.ex_enigmaticlegacy.Item.all;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.xiaoyang.ex_enigmaticlegacy.api.exboapi.IWaveName;


public class ModIngot extends Item implements IWaveName {
    public ModIngot(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.getDamageValue() == 2;
    }

    @Override
    public WaveStyle getWaveStyle(ItemStack stack) {
        return WaveStyle.GLITCH;
    }
}
